package me.litefine.mcbootstrap.console;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.BootingAPI;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.objects.UniqueFilesPolicy;
import me.litefine.mcbootstrap.objects.booting.*;
import me.litefine.mcbootstrap.utils.BasicUtils;
import org.fusesource.jansi.Ansi;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandsManager {

    private static final Map<String, CommandConsumer> commands = new ConcurrentHashMap<>();

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
        commands.put("info", args -> {
            System.out.println();
            System.out.println(BasicUtils.bolding("MCBootstrap") + " by "  + BasicUtils.bolding("LITEFINE") + " v2.0-SNAPSHOT");
            System.out.println("Type 'help' or visit project wiki for more information");
            System.out.println();
            System.out.println(" Extensions loaded: " + ExtensionsManager.getExtensions().size());
            System.out.println(" Main folder: " + Settings.getDataFolder().getAbsolutePath());
            System.out.println(" Screens folder: " + Settings.getScreensFolder().getAbsolutePath());
            System.out.println();
            System.out.println(" Uptime: " + BasicUtils.millisToPattern(ManagementFactory.getRuntimeMXBean().getUptime()));
            System.out.println(" RAM usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
            if (ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage() > 0)
                System.out.println(" System average load: " + BasicUtils.round(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage(), 2));
            System.out.println();
        });
        commands.put("help", args -> {
            System.out.println();
            System.out.println(BasicUtils.bolding("MCBootstrap") + " by "  + BasicUtils.bolding("LITEFINE") + " v2.0-SNAPSHOT");
            System.out.println("Project wiki: " + BasicUtils.colorize("https://github.com/LITEFINE/MCBootstrap/wiki", Ansi.Color.YELLOW));
            System.out.println("> This is very simple and powerful standalone tool");
            System.out.println("for atomization and improvement of your network management");
            System.out.println();
            System.out.println(BasicUtils.bolding(" Existing commands:"));
            System.out.println(" 'info' - basic information about runtime");
            System.out.println(" 'help' - this page");
            System.out.println(" 'status' - basic information about MCBootstrap status");
            System.out.println(" 'status list' - all objects statuses");
            System.out.println(" 'status <objectName>' - object status");
            System.out.println(" 'start <all/objectName>' - start all or one specific object");
            System.out.println(" 'stop <all/objectName>' - stop all or one specific object");
            System.out.println(" 'extensions' - information about active extensions");
            System.out.println(" 'shutdown' - full and safe shutdown with all objects stopping");
            System.out.println(" 'systemexit' - quick and NOT SAFE shutdown");
            System.out.println();
        });
        commands.put("status", args -> {
            if (args.length == 0) {
                System.out.println();
                System.out.println(BasicUtils.bolding("Bootstrap status:"));
                System.out.println();
                System.out.println(" Loaded SERVER objects: " + BootingAPI.getBootingServers(false).count());
                System.out.println(" Loaded GROUP objects: " + BootingAPI.getBootingGroups().count());
                System.out.println(" Loaded PRIMARY_SERVER objects: " + BootingAPI.getPrimaryBootingServers().count());
                System.out.println(" Loaded APPLICATION objects: " + BootingAPI.getBootingApplications().count());
                System.out.println();
                System.out.println(" Total running servers: " + BootingAPI.getRunningServers(true).count());
                System.out.println(" Total running applications: " + BootingAPI.getRunningApplications().count());
                System.out.println();
            } else if (args[0].equalsIgnoreCase("list")) {
                System.out.println();
                System.out.println(BasicUtils.bolding("Bootstrap objects status list:"));
                System.out.println();
                System.out.println(" Booting servers: " + (BootingAPI.getBootingServers(false).count() == 0 ? "NOT FOUND" : ""));
                BootingAPI.getBootingServers(false).forEach(server -> System.out.println(" - " + (server.isBooted() ? BasicUtils.colorize(server.getName(), Ansi.Color.GREEN) : BasicUtils.colorize(server.getName(), Ansi.Color.DEFAULT))));
                System.out.println(" Booting groups: " + (BootingAPI.getBootingGroups().count() == 0 ? "NOT FOUND" : ""));
                BootingAPI.getBootingGroups().forEach(group -> System.out.println(" - " + group.getName() + " " + BasicUtils.getServersString(group)));
                System.out.println(" Primary booting servers: " + (BootingAPI.getPrimaryBootingServers().count() == 0 ? "NOT FOUND" : ""));
                BootingAPI.getPrimaryBootingServers().forEach(primaryServer -> System.out.println(" - " + primaryServer.getName() + " " + BasicUtils.getServersString(primaryServer)));
                System.out.println(" Booting applications: " + (BootingAPI.getBootingApplications().count() == 0 ? "NOT FOUND" : ""));
                BootingAPI.getBootingApplications().forEach(application -> System.out.println(" - " + (application.isBooted() ? BasicUtils.colorize(application.getName(), Ansi.Color.GREEN) : BasicUtils.colorize(application.getName(), Ansi.Color.DEFAULT))));
                System.out.println();
            } else {
                BootingObject object = BootingAPI.getBootingObjectByName(args[0], true).orElse(null);
                if (object != null) {
                    System.out.println();
                    System.out.println("Object '" + BasicUtils.colorize(object.getName(), Ansi.Color.YELLOW) + "' status:");
                    System.out.println();
                    System.out.println(" Object type: " + BasicUtils.bolding(object.getClass().getSimpleName()));
                    System.out.println(" Priority: " + object.getPriority());
                    System.out.println(" Directory: '" + object.getDirectory().getAbsolutePath() + "'");
                    System.out.println(" Has auto restart: " + (object.hasAutoRestartProperty() ? "TRUE" : "FALSE"));
                    System.out.println(" Start command: '" + object.getBootCommand() + "'");
                    System.out.println(" Stop command: " + (object.hasStopCommand() ? "'" + object.getStopCommand() + "'" : "NOT DEFINED"));
                    System.out.println();
                    if (object instanceof BootingApplication) {
                        BootingApplication application = (BootingApplication) object;
                        if (application.isBooted()) {
                            System.out.println(" Status: " + BasicUtils.colorize("BOOTED", Ansi.Color.GREEN));
                            System.out.println(" Active screen: '" + application.getScreenID() + "." + application.getScreenName() + "'");
                        } else {
                            System.out.println(" Status: " + BasicUtils.colorize("OFF", Ansi.Color.RED));
                            System.out.println(" Screen name on boot: '" + application.getScreenName() + "'");
                        }
                    }
                    if (object instanceof BootingServer) {
                        BootingServer server = (BootingServer) object;
                        System.out.println(" Parent: " + (server.hasParent() ? "'" + server.getParentObject().getName() + "'" : "NOT DEFINED"));
                        System.out.println(" Custom port: " + (server.hasCustomPort() ? server.getCustomPort() : "NOT DEFINED"));
                    } else if (object instanceof BootingGroup) {
                        BootingGroup group = (BootingGroup) object;
                        System.out.println(" First port: " + (group.hasFirstPort() ?group.getFirstPort() : "NOT DEFINED"));
                        System.out.println(" Child servers: " + (!group.getChildServers().isEmpty() ? "NOT FOUND" : ""));
                        group.getChildServers().forEach(server -> System.out.println(" - " + (server.isBooted() ? BasicUtils.colorize(server.getName(), Ansi.Color.GREEN) : BasicUtils.colorize(server.getName(), Ansi.Color.DEFAULT))));
                    } else if (object instanceof PrimaryBootingServer) {
                        PrimaryBootingServer primaryServer = (PrimaryBootingServer) object;
                        System.out.println(" Generation directory: '" + primaryServer.getGenerationDirectory().getAbsolutePath() + "'");
                        System.out.println(" Unique files policy: "
                                + (primaryServer.getUniqueFilesPolicy() == UniqueFilesPolicy.INORDER_POLICY ? "INORDER" : (primaryServer.getUniqueFilesPolicy() == UniqueFilesPolicy.RANDOM_POLICY ? "RANDOM" : "CUSTOM")));
                        System.out.println(" First port: " + primaryServer.getFirstPort());
                        System.out.println(" Copies count: " + primaryServer.getCopiesCount());
                        System.out.println(" Child cloned servers: " + (!primaryServer.getClonedServers().isEmpty() ? "NOT FOUND" : ""));
                        primaryServer.getClonedServers().forEach(server -> System.out.println(" - " + (server.isBooted() ? BasicUtils.colorize(server.getName(), Ansi.Color.GREEN) : BasicUtils.colorize(server.getName(), Ansi.Color.DEFAULT))));
                    }
                    System.out.println();
                }
                else System.out.println("Object with name '" + args[0] + "' not found!");
            }
        });
        commands.put("start", args -> {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("all")) BootingAPI.startAllObjects();
                else {
                    BootingObject object = BootingAPI.getBootingObjectByName(args[0], true).orElse(null);
                    if (object != null) {
                        if (object instanceof BootingServer) {
                            BootingServer server = (BootingServer) object;
                            if (!server.isBooted() && !server.isStarting()) object.bootObject();
                            else System.out.println("That server is already booted or under loading!");
                        } else if (object instanceof BootingApplication) {
                            BootingApplication application = (BootingApplication) object;
                            if (!application.isBooted() && !application.isStarting()) object.bootObject();
                            else System.out.println("That application is already booted or under loading!");
                        } else object.bootObject();
                    }
                    else System.out.println("Object with name '" + args[0] + "' not found!");
                }
            } else System.out.println("Usage: 'start <all|objectName>'");
        });
        commands.put("extensions", args -> {
            System.out.println();
            System.out.println(BasicUtils.bolding("Extensions manager information:"));
            if (ExtensionsManager.getExtensions().isEmpty()) System.out.println("> No loaded extensions foubd.");
            ExtensionsManager.getExtensions().forEach(extension -> System.out.println(" - " + BasicUtils.colorize(extension.getName(), Ansi.Color.GREEN) + " v. " + extension.getVersion() + " | Author " + extension.getAuthor()));
            System.out.println();
        });
        commands.put("stop", args -> {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("all")) BootingAPI.stopAllObjects();
                else {
                    BootingObject object = BootingAPI.getBootingObjectByName(args[0], true).orElse(null);
                    if (object != null) {
                        if (object instanceof BootingServer) {
                            BootingServer server = (BootingServer) object;
                            if (server.isBooted()) object.stopObject();
                            else System.out.println("That server isn't started!");
                        } else if (object instanceof BootingApplication) {
                            BootingApplication application = (BootingApplication) object;
                            if (application.isBooted()) object.stopObject();
                            else System.out.println("That application isn't started!");
                        } else object.stopObject();
                    }
                    else System.out.println("Object with name '" + args[0] + "' not found!");
                }
            } else System.out.println("Usage: 'stop <all|objectName>'");
        });
        commands.put("shutdown", args -> MCBootstrap.shutdown(true));
        commands.put("systemexit", args -> MCBootstrap.shutdown(false));
    }

    public static void registerCommand(String command, CommandConsumer consumer) {
        commands.put(command, consumer);
        MCBootstrap.getLogger().debug("Command registration: '" + command + "'");
    }

    public static void unregisterCommand(String command) {
        commands.remove(command);
        MCBootstrap.getLogger().debug("Command deregistration: '" + command + "'");
    }

    public static Map<String, CommandConsumer> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

}