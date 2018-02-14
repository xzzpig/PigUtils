package com.xzzpig.pigutils.websocket.drafts;

import com.xzzpig.pigutils.websocket.WebSocket.Role;
import com.xzzpig.pigutils.websocket.exceptions.IncompleteHandshakeException;
import com.xzzpig.pigutils.websocket.exceptions.InvalidDataException;
import com.xzzpig.pigutils.websocket.exceptions.InvalidHandshakeException;
import com.xzzpig.pigutils.websocket.exceptions.LimitExedeedException;
import com.xzzpig.pigutils.websocket.framing.CloseFrame;
import com.xzzpig.pigutils.websocket.framing.FrameBuilder;
import com.xzzpig.pigutils.websocket.framing.Framedata;
import com.xzzpig.pigutils.websocket.framing.Framedata.Opcode;
import com.xzzpig.pigutils.websocket.framing.FramedataImpl1;
import com.xzzpig.pigutils.websocket.handshake.*;
import com.xzzpig.pigutils.websocket.util.Charsetfunctions;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Base class for everything of a websocket specification which is not common
 * such as the way the handshake is read or frames are transfered.
 **/
public abstract class Draft {

    public static final byte[] FLASH_POLICY_REQUEST = Charsetfunctions.utf8Bytes("<policy-file-request/>\0");
    public static int MAX_FAME_SIZE = 1000;
    public static int INITIAL_FAMESIZE = 64;
    /**
     * In some cases the handshake will be parsed different depending on whether
     */
    protected Role role = null;
    protected Opcode continuousFrameType = null;

    public static ByteBuffer readLine(ByteBuffer buf) {
        ByteBuffer sbuf = ByteBuffer.allocate(buf.remaining());
        byte prev = '0';
        byte cur = '0';
        while (buf.hasRemaining()) {
            prev = cur;
            cur = buf.get();
            sbuf.put(cur);
            if (prev == (byte) '\r' && cur == (byte) '\n') {
                sbuf.limit(sbuf.position() - 2);
                sbuf.position(0);
                return sbuf;

            }
        }
        // ensure that there wont be any bytes skipped
        buf.position(buf.position() - sbuf.position());
        return null;
    }

    public static String readStringLine(ByteBuffer buf) {
        ByteBuffer b = readLine(buf);
        return b == null ? null : Charsetfunctions.stringAscii(b.array(), 0, b.limit());
    }

    public static HandshakeBuilder translateHandshakeHttp(ByteBuffer buf, Role role)
            throws InvalidHandshakeException, IncompleteHandshakeException {
        HandshakeBuilder handshake;

        String line = readStringLine(buf);
        if (line == null)
            throw new IncompleteHandshakeException(buf.capacity() + 128);

        String[] firstLineTokens = line.split(" ", 3);// eg. HTTP/1.1 101
        // Switching the
        // Protocols
        if (firstLineTokens.length != 3) {
            throw new InvalidHandshakeException();
        }

        if (role == Role.CLIENT) {
            // translating/parsing the response from the SERVER
            handshake = new HandshakeImpl1Server();
            ServerHandshakeBuilder serverhandshake = (ServerHandshakeBuilder) handshake;
            serverhandshake.setHttpStatus(Short.parseShort(firstLineTokens[1]));
            serverhandshake.setHttpStatusMessage(firstLineTokens[2]);
        } else {
            // translating/parsing the request from the CLIENT
            ClientHandshakeBuilder clienthandshake = new HandshakeImpl1Client();
            clienthandshake.setResourceDescriptor(firstLineTokens[1]);
            handshake = clienthandshake;
        }

        line = readStringLine(buf);
        while (line != null && line.length() > 0) {
            String[] pair = line.split(":", 2);
            if (pair.length != 2)
                throw new InvalidHandshakeException("not an http header");
            handshake.put(pair[0], pair[1].replaceFirst("^ +", ""));
            line = readStringLine(buf);
        }
        if (line == null)
            throw new IncompleteHandshakeException();
        return handshake;
    }

