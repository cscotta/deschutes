package com.cscotta.deschutes.example.requests;

import com.cscotta.deschutes.api.EventWrapper;

public class RequestEventWrapper implements EventWrapper<Requests.Request, RequestEvent> {

    @Override
    public RequestEvent wrap(Requests.Request request) {
        return new RequestEvent(request);
    }

}
