package com.xzzpig.pigutils.pigcommandservice.server;

import com.xzzpig.pigutils.json.JSONObject;
import com.xzzpig.pigutils.pigcommandservice.Command;
import com.xzzpig.pigutils.websocket.WebSocket;

public abstract class ServerCommand extends Command {
    public WebSocket getClient(JSONObject msg) {
        return (WebSocket) msg.opt("client");
    }

    public CommandServer getServer() {
        return CommandServer.getServerInstance();
    }
}
