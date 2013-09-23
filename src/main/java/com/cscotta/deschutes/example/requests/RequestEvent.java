package com.cscotta.deschutes.example.requests;

import com.cscotta.deschutes.api.Event;
import com.cscotta.deschutes.example.requests.Requests.Request;
import com.cscotta.deschutes.example.requests.Requests.Request.Protocol;
import com.cscotta.deschutes.example.requests.Requests.Request.OperationType;

public class RequestEvent implements Event {

    private final Request request;

    public RequestEvent(Request request) {
       this.request = request;
    }

    @Override
    public long getTimestamp() {
        return request.getTimestamp();
    }

    public int getUserId() {
        return request.getUserId();
    }

    public int getClientIp() {
        return request.getClientIp();
    }

    public int getServerIp() {
        return request.getServerIp();
    }

    public OperationType getOperationType() {
        return request.getOperationType();
    }

    public Protocol getProtocol() {
        return request.getProtocol();
    }

    public int getResponseCode() {
        return request.getResponseCode();
    }

    public String getUri() {
        return request.getUri();
    }

    public int getRequestBytes() {
        return request.getRequestBytes();
    }

    public int getResponseBytes() {
        return request.getResponseBytes();
    }

    public long getHandshakeUsec() {
        return request.getHandshakeUsec();
    }

    public long getUsecToFirstByte() {
        return request.getUsecToFirstByte();
    }

    public long getUsecTotal() {
        return request.getUsecTotal();
    }

    public String getUserAgent() {
        return request.getUserAgent();
    }

    public String getLocale() {
        return request.getLocale();
    }

    public String getReferrer() {
        return request.getReferrer();
    }

    @Override
    public String toString() {
        return request.toString();
    }

}
