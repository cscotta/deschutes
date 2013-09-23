package com.cscotta.deschutes.json;

import java.util.List;
import java.util.Map;

import com.cscotta.deschutes.example.requests.RequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscotta.deschutes.api.ConcurrentRollup;
import com.cscotta.deschutes.api.OutputListener;
import com.cscotta.deschutes.json.Json;
import org.codehaus.jackson.map.ObjectMapper;

public class LoggedRollupListener implements OutputListener<RequestEvent> {

    private final ObjectMapper jackson = new Json().objectMapper;
    private static final Logger log = LoggerFactory.getLogger(LoggedRollupListener.class);

    @Override
    public void update(List<Long> remove, Map<Long, Map<String, ConcurrentRollup<RequestEvent>>> insert) {
        try {
            log.info("Remove: " + jackson.writeValueAsString(remove));
            log.info("Insert: " + jackson.writeValueAsString(insert));
        } catch (Exception e) {
            log.error("Error processing update.", e);
        }
    }
}
