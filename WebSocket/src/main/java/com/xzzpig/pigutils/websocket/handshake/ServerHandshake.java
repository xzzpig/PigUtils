package com.xzzpig.pigutils.websocket.handshake;

public interface ServerHandshake extends Handshakedata {
    short getHttpStatus();

    String getHttpStatusMessage();
}
