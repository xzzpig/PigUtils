package com.xzzpig.pigutils.pigsimpleweb.event;

import com.xzzpig.pigutils.pigsimpleweb.MIME;
import com.xzzpig.pigutils.pigsimpleweb.PigSimpleWebServer;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;


public class PigSWSSolveMIMEEvent extends PigSWSEvent {

    private MIME mime;
    private Response response;
    private IHTTPSession session;

    public PigSWSSolveMIMEEvent(PigSimpleWebServer psws, IHTTPSession session, MIME mime) {
        super(psws);
        this.session = session;
        this.mime = mime;
    }

    public MIME getMIME() {
        return mime;
    }

    Response getResponse() {
        return response;
    }

    public void setResponse(Response r) {
        response = r;
    }

    public IHTTPSession getSession() {
        return session;
    }

}
