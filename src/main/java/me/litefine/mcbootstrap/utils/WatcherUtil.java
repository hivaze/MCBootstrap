package me.litefine.mcbootstrap.utils;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.objects.booting.BootingServer;

import java.io.File;
import java.nio.file.*;

/**
 * Created by LITEFINE IDEA on 05.12.17.
 */
public class WatcherUtil {

    private static WatchService watchService;

    static {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Settings.getScreensFolder().toPath();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        } catch (Exception e) {
            MCBootstrap.getLogger().error("Can't register screens directory watcher", e);
            System.exit(105);
        }
    }

    public static void startWatcherThread() {
        new Thread(() -> {
            WatchKey key;
            try {
                while ((key = watchService.take()) != null) {
                    for (WatchEvent event : key.pollEvents()) {
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        String[] nameSplit = pathEvent.context().getFileName().toString().split("\\.");
                        int uniqueID = Integer.parseInt(nameSplit[0]);
                        BootingServer server = Settings.getServerByScreenName(nameSplit[1]);
                        if (pathEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            if (server != null && !server.isBooted()) {
                                server.setScreenID(uniqueID);
                                MCBootstrap.getLogger().info("Screen '" + pathEvent.context().getFileName() + "' for server '" + server.getName() + "' created.");
                            }
                        }
                        if (pathEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            if (server != null && server.isBooted()) {
                                server.setScreenID(-1);
                                MCBootstrap.getLogger().info("Screen '" + pathEvent.context().getFileName() + "' for server '" + server.getName() + "' deleted.");
                            }
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                MCBootstrap.getLogger().error("An error in screens directory watcher", e.getMessage());
            }
        }, "Watcher Thread").start();
    }

    public static void determineLaunchedObjects() {
        for (File screenFile : Settings.getScreensFolder().listFiles()) {
            String[] nameSplit = screenFile.getName().split("\\.");
            BootingServer server = Settings.getServerByScreenName(nameSplit[1]);
            if (server != null) server.setScreenID(Integer.parseInt(nameSplit[0]));
        }
        if (!Settings.getRunningServers().isEmpty())
            MCBootstrap.getLogger().info(Settings.getRunningServers().size() + " already launched servers found.");
    }

}