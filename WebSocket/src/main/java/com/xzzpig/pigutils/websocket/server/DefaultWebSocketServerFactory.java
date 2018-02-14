package com.xzzpig.pigutils.websocket.server;

import com.xzzpig.pigutils.websocket.WebSocketAdapter;
import com.xzzpig.pigutils.websocket.WebSocketImpl;
import com.xzzpig.pigutils.websocket.drafts.Draft;
import com.xzzpig.pigutils.websocket.server.WebSocketServer.WebSocketServerFactory;

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class DefaultWebSocketServerFactory implements WebSocketServerFactory {
    @Override
    public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d, Socket s) {
        return new WebSocketImpl(a, d);
    }

    @Override
    public WebSocketImpl createWebSocket(WebSocketAdapter a, List<Draft> d, Socket s) {
        return new WebSocketImpl(a, d);
    }

    @Override
    public SocketChannel wrapChannel(SocketChannel channel, SelectionKey key) {
        return channel;
    }
}