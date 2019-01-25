package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.utils.BasicUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BootingServer extends BootingApplication {

    private final BootingObject parentObject;
    private int customPort = -1;

    BootingServer(String name, Map<String, String> properties) {
        super(name, properties, false);
        if (properties.containsKey("port")) customPort = Integer.parseInt(properties.get("port"));
        this.parentObject = null;
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onBootingObjectAfterLoad(this)));
    }

    BootingServer(BootingGroup bootingGroup, String name, File directory) {
        super(directory, name, bootingGroup.bootCommand, bootingGroup.priority);
        this.parentObject = bootingGroup;
        this.autoRestart = bootingGroup.autoRestart;
        if (bootingGroup.hasFirstPort()) this.customPort = bootingGroup.getFirstPort() + bootingGroup.getChildServers().size();
    }

    BootingServer(PrimaryBootingServer primaryServer, String name, File directory) {
        super(directory, name, primaryServer.bootCommand, primaryServer.priority);
        this.parentObject = primaryServer;
        this.autoRestart = primaryServer.autoRestart;
        this.customPort = primaryServer.getFirstPort() + primaryServer.getClonedServers().indexOf(this);
    }

    @Override
    public synchronized void setScreenID(int screenID) {
        if (screenID == -1) {
            if (parentObject instanceof PrimaryBootingServer) {
                try {
                    BasicUtils.deleteDirectory(directory, false);
                } catch (IOException e) {
                    MCBootstrap.getLogger().error("Can't delete '" + name + "' directory (" + directory.getAbsolutePath() + ")", e.getMessage());
                }
            }
        }
        super.setScreenID(screenID);
    }

    public boolean hasCustomPort() {
        return customPort != -1;
    }

    public int getCustomPort() {
        return customPort;
    }

    public boolean hasParent() {
        return parentObject != null;
    }

    public BootingObject getParentObject() {
        return parentObject;
    }

}