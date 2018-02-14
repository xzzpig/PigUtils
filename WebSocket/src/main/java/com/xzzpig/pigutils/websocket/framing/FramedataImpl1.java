package com.xzzpig.pigutils.websocket.framing;

import com.xzzpig.pigutils.websocket.exceptions.InvalidDataException;
import com.xzzpig.pigutils.websocket.exceptions.InvalidFrameException;
import com.xzzpig.pigutils.websocket.util.Charsetfunctions;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class FramedataImpl1 implements FrameBuilder {
    protected static byte[] emptyarray = {};
    protected boolean fin;
    protected Opcode optcode;
    protected boolean transferemasked;
    private ByteBuffer unmaskedpayload;

    public FramedataImpl1() {
    }

    /**
     * Helper constructor which helps to create "echo" frames. The new object
     * will use the same underlying payload data.
     **/
    public FramedataImpl1(Framedata f) {
        fin = f.isFin();
        optcode = f.getOpcode();
        unmaskedpayload = f.getPayloadData();
        transferemasked = f.getTransfereMasked();
    }

    public FramedataImpl1(Opcode op) {
        this.optcode = op;
        unmaskedpayload = ByteBuffer.wrap(emptyarray);
    }

    @Override
    public void append(Framedata nextframe) throws InvalidFrameException {
        ByteBuffer b = nextframe.getPayloadData();
        if (unmaskedpayload == null) {
            unmaskedpayload = ByteBuffer.allocate(b.remaining());
            b.mark();
            unmaskedpayload.put(b);
            b.reset();
        } else {
            b.mark();
            unmaskedpayload.position(unmaskedpayload.limit());
            unmaskedpayload.limit(unmaskedpayload.capacity());

            if (b.remaining() > unmaskedpayload.remaining()) {
                ByteBuffer tmp = ByteBuffer.allocate(b.remaining() + unmaskedpayload.capacity());
                unmaskedpayload.flip();
                tmp.put(unmaskedpayload);
                tmp.put(b);
                unmaskedpayload = tmp;

            } else {
                unmaskedpayload.put(b);
            }
            unmaskedpayload.rewind();
            b.reset();
        }
        fin = nextframe.isFin();
    }

    @Override
    public Opcode getOpcode() {
        return optcode;
    }

    @Override
    public ByteBuffer getPayloadData() {
        return unmaskedpayload;
    }

    @Override
    public boolean getTransfereMasked() {
        return transferemasked;
    }

    @Override
    public boolean isFin() {
        return fin;
    }

    @Override
    public void setFin(boolean fin) {
        this.fin = fin;
    }

    @Override
    public void setOptcode(Opcode optcode) {
        this.optcode = optcode;
    }

    @Override
    public void setPayload(ByteBuffer payload) throws InvalidDataException {
        unmaskedpayload = payload;
    }

    @Override
    public void setTransferemasked(boolean transferemasked) {
        this.transferemasked = transferemasked;
    }

    @Override
    public String toString() {
        return "Framedata{ optcode:" + getOpcode() + ", fin:" + isFin() + ", payloadlength:[pos:"
                + unmaskedpayload.position() + ", len:" + unmaskedpayload.remaining() + "], payload:"
                + Arrays.toString(Charsetfunctions.utf8Bytes(new String(unmaskedpayload.array()))) + "}";
    }

}
