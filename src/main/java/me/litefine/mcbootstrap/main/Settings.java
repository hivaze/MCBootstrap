package me.litefine.mcbootstrap.main;

import me.litefine.mcbootstrap.objects.UniqueFilesPolicy;
import me.litefine.mcbootstrap.objects.booting.BootingGroup;
import me.litefine.mcbootstrap.objects.booting.BootingObject;
import me.litefine.mcbootstrap.objects.booting.BootingServer;
import me.litefine.mcbootstrap.objects.booting.PrimaryBootingServer;
import me.litefine.mcbootstrap.utils.BasicUtils;
import me.litefine.mcbootstrap.utils.WatcherUtil;
import me.litefine.mcbootstrap.utils.YamlUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by LITEFINE IDEA on 26.11.17.
 */
public class Settings {

    private static File dataFolder, configFile, runnedJar, screensFolder;
    private static final List<BootingObject> bootingObjects = new ArrayList<>();

    private static long startDelay;
    private static String screenNamePattern;
    private static boolean bootAllOnStart;
    private static boolean reverseOrderOnStop;

    static {
        try {
            Process request = Runtime.getRuntime().exec("screen -ls");
            request.waitFor();
            String folder = new Scanner(request.getInputStream()).findWithinHorizon("\\/(.*?)\\/(.*)\\b", Integer.MAX_VALUE);
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
    }

    public static void loadFromConfig() throws Exception {
        Map mapRepresentation = YamlUtils.readObject(HashMap.class);
        if (YamlUtils.getBooleanValue("Settings.enableDebug", mapRepresentation)) {
            Configurator.setLevel(MCBootstrap.class.getCanonicalName(), Level.DEBUG);
            MCBootstrap.getLogger().debug("Logger debug mode enabled!");
        }
        bootAllOnStart = YamlUtils.getBooleanValue("Settings.bootAllOnStart", mapRepresentation);
        screenNamePattern = YamlUtils.getStringValue("Settings.screenNamePattern", mapRepresentation);
        startDelay = YamlUtils.getLongValue("Settings.eachServerStartDelay", mapRepresentation);
        reverseOrderOnStop = YamlUtils.getBooleanValue("Settings.reverseStartOrderOnStop", mapRepresentation);
        Map<String, Map<String, String>> objects = YamlUtils.getMapValue("Booting Objects", mapRepresentation);
        objects.forEach(BootingObject::from);
        bootingObjects.sort(Comparator.comparingInt((BootingObject object) -> object.getPriority().getPoints()).reversed());
        MCBootstrap.getLogger().info(bootingObjects.size() + " objects for booting loaded from config.");
    }

    public static void setupFiles() throws IOException {
        if (dataFolder.mkdirs()) MCBootstrap.getLogger().debug("Main folder created: " + dataFolder.getAbsolutePath());
        if (!configFile.exists()) {
            configFile.createNewFile();
            BasicUtils.copyResourceFile(configFile);
            MCBootstrap.getLogger().debug("Config file created: " + configFile.getAbsolutePath());
        }
        File customPolicyFile = new File(Settings.getDataFolder(), "CUSTOM_FILES_POLICY.java");
        if (customPolicyFile.exists()) {
            MCBootstrap.getLogger().debug("Custom files policy found! Loading...");
            try {
                URLClassLoader loader = new URLClassLoader(new URL[]{customPolicyFile.toURI().toURL()});
                Class<? extends UniqueFilesPolicy> policyClass = loader.loadClass(customPolicyFile.getName()).asSubclass(UniqueFilesPolicy.class);
                CUSTOM_POLICY = policyClass.newInstance();
                MCBootstrap.getLogger().debug("Custom files policy successfully loaded.");
            }
            catch (ClassCastException ex) {
                MCBootstrap.getLogger().warn("Custom files policy class isn't a UniqueFilesPolicy instance!");
            } catch (ReflectiveOperationException | MalformedURLException ex) {
                MCBootstrap.getLogger().warn("An error occured while loading custom policy class" + ex.getMessage());
            }
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


    // Booting objects part
    public static List<BootingObject> getBootingObjects() {
        return bootingObjects;
    }

    public static List<BootingObject> getRunningObjects() {
        return bootingObjects.stream().filter(BootingObject::isBooted).collect(Collectors.toList());
    }

    public static BootingObject getBootingObjectByName(String name) {
        return bootingObjects.stream().filter(bootingObject -> bootingObject.getName().equals(name)).findFirst().orElse(null);
    }

    public static List<String> getBootingObjectsNames() {
        return bootingObjects.stream().map(BootingObject::getName).collect(Collectors.toList());
    }

    // Booting servers part
    public static List<BootingServer> getBootingServers() {
        return bootingObjects.stream().filter(BootingServer.class::isInstance).map(BootingServer.class::cast).collect(Collectors.toList());
    }

    public static List<BootingServer> getRunningServers() {
        return getBootingServers().stream().filter(BootingServer::isBooted).collect(Collectors.toList());
    }

    public static BootingServer getServerByScreenName(String screenName) {
        return getBootingServers().stream().filter(server -> server.getScreenName().equals(screenName)).findFirst().orElse(null);
    }

    // Booting groups part
    public static List<BootingGroup> getBootingGroups() {
        return bootingObjects.stream().filter(BootingGroup.class::isInstance).map(BootingGroup.class::cast).collect(Collectors.toList());
    }

    public static List<BootingGroup> getRunningGroups() {
        return getBootingGroups().stream().filter(BootingGroup::isBooted).collect(Collectors.toList());
    }

    // Booting primaries part
    public static List<PrimaryBootingServer> getPrimaryBootingServers() {
        return bootingObjects.stream().filter(PrimaryBootingServer.class::isInstance).map(PrimaryBootingServer.class::cast).collect(Collectors.toList());
    }

    public static List<PrimaryBootingServer> getRunningPrimaryServers() {
        return bootingObjects.stream().filter(PrimaryBootingServer.class::isInstance).map(PrimaryBootingServer.class::cast).collect(Collectors.toList());
    }

    // Unique files policies
    static final UniqueFilesPolicy INORDER_POLICY = (primaryBootingServer, forObject) -> {
        int index = primaryBootingServer.getClonedServers().indexOf(forObject);
        return primaryBootingServer.getUniqueFiles().listFiles()[index];
    };
    static final UniqueFilesPolicy RANDOM_POLICY = (primaryBootingServer, forObject) -> {
        int bound = primaryBootingServer.getUniqueFiles().listFiles().length - 1;
        return primaryBootingServer.getUniqueFiles().listFiles()[BasicUtils.RANDOM.nextInt(bound)];
    };
    static UniqueFilesPolicy CUSTOM_POLICY = (primaryBootingServer, forObject) -> null;

}