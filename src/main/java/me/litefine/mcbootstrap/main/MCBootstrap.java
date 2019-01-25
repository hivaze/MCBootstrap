package me.litefine.mcbootstrap.main;

import me.litefine.mcbootstrap.console.ConsoleManager;
import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.utils.WatcherUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        WatcherUtil.determineLaunchedApplications();
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(extension::onSystemStartup));
        if (Settings.bootAllOnStart()) BootingAPI.startAllObjects();
        logger.debug("Local screen utility folder: " + Settings.getScreensFolder().getAbsolutePath());
        logger.info("MCBootstrap started in " + (System.currentTimeMillis() - time) + " ms.");
        ConsoleManager.getConsoleThread().start();
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(extension::onSystemStartupFinished));
    }

    public static void shutdown(boolean stopServers) {
        logger.info("Shutdown...");
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(extension::onSystemShutdown));
        if (stopServers) BootingAPI.stopAllObjects();
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(extension::onSystemFinalizeShutdown));
        ExtensionsManager.disableExtensions();
        System.exit(0);
    }

    public static Logger getLogger() {
        return logger;
    }

}