package com.xzzpig.pigutils.pigcommandservice.client;

import com.xzzpig.pigutils.json.JSONObject;
import com.xzzpig.pigutils.pigcommandservice.Command;

public abstract class ClientCommand extends Command {
    public CommandClient getClient(JSONObject args) {
        return (CommandClient) args.opt("client");
    }
}
