package com.cscotta.deschutes.example.requests;

import com.cscotta.deschutes.api.ConcurrentRollup;
import com.cscotta.deschutes.api.OutputListener;
import com.cscotta.deschutes.example.app.websockets.OLAPWebSocket;
import com.cscotta.deschutes.json.Json;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebsocketRollupListener implements OutputListener<RequestEvent> {

    private final Set<OLAPWebSocket> users;
    private final ObjectMapper jackson = new Json().objectMapper;
    private static final Logger log = LoggerFactory.getLogger(WebsocketRollupListener.class);

    public WebsocketRollupListener(Set<OLAPWebSocket> users) {
        this.users = users;
    }

    @Override
    public void update(List<Long> remove, Map<Long, Map<String, ConcurrentRollup<RequestEvent>>> insert) {

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("remove", remove);
        map.put("insert", insert);

        try {
            String output = jackson.writeValueAsString(map);
            for (OLAPWebSocket user : users)
                user.connection.sendMessage(output);
        } catch (IOException e) {
            log.error("Error sending updates to websocket.");
        }
    }
}
