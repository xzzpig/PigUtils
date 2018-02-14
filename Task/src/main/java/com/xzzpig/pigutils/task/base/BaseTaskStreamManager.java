package com.xzzpig.pigutils.task.base;

import com.xzzpig.pigutils.task.TaskStreamBuilder;
import com.xzzpig.pigutils.task.TaskStreamManager;

public class BaseTaskStreamManager extends TaskStreamManager {

    public BaseTaskStreamManager() {
    }

    @Override
    public TaskStreamBuilder getBuilder(String name) {
        if (name.equalsIgnoreCase("base"))
            return new BaseTaskStreamBuilder();
        return null;
    }

    @Override
    public TaskStreamBuilder getBuilder() {
        return getBuilder("base");
    }

}
