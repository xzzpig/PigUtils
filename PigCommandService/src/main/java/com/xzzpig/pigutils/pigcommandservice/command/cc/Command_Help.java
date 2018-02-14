package com.xzzpig.pigutils.pigcommandservice.command.cc;

import com.xzzpig.pigutils.json.JSONObject;
import com.xzzpig.pigutils.pigcommandservice.Command;
import com.xzzpig.pigutils.pigcommandservice.CommandRunner;
import com.xzzpig.pigutils.pigcommandservice.client.ClientCommand;

import java.util.logging.Logger;

public class Command_Help extends ClientCommand {

    public Command_Help() {
    }

    @Override
    public String getCmd() {
        return "help";
    }

    @Override
    public CommandRunner getCommandRunner() {
        return this::run;
    }

    @Override
    public String getDescribe() {
        return "列出所有命令";
    }

    @Override
    public CommandTarget getType() {
        return CommandTarget.Client;
    }

    public JSONObject run(String cmd, JSONObject args) {
        StringBuffer sb = new StringBuffer("命令列表:\n");
        Command.getCommands().filter(ClientCommand.class::isInstance)
                .filter(c->c.getType().toString().equalsIgnoreCase("Client")).forEach(c->sb.append('\t').append(c.toString()).append('\n'));
        Logger.getAnonymousLogger().info(sb.toString());
        return null;
    }

}
