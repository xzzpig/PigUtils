package com.xzzpig.pigutils.plugin.url;

import com.xzzpig.pigutils.plugin.base.BasePlugin;

public abstract class URLPlugin extends BasePlugin {

    void loadInfo(URLPluginInfo info) {
        this.depends = info.depends;
        this.info = info.infos;
        this.name = info.name;
    }
}
