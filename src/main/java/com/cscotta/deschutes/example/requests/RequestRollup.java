package com.cscotta.deschutes.example.requests;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import java.util.concurrent.atomic.AtomicLong;
import com.cscotta.deschutes.api.ConcurrentRollup;

public class RequestRollup implements ConcurrentRollup<RequestEvent> {

    private final long timestamp;

    public RequestRollup(long timestamp) {
        this.timestamp = timestamp;
    }

    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong totalRequestBytes = new AtomicLong(0);
    private final AtomicLong totalResponseBytes = new AtomicLong(0);

    private final Histogram requestBytesHisto = new Histogram(new UniformReservoir());
    private final Histogram responseBytesHisto = new Histogram(new UniformReservoir());

    private final Histogram handshakeUsec = new Histogram(new UniformReservoir());
    private final Histogram usecToFirstByte = new Histogram(new UniformReservoir());
    private final Histogram usecTotal = new Histogram(new UniformReservoir());


    public long getRequestCount() { return requestCount.get(); }
    public long getTotalRequestBytes() { return totalRequestBytes.get(); }
    public long getTotalResponseBytes() { return totalResponseBytes.get(); }

    public Snapshot getRequestBytesHisto() { return requestBytesHisto.getSnapshot(); }
    public Snapshot getResponseBytesHisto() { return responseBytesHisto.getSnapshot(); }

    public Snapshot getHandshakeUsecHisto() { return handshakeUsec.getSnapshot(); }
    public Snapshot getUsecToFirstByteHisto() { return usecToFirstByte.getSnapshot(); }
    public Snapshot getUsecTotalHisto() { return usecTotal.getSnapshot(); }

    @Override
    public void rollup(RequestEvent request) {
        requestCount.incrementAndGet();
        totalRequestBytes.addAndGet(request.getRequestBytes());
        totalResponseBytes.addAndGet(request.getResponseBytes());
        requestBytesHisto.update(request.getRequestBytes());
        responseBytesHisto.update(request.getResponseBytes());
        handshakeUsec.update(request.getHandshakeUsec());
        usecToFirstByte.update(request.getUsecToFirstByte());
        usecTotal.update(request.getUsecTotal());
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }
}

