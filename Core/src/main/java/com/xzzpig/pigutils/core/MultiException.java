package com.xzzpig.pigutils.core;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiException extends Exception {
    @NotNull
    public List<Exception> exceptions = new ArrayList<>();
    private String message = "null";

    public MultiException() {}

    public MultiException(@Nullable Exception... es) {
        add(es);
    }

    public MultiException(@Nullable List<Exception> es) {
        add(es);
    }

    @Override public String getMessage() {
        return message;
    }

    public MultiException setMessage(@Nullable String msg) {
        if (msg != null)
            this.message = msg;
        return this;
    }

    public MultiException add(@Nullable Exception... es) {
        if (es != null)
            Collections.addAll(exceptions, es);
        return this;
    }

    public MultiException add(@Nullable List<Exception> es) {
        if (es != null)
            exceptions.addAll(es);
        return this;
    }
}
