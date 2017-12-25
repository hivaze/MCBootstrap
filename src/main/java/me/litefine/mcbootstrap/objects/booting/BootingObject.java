package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by LITEFINE IDEA on 02.12.17.
 */
public abstract class BootingObject {

    protected Priority priority = Priority.NORMAL;
    protected File directory;
    protected String name, javaCommand;
    protected boolean autoRestart = false;

    protected static Predicate<File> directoryValidator = file -> file.isDirectory() && file.listFiles() != null;

    BootingObject(String name, Map<String, String> properties) {
        this.name = name;
        this.javaCommand = properties.get("javaCommand");
        this.directory = new File(properties.get("directory"));
        if (properties.containsKey("autoRestart")) autoRestart = Boolean.valueOf(properties.get("autoRestart"));
        if (properties.containsKey("priority")) this.priority = Priority.valueOf(properties.get("priority").toUpperCase());
        if (directoryValidator.test(directory)) Settings.getBootingObjects().add(this);
        else throw new InvalidParameterException("Booting object '" + name + "' has invalid directory!");
    }

    BootingObject(File directory, String name, String processCommand, Priority priority) {
        if (directoryValidator.test(directory)) {
            this.directory = directory;
            this.name = name;
            this.javaCommand = processCommand;
            this.priority = priority;
        } else throw new InvalidParameterException("Booting object '" + name + "' has invalid directory!");
    }

    public abstract void bootObject();

    public abstract void stopObject();

    public boolean hasAutoRestartProperty() {
        return autoRestart;
    }

    public boolean isRunningServer() {
        return this instanceof BootingServer && ((BootingServer) this).isBooted();
    }

    public String getName() {
        return name;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public File getDirectory() {
        return directory;
    }

    public Priority getPriority() {
        return priority;
    }

    public static void from(String name, Map<String, String> properties) {
        try {
            String type = properties.get("type").toUpperCase();
            if (type.equals("SERVER")) new BootingServer(name, properties);
            else if (type.equals("GROUP")) new BootingGroup(name, properties);
            else if (type.equals("PRIMARY_SERVER")) new PrimaryBootingServer(name, properties);
            else MCBootstrap.getLogger().warn("Unknown type of '" + name + "' booting object!");
        } catch (Exception ex) {
            MCBootstrap.getLogger().warn("Can't load object '" + name + "'", ex.getMessage());
        }
    }


    public enum Priority {

        LOW(16), NORMAL(32), HIGH(64);

        private int points;

        Priority(int points) {
            this.points = points;
        }

        public int getPoints() {
            return points;
        }

    }


}