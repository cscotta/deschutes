package com.cscotta.deschutes.example.app.websockets;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebsocketApi extends WebSocketServlet {
    public final Set<OLAPWebSocket> users = Collections.synchronizedSet(new HashSet<OLAPWebSocket>());

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new OLAPWebSocket(users);
    }
}
