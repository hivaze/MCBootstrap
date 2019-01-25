package me.litefine.mcbootstrap.utils;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.BootingAPI;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.nio.file.*;

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
                        BootingAPI.getApplicationByScreenName(nameSplit[1]).ifPresent(application -> {
                            if (pathEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                if (!application.isBooted()) {
                                    MCBootstrap.getLogger().info("Screen '" + pathEvent.context().getFileName() + "' for application '" + BasicUtils.colorize(application.getName(), Ansi.Color.YELLOW) + "' created.");
                                    application.setScreenID(uniqueID);
                                    ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onApplicationStartup(application)));
                                }
                            }
                            if (pathEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                if (application.isBooted()) {
                                    MCBootstrap.getLogger().info("Screen '" + pathEvent.context().getFileName() + "' for application '" + BasicUtils.colorize(application.getName(), Ansi.Color.YELLOW) + "' deleted.");
                                    application.setScreenID(-1);
                                    ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onApplicationShutdown(application)));
                                }
                            }
                        });
                        BootingAPI.getServerByScreenName(nameSplit[1], true).ifPresent(server -> {
                            if (pathEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                if (!server.isBooted()) {
                                    MCBootstrap.getLogger().info("Screen '" + pathEvent.context().getFileName() + "' for server '" + BasicUtils.colorize(server.getName(), Ansi.Color.YELLOW) + "' created.");
                                    server.setScreenID(uniqueID);
                                    ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onServerStartup(server)));
                                }
                            }
                            if (pathEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                if (server.isBooted()) {
                                    MCBootstrap.getLogger().info("Screen '" + pathEvent.context().getFileName() + "' for server '" + BasicUtils.colorize(server.getName(), Ansi.Color.YELLOW) + "' deleted.");
                                    server.setScreenID(-1);
                                    ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onServerShutdown(server)));
                                }
                            }
                        });
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                MCBootstrap.getLogger().error("An error in screens directory watcher - " + e.getMessage());
            }
        }, "Watcher Thread").start();
    }

    public static void determineLaunchedApplications() {
        final int[] counter = {0, 0};
        for (File screenFile : Settings.getScreensFolder().listFiles()) {
            String[] nameSplit = screenFile.getName().split("\\.");
            BootingAPI.getApplicationByScreenName(nameSplit[1]).ifPresent(application -> {
                application.setScreenID(Integer.parseInt(nameSplit[0]));
                counter[0]++;
            });
            BootingAPI.getServerByScreenName(nameSplit[1], true).ifPresent(server -> {
                server.setScreenID(Integer.parseInt(nameSplit[0]));
                counter[1]++;
            });
        }
        if (counter[0] > 0) MCBootstrap.getLogger().info(counter[0] + " already launched applications found.");
        if (counter[1] > 0) MCBootstrap.getLogger().info(counter[1] + " already launched servers found.");
    }

}