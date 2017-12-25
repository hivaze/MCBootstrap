package me.litefine.mcbootstrap.console;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.objects.CommandConsumer;
import me.litefine.mcbootstrap.objects.booting.BootingGroup;
import me.litefine.mcbootstrap.objects.booting.BootingObject;
import me.litefine.mcbootstrap.objects.booting.BootingServer;
import me.litefine.mcbootstrap.objects.booting.PrimaryBootingServer;
import me.litefine.mcbootstrap.utils.BasicUtils;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

/**
 * Created by LITEFINE IDEA on 03.12.17.
 */
public class CommandsManager {

    private static final HashMap<String, CommandConsumer> commands = new HashMap<>();

    public static void execureCommand(String command) {
        if (!command.trim().isEmpty()) {
            command = BasicUtils.removeExtraSpaces(command);
            String[] args = BasicUtils.getArguments(command);
            command = command.split("\\s")[0].toLowerCase();
            if (commands.containsKey(command)) {
                long time = System.currentTimeMillis();
                commands.get(command).execute(args);
                MCBootstrap.getLogger().debug("Command '" + command + "' executed in " + (System.currentTimeMillis() - time) + " ms.");
            } else MCBootstrap.getLogger().warn("Unknown command! For more information use 'help' command.");
        } else MCBootstrap.getLogger().warn("Empty command input!");
    }

