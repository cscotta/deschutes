package com.cscotta.deschutes.example.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

import com.cscotta.deschutes.aggregation.*;
import com.cscotta.deschutes.api.OutputListener;
import com.cscotta.deschutes.example.app.websockets.WebsocketApi;
import com.cscotta.deschutes.example.requests.RequestRollups;
import com.cscotta.deschutes.example.requests.*;
import com.cscotta.deschutes.example.source.SyntheticProtobufs;
import com.cscotta.deschutes.input.CyclingProtobufPreReader;
import com.cscotta.deschutes.input.ProtobufReader;
import com.cscotta.deschutes.example.requests.Requests.Request;

/**
 * Main application entry point.
 */
public class App extends Service<Config> {

    public static final MetricRegistry metrics = new MetricRegistry();
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
    ConsoleReporter reporter;

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        bootstrap.setName("app");
    }

    @Override
    public void run(Config config, Environment env) throws Exception {
        WebsocketApi websocketApi = new WebsocketApi();
        env.addServlet(websocketApi, "/websocket");

        // Select a protobuf reader, a pre-loaded protobuf reader, or a synthetic stream.
        final Iterable<RequestEvent> reader;
        if (config.getDataSource().equals("generated")) {
            BufferedInputStream input = new BufferedInputStream(
                    new FileInputStream(new File(config.getDataFile())), 1024 * 1024);
            reader = new ProtobufReader<Request, RequestEvent>(
                    Request.PARSER, new RequestEventWrapper(), input);
        } else if (config.getDataSource().equals("preloaded")) {
            BufferedInputStream input = new BufferedInputStream(
                    new FileInputStream(new File(config.getDataFile())), 1024 * 1024);
            reader = new CyclingProtobufPreReader<Request, RequestEvent>(
                    Request.PARSER, new RequestEventWrapper(), input);
            ((CyclingProtobufPreReader<Request, RequestEvent>) reader).initialize();
        } else {
            reader = new SyntheticProtobufs(1000, 1000);
        }

        // Report metrics to the console.
        reporter = ConsoleReporter.forRegistry(metrics).
            convertRatesTo(TimeUnit.SECONDS).
            convertDurationsTo(TimeUnit.MILLISECONDS).
            build();
        reporter.start(30, TimeUnit.SECONDS);

        // Initialize websocket output
        OutputListener<RequestEvent> output = new WebsocketRollupListener(websocketApi.users);

        // Prepare our stream
        final Stream<RequestEvent> stream = new Stream<RequestEvent>(reader, config.getProcessorThreads());

        // Add aggregator(s) to stream - nonblocking or lock-striped for now.
        if (config.getAggregatorType().equals("nonblocking")) {
            log.info("Launching non-blocking stream.");
            stream.addQuery(
                new OLAPAggregator<RequestEvent>(Dimensions.protocol, RequestRollups.collapser)
                    .addListener(new WebsocketRollupListener(websocketApi.users)));

        } else if (config.getAggregatorType().equals("lock_striped")) {
            log.info("Launching lock-striped stream.");
                stream.addQuery(
                    new OLAPAggregator<RequestEvent>(Dimensions.protocol, RequestRollups.collapser)
                        .addListener(new WebsocketRollupListener(websocketApi.users)));
        } else {
            log.error("Unknown stream type specified.");
        }

        stream.launch();

        // Optionally terminate program after x seconds for benchmarking purposes.
        if (config.getRunFor() > 0) {
            log.info("Scheduling termination of program in " + config.getRunFor() + " seconds...");
            stpe.schedule(new Runnable() {
                @Override
                public void run() {
                    log.info("Terminating program. Final results: ");
                    reporter.report();
                    System.exit(0);
                }
            }, config.getRunFor(), TimeUnit.SECONDS);
        }
    }
}
