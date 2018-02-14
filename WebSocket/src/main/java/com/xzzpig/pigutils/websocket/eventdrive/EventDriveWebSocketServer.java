package com.xzzpig.pigutils.websocket.eventdrive;

import com.xzzpig.pigutils.event.EventAdapter;
import com.xzzpig.pigutils.event.EventBus;
import com.xzzpig.pigutils.websocket.WebSocket;
import com.xzzpig.pigutils.websocket.drafts.Draft;
import com.xzzpig.pigutils.websocket.event.WebSocketCloseEvent;
import com.xzzpig.pigutils.websocket.event.WebSocketErrorEvent;
import com.xzzpig.pigutils.websocket.event.WebSocketMessageEvent;
import com.xzzpig.pigutils.websocket.event.WebSocketOpenEvent;
import com.xzzpig.pigutils.websocket.handshake.ClientHandshake;
import com.xzzpig.pigutils.websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;

public class EventDriveWebSocketServer extends WebSocketServer implements EventAdapter {

    EventBus eventbus = new EventBus();

    public EventDriveWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    public EventDriveWebSocketServer(InetSocketAddress address, int decoders) {
        super(address, decoders);
    }

    public EventDriveWebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts) {
        super(address, decodercount, drafts);
    }

    public EventDriveWebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts,
                                     Collection<WebSocket> connectionscontainer) {
        super(address, decodercount, drafts, connectionscontainer);
    }

    public EventDriveWebSocketServer(InetSocketAddress address, List<Draft> drafts) {
        super(address, drafts);
    }

    @Override
    public EventBus getEventBus() {
        return eventbus;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        eventbus.callEvent(new WebSocketCloseEvent(conn, code, reason, remote));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        eventbus.callEvent(new WebSocketErrorEvent(conn, ex));
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        eventbus.callEvent(new WebSocketMessageEvent(conn, message));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        eventbus.callEvent(new WebSocketOpenEvent(conn, handshake));
    }

}
