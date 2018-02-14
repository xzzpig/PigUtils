package com.xzzpig.pigutils.pack.socket;

import com.xzzpig.pigutils.pack.Package;

import java.io.IOException;
import java.net.Socket;

public class PackageSocket {

    protected Socket socket;

    protected PackageSocket() {
    }

    public PackageSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized PackageSocket send(Package pack) {
        synchronized (socket) {
            try {
                pack.write(socket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public synchronized PackageSocket send(Package pack, long speed) {
        synchronized (socket) {
            try {
                pack.write(socket.getOutputStream(), speed);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }
}
