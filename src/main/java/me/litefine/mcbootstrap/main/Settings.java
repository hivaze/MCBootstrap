package me.litefine.mcbootstrap.main;

import me.litefine.mcbootstrap.objects.booting.BootingObject;
import me.litefine.mcbootstrap.utils.BasicUtils;
import me.litefine.mcbootstrap.utils.WatcherUtil;
import me.litefine.mcbootstrap.utils.YamlUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Settings {

    private static File dataFolder, configFile, extensionsFolder, runnedJar, screensFolder;

    private static long startDelay;
    private static String screenNamePattern;
    private static boolean bootAllOnStart;
    private static boolean reverseOrderOnStop;

    static {
        try {
            Process request = Runtime.getRuntime().exec("screen -ls");
            request.waitFor();
            String folder = new Scanner(request.getInputStream()).findWithinHorizon("\\s\\/(.*?)\\/(.*)\\b", Integer.MAX_VALUE).trim();
            screensFolder = new File(folder);
            WatcherUtil.startWatcherThread();
        } catch (IOException | InterruptedException e) {
            MCBootstrap.getLogger().error("Error in the determination of the screen utility location", e);
            MCBootstrap.getLogger().error("Solve this problem and try it again!");
            System.exit(104);
        }
        runnedJar = new File(MCBootstrap.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        dataFolder = new File(runnedJar.getAbsolutePath().replace(runnedJar.getName(), "") + "/MCBootstrap/");
        configFile = new File(dataFolder, "config.yml");
        extensionsFolder = new File(dataFolder, "extensions");
    }

    public static void loadFromConfig() throws Exception {
        Map mapRepresentation = YamlUtils.readObject(HashMap.class);
        if (YamlUtils.getBooleanValue("Settings.enableDebug", mapRepresentation)) {
            Configurator.setLevel(MCBootstrap.class.getCanonicalName(), Level.DEBUG);
            MCBootstrap.getLogger().debug("Logger debug mode enabled!");
        }
        bootAllOnStart = YamlUtils.getBooleanValue("Settings.bootAllObjectsOnStart", mapRepresentation);
        screenNamePattern = YamlUtils.getStringValue("Settings.screenNamePattern", mapRepresentation);
        startDelay = YamlUtils.getLongValue("Settings.eachObjectStartDelay", mapRepresentation);
        reverseOrderOnStop = YamlUtils.getBooleanValue("Settings.reverseStartOrderOnStop", mapRepresentation);
        Map<String, Map<String, String>> objects = YamlUtils.getMapValue("Booting Objects", mapRepresentation);
        MCBootstrap.getLogger().info("Loading booting objects from config...");
        objects.forEach(BootingObject::from);
        BootingAPI.getBootingObjects().sort(Comparator.comparingInt((BootingObject object) -> object.getPriority().getPoints()).reversed());
        MCBootstrap.getLogger().info(BootingAPI.getBootingObjects().size() + " objects for booting loaded from config.");
    }

    public static void setupFiles() throws IOException {
        dataFolder.mkdirs(); extensionsFolder.mkdirs();
        if (!configFile.exists()) {
            configFile.createNewFile();
            BasicUtils.copyResourceFile(configFile);
            MCBootstrap.getLogger().debug("Config file created: " + configFile.getAbsolutePath());
        }
    }

    public static File getScreensFolder() {
        return screensFolder;
    }

    public static File getDataFolder() {
        return dataFolder;
    }

    public static File getConfigFile() {
        return configFile;
    }

    public static File getExtensionsFolder() {
        return extensionsFolder;
    }

    public static File getRunnedJar() {
        return runnedJar;
    }

    public static boolean bootAllOnStart() {
        return bootAllOnStart;
    }

    public static boolean reverseOrderOnStop() {
        return reverseOrderOnStop;
    }

    public static long getStartDelay() {
        return startDelay;
    }

    public static String getScreenNamePattern() {
        return screenNamePattern;
    }

}