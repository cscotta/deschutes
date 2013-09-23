package com.cscotta.deschutes.example.source;

import com.cscotta.deschutes.example.requests.RequestEvent;
import com.cscotta.deschutes.example.requests.Requests;

import java.util.Iterator;
import java.util.Random;

public class SyntheticProtobufs implements Iterable<RequestEvent> {

    private static final Random rand = new Random();
    private static final int[] responseCodes = new int[] {200, 400, 404, 500};
    private static final String[] urls = new String[] {"article", "video", "comments", "download"};
    private static final String[] agents = new String[] {"Chrome", "Firefox", "Safari", "IE"};
    private static final String[] locales = new String[] {"en-US", "en-GB", "es-ES", "fr-FR"};

    private final int numServers;
    private final int numUsers;

    public SyntheticProtobufs(int numServers, int numUsers) {
        this.numServers = numServers;
        this.numUsers = numUsers;
    }

    public Requests.Request generateProtobuf() {

        final String action = urls[rand.nextInt(urls.length)];

        final long handshakeUsec = rand.nextInt(5 * 1000000);
        final long usecToFirstByte = handshakeUsec + rand.nextInt(5 * 1000000);
        final long usecTotal = usecToFirstByte + rand.nextInt(5 * 1000000);

        return  Requests.Request.newBuilder().
                setTimestamp(System.currentTimeMillis() / 1000 * 1000).
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
    }

    @Override
    public Iterator<RequestEvent> iterator() {
        return new Iterator<RequestEvent>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public RequestEvent next() {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return new RequestEvent(generateProtobuf());
            }

            @Override
            public void remove() {
                throw new RuntimeException("Not implemented.");
            }
        };
    }
}
