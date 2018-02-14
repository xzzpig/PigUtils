package com.xzzpig.pigutils.task;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;
import com.xzzpig.pigutils.core.MapData;

import java.util.HashMap;
import java.util.Map;

public class TaskObject extends MapData {

    TaskState state;

    public TaskObject() {
        this(null);
    }

    public TaskObject(@Nullable Map<String, Object> data) {
        super(data == null ? new HashMap<>() : data);
    }

    public @NotNull TaskState getState() {
        return state != null ? state : TaskState.DEFAULT;
    }

    public @NotNull TaskObject setState(@Nullable TaskState state) {
        this.state = state;
        return this;
    }

    public Object getInput() {
        return get("input");
    }

    public <T> T getInput(Class<T> clazz) {
        return get("input", clazz);
    }
}