    public abstract HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response)
            throws InvalidHandshakeException;

    public abstract HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata)
            throws InvalidHandshakeException;

    protected boolean basicAccept(Handshakedata handshakedata) {
        return handshakedata.getFieldValue("Upgrade").equalsIgnoreCase("websocket")
                && handshakedata.getFieldValue("Connection").toLowerCase(Locale.ENGLISH).contains("upgrade");
    }

    public int checkAlloc(int bytecount) throws LimitExedeedException, InvalidDataException {
        if (bytecount < 0)
            throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR, "Negative count");
        return bytecount;
    }

    public List<Framedata> continuousFrame(Opcode op, ByteBuffer buffer, boolean fin) {
        if (op != Opcode.BINARY && op != Opcode.TEXT && op != Opcode.TEXT) {
            throw new IllegalArgumentException("Only Opcode.BINARY or  Opcode.TEXT are allowed");
        }

        if (continuousFrameType != null) {
            continuousFrameType = Opcode.CONTINUOUS;
        } else {
            continuousFrameType = op;
        }

        FrameBuilder bui = new FramedataImpl1(continuousFrameType);
        try {
            bui.setPayload(buffer);
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }
        bui.setFin(fin);
        if (fin) {
            continuousFrameType = null;
        } else {
            continuousFrameType = op;
        }
        return Collections.singletonList((Framedata) bui);
    }

    /**
     * Drafts must only be by one websocket at all. To prevent drafts to be used
     * more than once the Websocket implementation should call this method in
     * order to create a new usable version of a given draft instance.<br>
     * The copy can be safely used in conjunction with a new websocket
     * connection.
     */
    public abstract Draft copyInstance();

    public abstract ByteBuffer createBinaryFrame(Framedata framedata); // TODO

    public abstract List<Framedata> createFrames(ByteBuffer binary, boolean mask);

    public abstract List<Framedata> createFrames(String text, boolean mask);
    // Allow
    // to
    // send
    // data
    // on
    // the
    // base
    // of an
    // Iterator
    // or
    // InputStream

    public List<ByteBuffer> createHandshake(Handshakedata handshakedata, Role ownrole) {
        return createHandshake(handshakedata, ownrole, true);
    }

    public List<ByteBuffer> createHandshake(Handshakedata handshakedata, Role ownrole, boolean withcontent) {
        StringBuilder bui = new StringBuilder(100);
        if (handshakedata instanceof ClientHandshake) {
            bui.append("GET ");
            bui.append(((ClientHandshake) handshakedata).getResourceDescriptor());
            bui.append(" HTTP/1.1");
        } else if (handshakedata instanceof ServerHandshake) {
            bui.append("HTTP/1.1 101 ").append(((ServerHandshake) handshakedata).getHttpStatusMessage());
        } else {
            throw new RuntimeException("unknow role");
        }
        bui.append("\r\n");
        Iterator<String> it = handshakedata.iterateHttpFields();
        while (it.hasNext()) {
            String fieldname = it.next();
            String fieldvalue = handshakedata.getFieldValue(fieldname);
            bui.append(fieldname);
            bui.append(": ");
            bui.append(fieldvalue);
            bui.append("\r\n");
        }
        bui.append("\r\n");
        byte[] httpheader = Charsetfunctions.asciiBytes(bui.toString());

        byte[] content = withcontent ? handshakedata.getContent() : null;
        ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0 : content.length) + httpheader.length);
        bytebuffer.put(httpheader);
        if (content != null)
            bytebuffer.put(content);
        bytebuffer.flip();
        return Collections.singletonList(bytebuffer);
    }

    public abstract CloseHandshakeType getCloseHandshakeType();

    public Role getRole() {
        return role;
    }

    public abstract ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request)
            throws InvalidHandshakeException;

    public abstract HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request,
                                                                          ServerHandshakeBuilder response) throws InvalidHandshakeException;

    public abstract void reset();

    public void setParseMode(Role role) {
        this.role = role;
    }

    public abstract List<Framedata> translateFrame(ByteBuffer buffer) throws InvalidDataException;

    public Handshakedata translateHandshake(ByteBuffer buf) throws InvalidHandshakeException {
        return translateHandshakeHttp(buf, role);
    }

    public enum CloseHandshakeType {
        NONE, ONEWAY, TWOWAY
    }

    public enum HandshakeState {
        /** Handshake matched this Draft successfully */
        MATCHED,
        /** Handshake is does not match this Draft */
        NOT_MATCHED
    }

}
