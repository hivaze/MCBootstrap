package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.BootingAPI;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.utils.BasicUtils;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BootingApplication extends BootingObject {

    protected final String screenName;
    protected int screenID = -1;
    protected boolean temporaryBootingFlag = false;
    private boolean blockAutoRestart = false;

    BootingApplication(String name, Map<String, String> properties, boolean justApp) {
        super(name, properties);
        this.screenName = BasicUtils.getScreenNameFor(this);
        if (justApp) {
            BootingAPI.getBootingObjects().add(this);
            ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onBootingObjectAfterLoad(this)));
        }
    }

    BootingApplication(boolean doDirCheck, File directory, String name, String startCommand, Priority priority) {
        super(doDirCheck, directory, name, startCommand, priority);
        this.screenName = BasicUtils.getScreenNameFor(this);
    }

    @Override
    public void bootObject() {
        try {
            temporaryBootingFlag = true;
            MCBootstrap.getLogger().info("Launch '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "', screen name: '" + screenName + "'...");
            new ProcessBuilder("screen", "-dmS", screenName, "bash", "-c", bootCommand).directory(directory).inheritIO().start();
        } catch (IOException e) {
            MCBootstrap.getLogger().error("An error occurred while booting '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "'", e);
            temporaryBootingFlag = false;
        }
    }

    @Override
    public void stopObject() {
        blockAutoRestart = true;
        if (temporaryBootingFlag || !isBooted()) return;
        try {
            MCBootstrap.getLogger().info("Stopping '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "', screen: '" + screenID + "." + screenName + "'...");
            if (hasStopCommand()) new ProcessBuilder("screen", "-p", "0", "-S", screenID + "." + screenName, "-X", "eval", "stuff " + stopCommand + "\\015").inheritIO().start();
            else new ProcessBuilder("screen", "-X", "-S", screenID + "." + screenName, "quit").inheritIO().start();
        } catch (IOException e) {
            MCBootstrap.getLogger().error("Can't stop screen for '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "'", e);
        }
    }

    public synchronized void setScreenID(int screenID) {
        this.screenID = screenID;
        this.temporaryBootingFlag = false;
        this.notifyAll();
        if (screenID == -1 && autoRestart && !blockAutoRestart) this.bootObject();
        else if (screenID == -1 && autoRestart) blockAutoRestart = false;
    }

    public String getScreenName() {
        return screenName;
    }

    public synchronized boolean isStarting() {
        return temporaryBootingFlag;
    }

    public synchronized boolean isBooted() {
        return screenID != -1;
    }

    public synchronized int getScreenID() {
        return screenID;
    }

}