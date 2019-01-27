package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.extensions.ExtensionsManager;
import me.litefine.mcbootstrap.main.BootingAPI;
import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.objects.UniqueFilesPolicy;
import me.litefine.mcbootstrap.utils.BasicUtils;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PrimaryBootingServer extends BootingObject implements ParenthoodObject {

    private final List<BootingServer> clonedChildServers = new ArrayList<>();

    private UniqueFilesPolicy uniqueFilesPolicy;
    private final File generationDirectory, uniqueFilesFolder = new File(directory.getAbsolutePath() + "/_unique_/");
    private final int clonesCount, firstPort;

    PrimaryBootingServer(String name, Map<String, String> properties) {
        super(name, properties);
        clonesCount = Integer.parseInt(properties.get("clonesCount"));
        firstPort = Integer.parseInt(properties.get("firstPort"));
        generationDirectory = new File(properties.get("generationDirectory"));
        uniqueFilesPolicy = UniqueFilesPolicy.getUniqueFilesPolicy(properties.get("uniqueFilesPolicy"));
        for (int number = 1; number <= clonesCount; number++) {
            File destinationDir = new File(generationDirectory.getAbsolutePath() + "/" + name + "-" + number);
            try {
                clonedChildServers.add(new BootingServer(this, destinationDir.getName(), destinationDir));
            } catch (Exception ex) {
                MCBootstrap.getLogger().warn("Can't create '" + BasicUtils.colorize(destinationDir.getName(), Ansi.Color.YELLOW)  + "' server for primary server '" + BasicUtils.colorize(name, Ansi.Color.YELLOW)  + "' - " + ex);
            }
        }
        BootingAPI.getBootingObjects().add(this);
        ExtensionsManager.getExtensions().forEach(extension -> extension.executor().submit(() -> extension.onBootingObjectAfterLoad(this)));
    }

    @Override
    public synchronized void bootObject() {
        MCBootstrap.getLogger().info("Launch primary server '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "', clones count: " + clonesCount);
        if (generationDirectory.mkdirs()) MCBootstrap.getLogger().debug("Servers generation folder created " + generationDirectory.getAbsolutePath());
        clonedChildServers.forEach(bootingServer -> {
            if (!bootingServer.isBooted() && !bootingServer.isStarting()) {
                long startTime = System.currentTimeMillis();
                bootingServer.bootObject();
                long pause = Settings.getStartDelay() * 1000L - (System.currentTimeMillis() - startTime);
                if (pause > 0 && clonedChildServers.indexOf(bootingServer) != clonedChildServers.size()-1) {
                    try {
                        MCBootstrap.getLogger().info("Waiting for " + pause + " ms (delay)...");
                        Thread.sleep(pause);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    @Override
    public synchronized void stopObject() {
        MCBootstrap.getLogger().info("Stopping clones of primary server '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "', copies: " + clonesCount);
        clonedChildServers.forEach(childStopper);
    }

    public File getGenerationDirectory() {
        return generationDirectory;
    }

    public File getUniqueFilesFolder() {
        return uniqueFilesFolder;
    }

    public synchronized void setUniqueFilesPolicy(UniqueFilesPolicy uniqueFilesPolicy) {
        this.uniqueFilesPolicy = uniqueFilesPolicy;
    }

    public synchronized UniqueFilesPolicy getUniqueFilesPolicy() {
        return uniqueFilesPolicy;
    }

    public int getClonesCount() {
        return clonesCount;
    }

    public int getFirstPort() {
        return firstPort;
    }

    @Override
    public List<BootingServer> getChildServers() {
        return Collections.unmodifiableList(clonedChildServers);
    }

    void clonePrimaryDirectory(BootingServer forObject) throws IOException {
        MCBootstrap.getLogger().info("Generation clone of '" + BasicUtils.colorize(name, Ansi.Color.YELLOW) + "' primary for server '" + BasicUtils.colorize(forObject.name, Ansi.Color.YELLOW) + "' in " + forObject.directory.getAbsolutePath());
        long time = System.currentTimeMillis();
        Function<Path, Path> basicRelationMaker = path -> Paths.get(forObject.directory.getAbsolutePath() + "/" + directory.toPath().relativize(path));
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.equals(uniqueFilesFolder.toPath())) return FileVisitResult.SKIP_SUBTREE;
                else {
                    Files.copy(dir, basicRelationMaker.apply(dir));
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, basicRelationMaker.apply(file));
                return FileVisitResult.CONTINUE;
            }

        });
        if (uniqueFilesPolicy != null) {
            if (uniqueFilesFolder.exists() && uniqueFilesFolder.listFiles() != null) {
                File uniqueFilesFrom = uniqueFilesPolicy.getUniqueFolder(this, forObject);
                Function<Path, Path> uniqueRelationMaker = path -> Paths.get(forObject.directory.getAbsolutePath() + "/" + uniqueFilesFrom.toPath().relativize(path));
                MCBootstrap.getLogger().info("Using unique files from " + uniqueFilesFrom.getAbsolutePath() + " for '" + BasicUtils.colorize(forObject.name, Ansi.Color.YELLOW) + "' clone");
                Files.walkFileTree(uniqueFilesFrom.toPath(), new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (dir != uniqueFilesFrom.toPath()) Files.copy(dir, uniqueRelationMaker.apply(dir));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, uniqueRelationMaker.apply(file));
                        return FileVisitResult.CONTINUE;
                    }

                });
            } else MCBootstrap.getLogger().debug("Unique files directory " + uniqueFilesFolder.getAbsolutePath() + " does not exist!");
        }
        MCBootstrap.getLogger().info("The clone '" + BasicUtils.colorize(forObject.name, Ansi.Color.YELLOW) + "' generated in " + (System.currentTimeMillis() - time) + " ms!");
    }

}