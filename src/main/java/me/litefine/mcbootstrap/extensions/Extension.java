package me.litefine.mcbootstrap.extensions;

import me.litefine.mcbootstrap.objects.booting.BootingApplication;
import me.litefine.mcbootstrap.objects.booting.BootingObject;
import me.litefine.mcbootstrap.objects.booting.BootingServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Extension {

    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();
    protected String name, author, version;

    public void onSystemStartup() {}

    public void onSystemStartupFinished() {}

    public void onSystemShutdown() {}

    public void onSystemFinalizeShutdown() {}

    public void onBootingObjectAfterLoad(BootingObject object) {}

    public void onServerStartup(BootingServer server) {}

    public void onServerShutdown(BootingServer server) {}

    public void onApplicationStartup(BootingApplication application) {}

    public void onApplicationShutdown(BootingApplication application) {}

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public ExecutorService executor() {
        return executorService;
    }

}