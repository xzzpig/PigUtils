package com.xzzpig.pigutils.pack.socket;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.pack.Package;
import com.xzzpig.pigutils.reflect.ClassUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PackageSocketClient extends PackageSocket implements Runnable {

    private final String ip;
    private final int port;
    /**
     * 允许的最大连续错误数<br/>
     * 当接收Package时连续报错数目超过maxErrorCount时自动与服务端断开连接
     */
    public int maxErrorCount = 10;
    private AtomicBoolean started;
    private Thread thread;

    public PackageSocketClient(@NotNull String ip, int port) {
        ClassUtils.checkThisConstructorArgs(ip, port);
        this.ip = ip;
        this.port = port;
        this.started = new AtomicBoolean(false);
    }

    public synchronized boolean isStarted() {
        return started.get();
    }

    public abstract void onClose();

    public abstract void onError(Exception exception);

    public abstract void onOpen();

    public abstract void onPackage(Package pack);

    @Override
    public void run() {
        try {
            this.socket = new Socket(ip, port);
            started.set(true);
        } catch (IOException e) {
            onError(e);
            return;
        }
        onOpen();
        int errorCounter = 0;
        while (!thread.isInterrupted() && !getSocket().isClosed()) {
            try {
                Package pack = Package.read(getSocket().getInputStream());
                onPackage(pack);
                errorCounter = 0;
            } catch (IOException e) {
                if (e.getMessage().contains("Connection reset"))
                    break;
                onError(e);
                errorCounter++;
                if (errorCounter > maxErrorCount)
                    break;
            } catch (NegativeArraySizeException e) {
                break;
            }
        }
        onClose();
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
    }
}
