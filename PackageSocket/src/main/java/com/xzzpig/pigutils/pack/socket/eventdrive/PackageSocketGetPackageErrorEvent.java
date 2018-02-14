package com.xzzpig.pigutils.pack.socket.eventdrive;

import com.xzzpig.pigutils.pack.socket.PackageSocket;

public class PackageSocketGetPackageErrorEvent extends PackageSocketEvent {

    boolean disconnect;
    private Exception error;

    public PackageSocketGetPackageErrorEvent(PackageSocket socket, Exception error) {
        super(socket);
        this.error = error;
    }

    public Exception getError() {
        return error;
    }

    public PackageSocketGetPackageErrorEvent setDisconnect(boolean b) {
        this.disconnect = b;
        return this;
    }
}
