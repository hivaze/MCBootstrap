package me.litefine.mcbootstrap.main;

import me.litefine.mcbootstrap.console.ConsoleManager;
import me.litefine.mcbootstrap.extensions.Extension;
import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.objects.booting.BootingServer;
import me.litefine.mcbootstrap.utils.WatcherUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by LITEFINE IDEA on 26.11.17.
 */
public class MCBootstrap {

    static {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(MCBootstrap.class.getResourceAsStream("/logo.txt")));
            while (reader.ready()) System.out.println(reader.readLine());
        } catch (IOException ignored) {}
        System.out.println("Loading libraries, please wait...");
    }

    private static final Logger logger = LogManager.getLogger(MCBootstrap.class);

    public static void main(String[] args) {
        logger.info("Startup initialization of the MCBootstrap's engine!");
        long time = System.currentTimeMillis();
        try {
            Settings.setupFiles();
            Settings.loadFromConfig();
            ConsoleManager.setup();
            ExtensionsManager.loadExtensions();
        } catch (Exception ex) {
            logger.error("An unexpected error occurred during startup", ex);
            System.exit(103);
        }
        WatcherUtil.determineLaunchedObjects();
        ExtensionsManager.getExtensions().forEach(Extension::onSystemStartup);
        if (Settings.bootAllOnStart()) MCBootstrap.startAllObjects();
        logger.debug("Local screen utility folder: " + Settings.getScreensFolder().getAbsolutePath());
        logger.info("MCBootstrap started in " + (System.currentTimeMillis() - time) + " ms.");
        ConsoleManager.getConsoleThread().start();
        ExtensionsManager.getExtensions().forEach(Extension::onSystemStartupFinished);
    }

    public static void shutdown(boolean stopServers) {
        logger.info("Shutdown...");
        ExtensionsManager.getExtensions().forEach(Extension::onSystemShutdown);
        if (stopServers) MCBootstrap.stopAllObjects();
        ExtensionsManager.getExtensions().forEach(Extension::onSystemFinalizeShutdown);
        ExtensionsManager.disableExtensions();
        System.exit(0);
    }

    public static void startAllObjects() {
        if (Settings.getBootingServers().size() == Settings.getRunningServers().size()) MCBootstrap.getLogger().warn("No booting objects to run!");
        else Settings.getBootingObjects().forEach(bootingObject -> {
            if (!bootingObject.isRunningServer()) {
                bootingObject.bootObject();
                if (Settings.getStartDelay() > 0 && Settings.getBootingObjects().indexOf(bootingObject) != Settings.getBootingObjects().size()-1) {
                    try {
                        MCBootstrap.getLogger().info("Waiting for " + Settings.getStartDelay() * 1000L + " ms (delay)...");
                        Thread.sleep(Settings.getStartDelay() * 1000L);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    public static void stopAllObjects() {
        if (Settings.getRunningServers().isEmpty()) MCBootstrap.getLogger().warn("No running objects to stop!");
        else {
            if (Settings.reverseOrderOnStop()) {
                List<BootingServer> reversedCopy = new ArrayList<>(Settings.getRunningServers());
                reversedCopy.sort(Comparator.comparingInt(object -> object.getPriority().getPoints()));
                reversedCopy.forEach(BootingServer::stopObject);
            } else Settings.getRunningServers().forEach(BootingServer::stopObject);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

}