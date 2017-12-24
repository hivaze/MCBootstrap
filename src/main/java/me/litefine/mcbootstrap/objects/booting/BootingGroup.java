package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by LITEFINE IDEA on 03.12.17.
 */
public class BootingGroup extends BootingObject {

    private final Supplier<List<BootingServer>> servers = () ->
            Settings.getBootingServers().stream().filter(server -> server.getParent() == this).collect(Collectors.toList());

    private boolean booted = false;
    private int firstPort = -1;

    BootingGroup(String name, Map<String, String> properties) {
        super(name, properties);
        if (properties.containsKey("firstPort")) firstPort = Integer.parseInt(properties.get("firstPort"));
        List<File> insideDirs = Arrays.stream(directory.listFiles()).filter(File::isDirectory).collect(Collectors.toList());
        insideDirs.forEach(file -> {
            String serverName = name + "-" + insideDirs.indexOf(file);
            try {
                new BootingServer(this, serverName, file);
            } catch (Exception ex) {
                MCBootstrap.getLogger().warn("Can't load server '" + serverName + "' in group '" + name + "'", ex.getMessage());
            }
        });
        MCBootstrap.getLogger().info(servers.get().size() + " valid servers found in '" + name + "' booting group.");
    }

    @Override
    public void bootObject() {
        MCBootstrap.getLogger().info("Launch group of servers '" + name + "', servers: " + servers.get().size());
        servers.get().forEach(bootingServer -> {
            if (!bootingServer.isBooted()) {
                bootingServer.bootObject();
                if (Settings.getStartDelay() > 0 && servers.get().indexOf(bootingServer) != servers.get().size()-1) {
                    try {
                        MCBootstrap.getLogger().info("Waiting for " + Settings.getStartDelay() * 1000L + " ms (delay)...");
                        Thread.sleep(Settings.getStartDelay() * 1000L);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
        booted = true;
    }

    @Override
    public void stopObject() {
        MCBootstrap.getLogger().info("Stopping group of servers '" + name + "', servers: " + servers.get().size());
        servers.get().forEach(bootingServer -> {
            if (bootingServer.isBooted())
                bootingServer.stopObject();
        });
        booted = false;
    }

    public List<BootingServer> getServers() {
        return servers.get();
    }

    @Override
    public boolean isBooted() {
        return booted;
    }

    public boolean hasFirstPort() {
        return firstPort != -1;
    }

    public int getFirstPort() {
        return firstPort;
    }

}