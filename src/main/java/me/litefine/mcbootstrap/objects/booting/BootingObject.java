package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.main.BootingAPI;
import me.litefine.mcbootstrap.main.MCBootstrap;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.function.Predicate;

public abstract class BootingObject {

    protected Priority priority = Priority.NORMAL;
    protected File directory;
    protected String name, bootCommand, stopCommand = null;
    protected boolean autoRestart = false;

    protected static Predicate<File> directoryValidator = file -> file.isDirectory() && file.listFiles() != null;

    BootingObject(String name, Map<String, String> properties) {
        if (BootingAPI.getBootingObjectByName(name, false).isPresent())
            throw new InvalidParameterException("Object with name '" + name + "' already exists!");
        this.name = name;
        this.bootCommand = properties.get("bootCommand");
        this.directory = new File(properties.get("directory"));
        if (properties.containsKey("stopCommand")) stopCommand = properties.get("stopCommand");
        if (properties.containsKey("autoRestart")) autoRestart = Boolean.valueOf(properties.get("autoRestart"));
        if (properties.containsKey("priority")) this.priority = Priority.valueOf(properties.get("priority").toUpperCase());
        if (directoryValidator.test(directory)) BootingAPI.getBootingObjects().add(this);
        else throw new InvalidParameterException("Invalid directory '" + directory + "'");
    }

    BootingObject(File directory, String name, String processCommand, Priority priority) {
        if (directoryValidator.test(directory)) {
            this.directory = directory;
            this.name = name;
            this.bootCommand = processCommand;
            this.priority = priority;
        } else throw new InvalidParameterException("Invalid directory!");
    }

    public abstract void bootObject();

    public abstract void stopObject();

    public boolean hasAutoRestartProperty() {
        return autoRestart;
    }

    public String getName() {
        return name;
    }

    public String getBootCommand() {
        return bootCommand;
    }

    public boolean hasStopCommand() {
        return stopCommand != null;
    }

    public String getStopCommand() {
        return stopCommand;
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
            if (type.equalsIgnoreCase("SERVER")) new BootingServer(name, properties);
            else if (type.equalsIgnoreCase("GROUP")) new BootingGroup(name, properties);
            else if (type.equalsIgnoreCase("PRIMARY_SERVER")) new PrimaryBootingServer(name, properties);
            else if (type.equalsIgnoreCase("APPLICATION")) new BootingApplication(name, properties, true);
            else MCBootstrap.getLogger().warn("Unknown type of '" + name + "' booting object!");
        } catch (Exception ex) {
            MCBootstrap.getLogger().warn("Can't load object '" + name + "' - " + ex.getMessage());
        }
    }


    public enum Priority {

        LOWEST(8), LOW(16), NORMAL(32), HIGH(64), HIGHEST(128), DONALD_TRUMP(256);

        private int points;

        Priority(int points) {
            this.points = points;
        }

        public int getPoints() {
            return points;
        }

    }


}