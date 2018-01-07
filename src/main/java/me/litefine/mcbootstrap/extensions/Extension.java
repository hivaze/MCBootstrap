package me.litefine.mcbootstrap.extensions;

import me.litefine.mcbootstrap.objects.booting.BootingServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LITEFINE IDEA on 29.12.2017.
 */
public class Extension {

    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();
    protected String name, author, version;

    public void onSystemStartup() {}

    public void onSystemStartupFinished() {}

    public void onSystemShutdown() {}

    public void onSystemFinalizeShutdown() {}

    public void onServerStartup(BootingServer object) {}

    public void onServerShutdown(BootingServer object) {}

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}