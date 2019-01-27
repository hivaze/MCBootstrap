package me.litefine.mcbootstrap.objects.booting;

import java.util.List;
import java.util.function.Consumer;

public interface ParenthoodObject {

    Consumer<BootingServer> childStopper = bootingServer -> {
        synchronized (bootingServer) {
            if (bootingServer.isBooted()) {
                bootingServer.stopObject();
                try { bootingServer.wait(); } catch (InterruptedException ignore) {}
            }
        }
    };

    List<BootingServer> getChildServers();

}