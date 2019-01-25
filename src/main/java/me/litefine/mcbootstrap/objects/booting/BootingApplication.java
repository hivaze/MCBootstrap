package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.utils.BasicUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BootingApplication extends BootingObject {

    protected final String screenName;
    protected int screenID = -1;
    protected boolean temporaryBootingFlag = false;

    BootingApplication(String name, Map<String, String> properties, boolean justApp) {
        super(name, properties);
        this.screenName = BasicUtils.getScreenNameFor(this);
        if (justApp) ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onBootingObjectAfterLoad(this)));
    }

    BootingApplication(File directory, String name, String startCommand, Priority priority) {
        super(directory, name, startCommand, priority);
        this.screenName = BasicUtils.getScreenNameFor(this);
    }

    @Override
    public void bootObject() {
        try {
            temporaryBootingFlag = true;
            MCBootstrap.getLogger().info("Launch application '" + name + "', screen: " + screenName);
            new ProcessBuilder("screen", "-dmS", screenName, "bash", "-c", startCommand).directory(directory).inheritIO().start();
        } catch (IOException e) {
            MCBootstrap.getLogger().error("An error occurred while booting server '" + name + "' - " + e.getMessage());
            temporaryBootingFlag = false;
        }
    }

    @Override
    public void stopObject() {
        if (temporaryBootingFlag) return;
        try {
            MCBootstrap.getLogger().info("Stopping application '" + name + "', screen: " + screenID + "." + screenName);
            if (hasStopCommand()) new ProcessBuilder("screen", "-p", "0", "-S", screenID + "." + screenName, "-X", "eval", "stuff", "\"" + stopCommand +"\"\\015").inheritIO().start();
            else new ProcessBuilder("screen", "-X", "-S", screenID + "." + screenName, "quit").inheritIO().start();
        } catch (IOException e) {
            MCBootstrap.getLogger().error("Can't stop screen for application '" + name + "' - " + e.getMessage());
        }
    }

    public synchronized void setScreenID(int screenID) {
        this.screenID = screenID;
        this.temporaryBootingFlag = false;
        if (screenID == -1 && autoRestart) this.bootObject();
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