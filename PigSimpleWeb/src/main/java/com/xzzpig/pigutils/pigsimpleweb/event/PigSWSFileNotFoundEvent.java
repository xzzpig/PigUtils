package com.xzzpig.pigutils.pigsimpleweb.event;

import com.xzzpig.pigutils.pigsimpleweb.PigSimpleWebServer;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

/*
 * 当GET的文件不存在时触发
 * 用于返回404的Response
 */
public class PigSWSFileNotFoundEvent extends PigSWSEvent {

    Response r;
    IHTTPSession session;

    public PigSWSFileNotFoundEvent(PigSimpleWebServer psws, IHTTPSession session) {
        super(psws);
        this.session = session;
    }

    Response getResponse() {
        return r;
    }

    public void setResponse(Response response) {
        r = response;
    }

    public IHTTPSession getSession() {
        return this.session;
    }
}
