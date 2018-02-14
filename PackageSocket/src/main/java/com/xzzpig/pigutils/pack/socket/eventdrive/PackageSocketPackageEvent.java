package com.xzzpig.pigutils.pack.socket.eventdrive;

import com.xzzpig.pigutils.pack.Package;
import com.xzzpig.pigutils.pack.socket.PackageSocket;

public class PackageSocketPackageEvent extends PackageSocketEvent {

    private Package pack;

    public PackageSocketPackageEvent(PackageSocket socket, Package pack) {
        super(socket);
        this.pack = pack;
    }

    public Package getPackage() {
        return pack;
    }
}
