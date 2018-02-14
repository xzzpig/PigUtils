package com.xzzpig.pigutils.pigsimpleweb.event;

import com.xzzpig.pigutils.pigsimpleweb.PigSimpleWebServer;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;


public class PigSWSServeEvent extends PigSWSEvent {

    private Response response;
    private IHTTPSession session;

    public PigSWSServeEvent(PigSimpleWebServer psws, IHTTPSession session) {
        super(psws);
        this.session = session;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public IHTTPSession getSession() {
        return session;
    }
}
