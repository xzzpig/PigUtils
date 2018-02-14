package com.xzzpig.pigutils.task;

import com.xzzpig.pigutils.task.base.BaseTaskStreamManager;

import java.util.HashMap;
import java.util.Map;

public abstract class TaskStreamManager {

    public static String defaultTaskStreamManagerName = "base";
    private static Map<String, TaskStreamManager> map;

    static {
        register("base", new BaseTaskStreamManager());
    }

    protected TaskStreamManager() {
    }

    public static void register(String name, TaskStreamManager manager) {
        if (map == null)
            map = new HashMap<>();
        map.put(name, manager);
    }

    public static TaskStreamManager getTaskStreamManager(String name) {
        if (map == null)
            return null;
        return map.get(name);
    }

    public static TaskStreamManager getTaskStreamManager() {
        return getTaskStreamManager(defaultTaskStreamManagerName);
    }

    public abstract TaskStreamBuilder getBuilder(String name);

    public abstract TaskStreamBuilder getBuilder();
}
