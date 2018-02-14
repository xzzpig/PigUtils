package com.xzzpig.pigutils.websocket.framing;

import com.xzzpig.pigutils.websocket.exceptions.InvalidDataException;

import java.nio.ByteBuffer;

public interface FrameBuilder extends Framedata {

    void setFin(boolean fin);

    void setOptcode(Opcode optcode);

    void setPayload(ByteBuffer payload) throws InvalidDataException;

    void setTransferemasked(boolean transferemasked);

}