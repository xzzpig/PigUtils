package com.xzzpig.pigutils.core;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class YieldIterator<E> implements Iterator<E>, Iterable<E> {

    AtomicBoolean finished;
    private YieldThread thread;

    public YieldIterator(Runnable runnable) {
        finished = new AtomicBoolean(false);
        thread = new YieldThread(()->{
            runnable.run();
            finished.set(true);
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void yield(Object obj) {
        YieldThread yieldThread = (YieldThread) Thread.currentThread();
        yieldThread.value = obj;
        yieldThread.valueSated.set(true);
        synchronized (yieldThread.valueSated) {
            yieldThread.valueSated.notifyAll();
        }
        synchronized (yieldThread) {
            try {
                yieldThread.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override public Iterator<E> iterator() {
        return this;
    }

    @Override public boolean hasNext() {
        return !finished.get();
    }

    @Override public E next() {
        while (!thread.valueSated.get())
            synchronized (thread.valueSated) {
                try {
                    thread.valueSated.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        Object value = thread.value;
        synchronized (thread) {
            thread.notifyAll();
        }
        return (E) value;
    }

    static class YieldThread extends Thread {
        AtomicBoolean valueSated;
        Object value;

        YieldThread(Runnable runnable) {
            super(runnable);
            valueSated = new AtomicBoolean(false);
        }
    }
}
