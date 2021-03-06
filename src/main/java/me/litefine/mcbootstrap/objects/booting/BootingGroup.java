package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.BootingAPI;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.utils.BasicUtils;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BootingGroup extends BootingObject implements ParenthoodObject {

    private final List<BootingServer> childServers = new ArrayList<>();

    private int firstPort = -1;
    private String commonHost = null;

    BootingGroup(String name, Map<String, String> properties) {
        super(name, properties);
        if (properties.containsKey("firstPort")) firstPort = Integer.parseInt(properties.get("firstPort"));
        if (properties.containsKey("commonHost")) commonHost = properties.get("commonHost");
        List<File> insideDirs = Arrays.stream(directory.listFiles()).filter(File::isDirectory).collect(Collectors.toList());
        insideDirs.forEach(file -> {
            String serverName = name + "-" + (insideDirs.indexOf(file) + 1);
            try {
                childServers.add(new BootingServer(this, serverName, file));
            } catch (Exception ex) {
                MCBootstrap.getLogger().warn("Can't load '" + BasicUtils.colorize(serverName, Ansi.Color.YELLOW)  + "' server in group '" + BasicUtils.colorize(name, Ansi.Color.YELLOW)  + "' - " + ex);
            }
        });
        MCBootstrap.getLogger().debug(childServers.size() + " servers found in '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "' booting group.");
        BootingAPI.getBootingObjects().add(this);
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onBootingObjectAfterLoad(this)));
    }

    @Override
    public synchronized void bootObject() {
        MCBootstrap.getLogger().info("Launch group of servers '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "', servers: " + childServers.size());
        childServers.forEach(bootingServer -> {
            if (!bootingServer.isBooted() && !bootingServer.isStarting()) {
                bootingServer.bootObject();
                if (Settings.getStartDelay() > 0 && childServers.indexOf(bootingServer) != childServers.size()-1) {
                    try {
                        MCBootstrap.getLogger().info("Waiting for " + Settings.getStartDelay() + " second(s) (delay)...");
                        Thread.sleep(Settings.getStartDelay() * 1000L);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    @Override
    public synchronized void stopObject() {
        MCBootstrap.getLogger().info("Stopping group of servers '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "', servers: " + childServers.size());
        childServers.forEach(childStopper);
    }

    @Override
    public List<BootingServer> getChildServers() {
        return Collections.unmodifiableList(childServers);
    }

    public boolean hasFirstPort() {
        return firstPort != -1;
    }

    public int getFirstPort() {
        return firstPort;
    }

    public boolean hasCommonHost() {
        return commonHost != null;
    }

    public String getCommonHost() {
        return commonHost;
    }

}