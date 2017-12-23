package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.utils.BasicUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by LITEFINE IDEA on 03.12.17.
 */
public class BootingServer extends BootingObject {

    // START: screen -dmS test bash -c "cd /Users/Sergey/Desktop/testServer/ && java -jar Spigot.jar"
    // STOP: screen -S test -p 0 -X stuff "stop`echo -ne '\015'`"
    // KILL: screen -X -S test quit

    private final BootingObject parent;
    private final String screenName;
    private int uniqueID = -1, customPort = -1;

    BootingServer(String name, Map<String, String> properties) {
        super(name, properties);
        if (properties.containsKey("port")) customPort = Integer.parseInt(properties.get("port"));
        this.screenName = BasicUtils.getScreenNameFor(this);
        this.parent = null;
    }

    BootingServer(BootingGroup bootingGroup, String name, File directory) {
        super(directory, name, bootingGroup.javaCommand, bootingGroup.priority);
        this.screenName = BasicUtils.getScreenNameFor(this);
        this.parent = bootingGroup;
    }

    BootingServer(PrimaryBootingServer primaryServer, String name, File directory) {
        super(directory, name, primaryServer.javaCommand, primaryServer.priority);
        this.customPort = primaryServer.getFirstPort() + primaryServer.getClonedServers().size() - 1;
        this.screenName = BasicUtils.getScreenNameFor(this);
        this.parent = primaryServer;
    }

    @Override
    public void bootObject() {
        try {
            MCBootstrap.getLogger().info("Launch server '" + name + "', screen: " + screenName);
            if (parent instanceof PrimaryBootingServer) {
                PrimaryBootingServer pServer = (PrimaryBootingServer) parent;
                if (directory.exists()) {
                    try {
                        BasicUtils.deleteDirectory(directory, true);
                    } catch (IOException e) {
                        MCBootstrap.getLogger().error("Can't clean '" + name + "' directory (" + directory.getAbsolutePath() + ")", e.getMessage());
                    }
                } else directory.mkdirs();
                directory.deleteOnExit();
                pServer.clonePrimaryDirectory(this);
            }
            new ProcessBuilder("screen", "-dmS", screenName, "bash", "-c", javaCommand + (customPort != -1 ? " -p " + customPort : ""))
                    .directory(directory).inheritIO().start();
        } catch (IOException e) {
            MCBootstrap.getLogger().error("An error occured while booting server '" + name + "'", e.getMessage());
        }
    }

    @Override
    public void stopObject() {
        try {
            MCBootstrap.getLogger().info("Stopping server '" + name + "', screen: " + screenName);
            new ProcessBuilder("screen", "-S", uniqueID + "." + screenName, "-p 0", "-X stuff", "\"stop`echo -ne '\\015'`\"").inheritIO().start();
        } catch (IOException e) {
            MCBootstrap.getLogger().error("Can't stop screen for " + name, e.getMessage());
        }
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
        if (uniqueID == -1 && parent instanceof PrimaryBootingServer) {
            try {
                BasicUtils.deleteDirectory(directory, false);
            } catch (IOException e) {
                MCBootstrap.getLogger().error("Can't delete '" + name + "' directory (" + directory.getAbsolutePath() + ")", e.getMessage());
            }
        }
    }

    public int getCustomPort() {
        return customPort;
    }

    public boolean hasCustomPort() {
        return customPort != -1;
    }

    public String getScreenName() {
        return screenName;
    }

    @Override
    public boolean isBooted() {
        return uniqueID != -1;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public BootingObject getParent() {
        return parent;
    }

}