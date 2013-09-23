package com.cscotta.deschutes.example.source;

import com.cscotta.deschutes.example.requests.Requests;

import java.io.*;
import java.util.Random;

public class GenerateData {

    private static final Random rand = new Random();
    private static final int[] responseCodes = new int[] {200, 400, 404, 500};
    private static final String[] urls = new String[] {"article", "video", "comments", "download"};
    private static final String[] agents = new String[] {"Chrome", "Firefox", "Safari", "IE"};
    private static final String[] locales = new String[] {"en-US", "en-GB", "es-ES", "fr-FR"};

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.out.println("Usage: GenerateData <events/sec> <seconds> <numServers> <numUsers> <outFilename>");
            return;
        }

        long startedAt = System.currentTimeMillis() / 1000 * 1000;
        int eventsPerSecond = Integer.parseInt(args[0]);
        int millis = Integer.parseInt(args[1]) * 1000;
        int numServers = Integer.parseInt(args[2]);
        int numUsers = Integer.parseInt(args[3]);
        String outputFilename = args[4];

        OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outputFilename)));

        System.out.println("Writing " + (millis / 1000) + " seconds of data at " + eventsPerSecond +
            " events/sec with up to " + numServers + " distinct servers and up to " +
            numUsers + " distinct users to " + outputFilename);

        for (long ts = startedAt; ts < startedAt + millis; ts += 1000) {
            System.out.println("Writing events for " + ts);
            for (int event = 0; event < eventsPerSecond; event ++) {
                final String action = urls[rand.nextInt(urls.length)];

                final long handshakeUsec = rand.nextInt(5 * 1000000);
                final long usecToFirstByte = handshakeUsec + rand.nextInt(5 * 1000000);
                final long usecTotal = usecToFirstByte + rand.nextInt(5 * 1000000);

                final Requests.Request proto = Requests.Request.newBuilder().
                        setTimestamp(ts).
                        setServerId(rand.nextInt(numServers)).
                        setUserId(rand.nextInt(numServers)).
                        setClientIp(rand.nextInt(numUsers)).
                        setServerIp(rand.nextInt(numServers)).
                        setOperationType(Requests.Request.OperationType.valueOf(rand.nextInt(3))).
                        setProtocol(Requests.Request.Protocol.valueOf(rand.nextInt(4))).
                        setResponseCode(responseCodes[rand.nextInt(responseCodes.length)]).
                        setUri("/" + action + "/" + rand.nextInt(1000)).
                        setRequestBytes(rand.nextInt(1024 * 5)).
                        setResponseBytes(rand.nextInt(1024 * 1024 * 10)).
                        setHandshakeUsec(handshakeUsec).
                        setUsecToFirstByte(usecToFirstByte).
                        setUsecTotal(usecTotal).setUserAgent(agents[rand.nextInt(agents.length)]).
                        setLocale(locales[rand.nextInt(locales.length)]).
                        setReferrer("/" + action + "/" + rand.nextInt(1000)).
                        build();

                proto.writeDelimitedTo(out);
            }
        }

        out.flush();
        out.close();
        System.out.println("Done! Output written to " + outputFilename);
    }
}
