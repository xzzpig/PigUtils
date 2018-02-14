package com.xzzpig.pigutils.websocket.event;

import com.xzzpig.pigutils.event.Event;
import com.xzzpig.pigutils.websocket.WebSocket;

public class WebSocketEvent extends Event {

    WebSocket webSocket;

    public WebSocketEvent(WebSocket ws) {
        webSocket = ws;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

}
