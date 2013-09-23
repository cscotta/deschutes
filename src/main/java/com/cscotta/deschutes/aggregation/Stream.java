package com.cscotta.deschutes.aggregation;

import com.codahale.metrics.Meter;
import com.cscotta.deschutes.api.Event;
import com.cscotta.deschutes.api.StreamAggregator;
import com.cscotta.deschutes.example.app.App;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Stream<Input extends Event> {

    private final Iterable<Input> source;
    private final Meter meter = App.metrics.meter("event_rate");
    private final int processorThreads;
    private static final Logger log = LoggerFactory.getLogger(Stream.class);

    private final List<StreamAggregator<Input>> queries = Lists.newCopyOnWriteArrayList();
    private final ExecutorService consumer = Executors.newSingleThreadExecutor();
    private final ExecutorService processor;
    private final LinkedBlockingQueue<List<Input>> buffer = new LinkedBlockingQueue<List<Input>>(10000);
    public long startedAt = 0L;
    private final int batchSize = 1;

    public Stream(Iterable<Input> source, int processorThreads) {
        this.source = source;
        this.processorThreads = processorThreads;
        this.processor = Executors.newFixedThreadPool(processorThreads);
    }

    public void launch() {
        consumer.submit(consumeFromSource);
        startedAt = System.currentTimeMillis();
        for (int i = 0; i < processorThreads; i++) {
            log.info("Launching thread " + i + " at " + System.currentTimeMillis());
            processor.submit(processInput());
        }
    }

    public Stream<Input> addQuery(StreamAggregator<Input> aggr) {
        queries.add(aggr);
        return this;
    }

    private void observe(Input event) {
        if (event == null) {
            log.error("Got a null event, how?!");
            return;
        }
        meter.mark();
        for (StreamAggregator<Input> query : queries) {
            query.observe(event);
        }
    }

    private Runnable consumeFromSource = new Runnable() {
        @Override
        public void run() {
            List<Input> buf = new ArrayList<Input>(batchSize);
            while (true) {
                try {
                    for (Input event : source) {
                        if (buf.size() < batchSize) {
                            buf.add(event);
                        } else {
                            buffer.put(buf);
                            buf = new ArrayList<Input>(batchSize);
                        }
                    }
                    buffer.put(buf);
                } catch (Exception e) {
                    log.error("Error consuming input.", e);
                }
            }
        }
    };

    private Runnable processInput()  {
        return new Runnable() {
            @Override
            public void run() {
                log.info("Launching " + Thread.currentThread().getName());
                try {
                    while (true) for (Input rec : buffer.take()) observe(rec);
                } catch (Exception e) {
                    log.error("Error processing input!", e);
                }
            }
        };
    }
}
