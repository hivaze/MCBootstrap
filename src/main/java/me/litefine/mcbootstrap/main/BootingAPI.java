package me.litefine.mcbootstrap.main;

import me.litefine.mcbootstrap.objects.booting.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BootingAPI {

    private static final List<BootingObject> bootingObjects = new ArrayList<>();

    public static synchronized void startAllObjects() {
        final int[] count = {0};
        MCBootstrap.getLogger().info("Booting all booting objects...");
        bootingObjects.forEach(bootingObject -> {
            if (!(bootingObject instanceof BootingServer && ((BootingServer) bootingObject).isBooted())) {
                bootingObject.bootObject();
                if (Settings.getStartDelay() > 0 && bootingObjects.indexOf(bootingObject) != bootingObjects.size() - 1) {
                    try {
                        MCBootstrap.getLogger().info("Waiting for " + Settings.getStartDelay() * 1000L + " ms (delay)...");
                        Thread.sleep(Settings.getStartDelay() * 1000L);
                    } catch (InterruptedException ignored) {}
                }
                count[0]++;
            }
        });
        MCBootstrap.getLogger().info(count[0] + " objects started");
    }

    public static synchronized void stopAllObjects() {
        MCBootstrap.getLogger().info("Stopping all booting objects (" + bootingObjects.size() + ")...");
        if (Settings.reverseOrderOnStop()) {
            bootingObjects.stream()
                    .sorted(Comparator.comparingInt(object -> object.getPriority().getPoints()))
                    .collect(Collectors.toList()).forEach(BootingObject::stopObject);
        } else bootingObjects.forEach(BootingObject::stopObject);
    }

    public static synchronized List<BootingObject> getBootingObjects() {
        return bootingObjects;
    }

    public static synchronized Optional<BootingObject> getBootingObjectByName(String name, boolean includeChildes) {
        Stream<BootingObject> stream = bootingObjects.stream();
        if (includeChildes) {
            stream = Stream.concat(stream, getBootingGroups().map(BootingGroup::getChildServers).map(List::toArray).map(BootingObject.class::cast));
            stream = Stream.concat(stream, getPrimaryBootingServers().map(PrimaryBootingServer::getClonedServers).map(List::toArray).map(BootingObject.class::cast));
        }
        return stream.filter(bootingObject -> bootingObject.getName().equals(name)).findFirst();
    }

    public static synchronized Stream<BootingServer> getBootingServers(boolean includeChildes) {
        Stream<BootingServer> serverStream = bootingObjects.stream().filter(BootingServer.class::isInstance).map(BootingServer.class::cast);
        if (includeChildes) {
            serverStream = Stream.concat(serverStream, getBootingGroups().map(BootingGroup::getChildServers).map(List::toArray).map(BootingServer.class::cast));
            serverStream = Stream.concat(serverStream, getPrimaryBootingServers().map(PrimaryBootingServer::getClonedServers).map(List::toArray).map(BootingServer.class::cast));
        }
        return serverStream;
    }

    public static synchronized Stream<BootingServer> getRunningServers(boolean includeChilds) {
        return getBootingServers(includeChilds).filter(BootingServer::isBooted);
    }

    public static synchronized Optional<BootingServer> getServerByScreenName(String screenName, boolean includeChilds) {
        return getBootingServers(includeChilds).filter(server -> server.getScreenName().equals(screenName)).findFirst();
    }

    public static synchronized Stream<BootingGroup> getBootingGroups() {
        return bootingObjects.stream().filter(BootingGroup.class::isInstance).map(BootingGroup.class::cast);
    }

    public static synchronized Stream<PrimaryBootingServer> getPrimaryBootingServers() {
        return bootingObjects.stream().filter(PrimaryBootingServer.class::isInstance).map(PrimaryBootingServer.class::cast);
    }

    public static synchronized Stream<BootingApplication> getBootingApplications() {
        return bootingObjects.stream().filter(obj -> obj.getClass() == BootingApplication.class).map(BootingApplication.class::cast);
    }

    public static synchronized Optional<BootingApplication> getApplicationByScreenName(String screenName) {
        return getBootingApplications().filter(app -> app.getScreenName().equals(screenName)).findFirst();
    }

    public static synchronized Stream<BootingApplication> getRunningApplications() {
        return getBootingApplications().filter(BootingApplication::isBooted);
    }

}