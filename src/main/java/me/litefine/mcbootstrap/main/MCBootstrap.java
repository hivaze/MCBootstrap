package me.litefine.mcbootstrap.main;

import me.litefine.mcbootstrap.console.ConsoleManager;
import me.litefine.mcbootstrap.objects.UniqueFilesPolicy;
import me.litefine.mcbootstrap.objects.booting.BootingObject;
import me.litefine.mcbootstrap.utils.WatcherUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by LITEFINE IDEA on 26.11.17.
 */
public class MCBootstrap {

    static {
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
        } catch (Exception ex) {
            logger.error("An unexpected error occurred during startup", ex);
            System.exit(103);
        }
        WatcherUtil.determineLaunchedObjects();
        if (Settings.bootAllOnStart()) MCBootstrap.startAllObjects();
        logger.debug("Local screen utility folder: " + Settings.getScreensFolder().getAbsolutePath());
        logger.info("MCBootstrap started in " + (System.currentTimeMillis() - time) + " ms.");
        ConsoleManager.getConsoleThread().start();
    }

    public static void shutdown(boolean stopServers) {
        logger.info("Shutdown...");
        if (stopServers) MCBootstrap.stopAllObjects();
        System.exit(0);
    }

    public static void startAllObjects() {
        if (Settings.getBootingObjects().isEmpty()) MCBootstrap.getLogger().warn("No booting objects to run!");
        else Settings.getBootingObjects().forEach(bootingObject -> {
            if (!bootingObject.isBooted()) {
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
        if (Settings.getRunningObjects().isEmpty()) MCBootstrap.getLogger().warn("No running objects to stop!");
        else {
            if (Settings.reverseOrderOnStop()) {
                List<BootingObject> reversedCopy = new ArrayList<>(Settings.getRunningObjects());
                reversedCopy.sort(Comparator.comparingInt(object -> object.getPriority().getPoints()));
                reversedCopy.forEach(BootingObject::stopObject);
            } else Settings.getRunningObjects().forEach(BootingObject::stopObject);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public static UniqueFilesPolicy getUniqueFilesPolicy(String name) {
        if (name.equalsIgnoreCase("inorder")) return Settings.INORDER_POLICY;
        else if (name.equalsIgnoreCase("random")) return Settings.RANDOM_POLICY;
        else if (name.equalsIgnoreCase("custom")) return Settings.CUSTOM_POLICY;
        else return null;
    }

}