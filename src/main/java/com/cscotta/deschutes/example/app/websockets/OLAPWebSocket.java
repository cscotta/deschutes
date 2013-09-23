package com.cscotta.deschutes.example.app.websockets;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.websocket.WebSocket;

/**
 * Websocket connection handler.
 */
public class OLAPWebSocket implements WebSocket, WebSocket.OnTextMessage {

    private static final Logger log = LoggerFactory.getLogger(OLAPWebSocket.class);

    public Connection connection;
    private Set<OLAPWebSocket> users;

    public OLAPWebSocket(Set<OLAPWebSocket> users) {
        this.users = users;
    }

    @Override
    public void onOpen(Connection connection) {
        log.info("Connection opened: " + connection.toString());
        this.connection = connection;
        users.add(this);
    }

    @Override
    public void onClose(int closeCode, String message) {
        log.info("Connection closed: " + connection.toString());
        users.remove(this);
    }

    @Override
    public void onMessage(String data) {
        log.info("Message received from : " + connection.toString() + ": " + data);
    }
}
