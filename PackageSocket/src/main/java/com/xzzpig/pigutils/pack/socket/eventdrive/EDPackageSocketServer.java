package com.xzzpig.pigutils.pack.socket.eventdrive;

import com.xzzpig.pigutils.event.EventAdapter;
import com.xzzpig.pigutils.event.EventBus;
import com.xzzpig.pigutils.pack.Package;
import com.xzzpig.pigutils.pack.socket.PackageSocket;
import com.xzzpig.pigutils.pack.socket.PackageSocketServer;

public class EDPackageSocketServer extends PackageSocketServer implements EventAdapter {

    private EventBus bus = new EventBus();

    public EDPackageSocketServer(int port) {
        super(port);
    }

    @Override
    public EventBus getEventBus() {
        return bus;
    }

    @Override
    public void onClose(PackageSocket socket) {
        bus.callEvent(new PackageSocketCloseEvent(socket));
    }

    @Override
    public void onError(PackageSocket socket, Exception exception) {
        bus.callEvent(new PackageSocketErrorEvent(socket, exception));
    }

    @Override
    public void onOpen(PackageSocket socket) {
        bus.callEvent(new PackageSocketOpenEvent(socket));
    }

    @Override
    public void onPackage(PackageSocket socket, Package pack) {
        bus.callEvent(new PackageSocketPackageEvent(socket, pack));
    }

    @Override
    public boolean onGetPackageError(PackageSocket socket, Exception exception) {
        PackageSocketGetPackageErrorEvent event = new PackageSocketGetPackageErrorEvent(socket, exception);
        bus.callEvent(event);
        return event.disconnect;
    }
}
