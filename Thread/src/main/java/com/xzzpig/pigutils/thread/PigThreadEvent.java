package com.xzzpig.pigutils.thread;

import com.xzzpig.pigutils.event.Event;

@Deprecated
public abstract class PigThreadEvent extends Event {
    private PigThread t;

    public PigThreadEvent(PigThread t) {
        this.t = t;
    }

    public PigThread getPigThread() {
        return t;
    }
}
