package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.BootingAPI;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.utils.BasicUtils;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.function.Predicate;

public class BootingServer extends BootingApplication {

    private final BootingObject parentObject;
    private String customHost = null;
    private int customPort = -1;

    protected static Predicate<File> serverDirectoryValidator = dir -> {
        for (String fileName : dir.list()) if (fileName.endsWith(".jar")) return true;
        return false;
    };

    BootingServer(String name, Map<String, String> properties) {
        super(name, properties, false);
        if (properties.containsKey("port")) customPort = Integer.parseInt(properties.get("port"));
        if (properties.containsKey("host")) customHost = properties.get("host");
        this.parentObject = null;
        BootingAPI.getBootingObjects().add(this);
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onBootingObjectAfterLoad(this)));
        afterLoad(true);
    }

    BootingServer(BootingGroup bootingGroup, String name, File directory) {
        super(true, directory, name, bootingGroup.bootCommand, bootingGroup.priority);
        this.parentObject = bootingGroup;
        this.autoRestart = bootingGroup.autoRestart;
        if (bootingGroup.hasCommonHost()) this.customHost = bootingGroup.getCommonHost();
        if (bootingGroup.hasFirstPort()) this.customPort = bootingGroup.getFirstPort() + bootingGroup.getChildServers().size();
        afterLoad(true);
    }

    BootingServer(PrimaryBootingServer primaryServer, String name, File directory) {
        super(false, directory, name, primaryServer.bootCommand, primaryServer.priority);
        this.parentObject = primaryServer;
        this.autoRestart = primaryServer.autoRestart;
        this.customPort = primaryServer.getFirstPort() + primaryServer.getChildServers().size();
        afterLoad(false);
    }

    private void afterLoad(boolean doDirCheck) {
        if (hasCustomHost()) bootCommand += " -host " + customHost;
        if (hasCustomPort()) bootCommand += " -port " + customPort;
        if (doDirCheck && !serverDirectoryValidator.test(directory)) throw new InvalidParameterException("Invalid directory '" + directory + "': server core (.jar) not found!");
    }

    @Override
    public void bootObject() {
        if (parentObject != null && parentObject instanceof PrimaryBootingServer) {
            temporaryBootingFlag = true;
            PrimaryBootingServer parent = (PrimaryBootingServer) parentObject;
            if (directory.exists()) {
                try {
                    BasicUtils.deleteDirectory(directory, false);
                } catch (IOException e) {
                    MCBootstrap.getLogger().error("Can't delete '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "' directory (" + directory.getAbsolutePath() + ")", e);
                }
            }
            try {
                parent.clonePrimaryDirectory(this);
            } catch (IOException e) {
                MCBootstrap.getLogger().error("An error occurred while booting '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "'", e);
                temporaryBootingFlag = false;
                return;
            }
        }
        super.bootObject();
    }

    @Override
    public synchronized void setScreenID(int screenID) {
        if (screenID == -1) {
            if (parentObject instanceof PrimaryBootingServer) {
                try {
                    BasicUtils.deleteDirectory(directory, false);
                } catch (IOException e) {
                    MCBootstrap.getLogger().error("Can't delete '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "' directory (" + directory.getAbsolutePath() + ")", e);
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

    public boolean hasCustomHost() {
        return customHost != null;
    }

    public String getCustomHost() {
        return customHost;
    }

    public boolean hasParent() {
        return parentObject != null;
    }

    public BootingObject getParentObject() {
        return parentObject;
    }

}