package com.cscotta.deschutes.example.requests;

import com.cscotta.deschutes.api.ConcurrentRollup;
import com.cscotta.deschutes.api.RollupFactory;
import com.cscotta.deschutes.example.requests.RequestRollup;
import com.cscotta.deschutes.example.requests.RequestEvent;

public class RequestRollups {

    public static final RollupFactory<RequestEvent> collapser = new RollupFactory<RequestEvent>() {
        @Override
        public ConcurrentRollup<RequestEvent> buildRollup(long timestamp) {
            return new RequestRollup(timestamp);
        }
    };

}

