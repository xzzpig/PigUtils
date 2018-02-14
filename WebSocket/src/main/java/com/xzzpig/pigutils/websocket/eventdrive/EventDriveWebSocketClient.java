package com.xzzpig.pigutils.websocket.eventdrive;

import com.xzzpig.pigutils.event.EventAdapter;
import com.xzzpig.pigutils.event.EventBus;
import com.xzzpig.pigutils.websocket.client.WebSocketClient;
import com.xzzpig.pigutils.websocket.drafts.Draft;
import com.xzzpig.pigutils.websocket.event.WebSocketCloseEvent;
import com.xzzpig.pigutils.websocket.event.WebSocketErrorEvent;
import com.xzzpig.pigutils.websocket.event.WebSocketMessageEvent;
import com.xzzpig.pigutils.websocket.event.WebSocketOpenEvent;
import com.xzzpig.pigutils.websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class EventDriveWebSocketClient extends WebSocketClient implements EventAdapter {

    EventBus eventbus = new EventBus();

    public EventDriveWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    public EventDriveWebSocketClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public EventDriveWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders,
                                     int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    @Override
    public EventBus getEventBus() {
        return eventbus;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        eventbus.callEvent(new WebSocketCloseEvent(this, code, reason, remote));
    }

    @Override
    public void onError(Exception ex) {
        eventbus.callEvent(new WebSocketErrorEvent(this, ex));
    }

    @Override
    public void onMessage(String message) {
        eventbus.callEvent(new WebSocketMessageEvent(this, message));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        eventbus.callEvent(new WebSocketOpenEvent(this, handshakedata));
    }

}