    public static void registerCommands() {
        commands.put("help", args -> {
            System.out.println();
            System.out.println(("MCBootstrap by LITEFINE v1.0-SNAPSHOT"));
            System.out.println();
            System.out.println(" Standalone application to control minecraft servers start & stop");
            System.out.println(" Wiki: {URL}");
            System.out.println();
            System.out.println(" Known commands:");
            System.out.println(" - 'info' -> Shows basic information about application work");
            System.out.println(" - 'info objects' -> Shows information about booting objects");
            System.out.println(" - 'info servers' -> Shows information about all booting servers");
            System.out.println(" - 'info groups' -> Shows information about all booting groups");
            System.out.println(" - 'info primaries' -> Shows information about all primary booting servers");
            System.out.println(" - 'info <objectName>' -> Shows all information about specific object");
            System.out.println(" - 'start <all|objectName> -> Start specific object or all booting objects");
            System.out.println(" - 'stop <all|objectName> -> Stop specific object or all running objects");
            System.out.println(" - 'shutdown -> Shutdown application and all running objects");
            System.out.println();
        });
        commands.put("info", args -> {
            if (args.length == 0) {
                System.out.println();
                System.out.println("MCBootstrap information:");
                System.out.println();
                System.out.println(" Launched servers: " + Settings.getRunningServers().size());
                System.out.println(" For more information about booting objects use 'info objects' command.");
                System.out.println();
                System.out.println(" Main folder: " + Settings.getDataFolder().getAbsolutePath());
                System.out.println(" Screens folder: " + Settings.getScreensFolder().getAbsolutePath());
                System.out.println();
                System.out.println(" Uptime: " + BasicUtils.millisToPattern(ManagementFactory.getRuntimeMXBean().getUptime()));
                System.out.println(" RAM usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
                if (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() > 0)
                    System.out.println(" System average load: " + BasicUtils.round(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage(), 2));
                System.out.println();
            } else if (args[0].equalsIgnoreCase("objects")) {
                System.out.println();
                System.out.println("Booting objects information");
                System.out.println();
                System.out.println(" Total count: " + Settings.getBootingObjects().size());
                System.out.println(" For information about specific object use 'info <objectName>' command.");
                System.out.println(" Inrormation about all servers: 'info servers', groups: 'info groups', primaries: 'info primaries'");
                if (!Settings.getBootingObjects().isEmpty()) {
                    System.out.println();
                    System.out.println(" Loaded servers count: " + Settings.getBootingServers().size());
                    System.out.println(" Loaded groups count: " + Settings.getBootingGroups().size());
                    System.out.println(" Loaded primaries count: " + Settings.getPrimaryBootingServers().size());
                    System.out.println();
                    System.out.println(" List of objects in priority order:");
                    System.out.println(" {" + BasicUtils.join(Settings.getBootingObjectsNames(), ", ") + "}");
                }
                System.out.println();
            } else if (args[0].equalsIgnoreCase("servers")) {
                System.out.println();
                System.out.println("Booting servers information");
                System.out.println();
                System.out.println(" Launched servers: " + Settings.getRunningServers().size() + "/" + Settings.getBootingServers().size());
                Settings.getBootingServers().forEach(server -> System.out.println(" - " + server.getName() +
                        " (" + (server.isBooted() ? "BOOTED" : "NOT BOOTED") + (server.hasCustomPort() ? ", port: " + server.getCustomPort() : "") +")"));
                System.out.println();
            } else if (args[0].equalsIgnoreCase("groups")) {
                System.out.println();
                System.out.println("Booting groups information");
                System.out.println();
                System.out.println(" Total groups count: " + Settings.getBootingGroups().size());
                Settings.getBootingGroups().forEach(group -> {
                    System.out.println(" - Group name: '" + group.getName() + "', servers count: " + group.getServers().size());
                    System.out.println(" Servers: " + BasicUtils.getServersString(group));
                });
                System.out.println();
            } else if (args[0].equalsIgnoreCase("primaries")) {
                System.out.println();
                System.out.println("Primary booting servers information:");
                System.out.println();
                System.out.println(" Total primaries count: " + Settings.getPrimaryBootingServers().size());
                Settings.getPrimaryBootingServers().forEach(primary -> {
                    System.out.println(" - Primary name: '" + primary.getName() + "', servers count: " + primary.getClonedServers().size());
                    System.out.println(" Servers: " + BasicUtils.getServersString(primary));
                });
                System.out.println();
            } else {
                BootingObject object = Settings.getBootingObjectByName(args[0]);
                if (object != null) {
                    System.out.println();
                    System.out.println("Object name: " + object.getName());
                    System.out.println();
                    System.out.println(" Type: " + object.getClass().getSimpleName());
                    System.out.println(" Priority: " + object.getPriority());
                    System.out.println(" Directory: " + object.getDirectory().getAbsolutePath());
                    System.out.println(" Auto restart: " + object.hasAutoRestartProperty());
                    System.out.println(" Java command: " + object.getJavaCommand());
                    System.out.println();
                    if (object instanceof BootingServer) {
                        BootingServer server = (BootingServer) object;
                        System.out.println(" Is booted: " + server.isBooted());
                        if (server.hasCustomPort()) System.out.println(" Custom port: " + server.getCustomPort());
                        System.out.println(" Screen name: " + (server.isBooted() ? server.getScreenID() + "." : "") + server.getScreenName());
                        if (server.hasParent()) System.out.println(" Parent object: " + server.getParent());
                    } else if (object instanceof BootingGroup) {
                        BootingGroup group = (BootingGroup) object;
                        if (group.hasFirstPort()) System.out.println(" First port: " + group.getFirstPort());
                        System.out.println(" Servers: " + BasicUtils.getServersString(group));
                    } else if (object instanceof PrimaryBootingServer) {
                        PrimaryBootingServer primary = (PrimaryBootingServer) object;
                        System.out.println(" First port: " + primary.getFirstPort());
                        System.out.println(" Generation directory: " + primary.getGenerationDirectory().getAbsolutePath());
                        System.out.println(" Servers: " + BasicUtils.getServersString(primary));
                    }
                    System.out.println();
                } else System.out.println("Object with name '" + args[0] + "' not found!");
            }
        });
        commands.put("start", args -> {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("all")) MCBootstrap.startAllObjects();
                else {
                    BootingObject object = Settings.getBootingObjectByName(args[0]);
                    if (object != null) {
                        if (!object.isRunningServer()) object.bootObject();
                        else System.out.println("That server is already booted!");
                    } else System.out.println("Object with name '" + args[0] + "' not found!");
                }
            } else System.out.println("Usage: 'start <all|objectName>'");
        });
        commands.put("stop", args -> {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("all")) MCBootstrap.stopAllObjects();
                else {
                    BootingObject object = Settings.getBootingObjectByName(args[0]);
                    if (object != null) object.stopObject();
                    else System.out.println("Object with name '" + args[0] + "' not found!");
                }
            } else System.out.println("Usage: 'stop <all|objectName>'");
        });
        commands.put("shutdown", args -> MCBootstrap.shutdown(true));
        commands.put("systemexit", args -> MCBootstrap.shutdown(false));
    }

}