package me.litefine.mcbootstrap.objects.booting;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.objects.UniqueFilesPolicy;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by LITEFINE IDEA on 14.12.17.
 */
public class PrimaryBootingServer extends BootingObject {

    private final Supplier<List<BootingServer>> clonedServers = () ->
            Settings.getBootingServers().stream().filter(server -> server.getParent() == this).collect(Collectors.toList());

    private final UniqueFilesPolicy uniqueFilesPolicy;
    private final File generationDirectory, uniqueFiles;
    private final int copiesCount, firstPort;

    PrimaryBootingServer(String name, Map<String, String> properties) {
        super(name, properties);
        copiesCount = Integer.parseInt(properties.get("copiesCount"));
        firstPort = Integer.parseInt(properties.get("firstPort"));
        generationDirectory = new File(properties.get("generationDirectory"));
        uniqueFiles = new File(directory.getAbsolutePath() + "/_unique_/");
        uniqueFilesPolicy = Settings.getUniqueFilesPolicy(properties.get("uniqueFilesPolicy"));
        for (int number = 1; number <= copiesCount; number++) {
            File destination = new File(generationDirectory.getAbsolutePath() + "/" + name + "-" + number);
            try {
                new BootingServer(this, destination.getName(), destination);
            } catch (Exception ex) {
                MCBootstrap.getLogger().warn("Can't load server '" + destination.getName() + "' in primary '" + name + "' - " +  ex.getMessage());
            }
        }
    }

    @Override
    public void bootObject() {
        MCBootstrap.getLogger().info("Launch primary server '" + name + "', copies: " + copiesCount);
        if (generationDirectory.mkdirs()) MCBootstrap.getLogger().debug("Servers generation folder created " + generationDirectory.getAbsolutePath());
        clonedServers.get().forEach(bootingServer -> {
            if (!bootingServer.isBooted()) {
                long startTime = System.currentTimeMillis();
                bootingServer.bootObject();
                long pause = Settings.getStartDelay() * 1000L - (System.currentTimeMillis() - startTime);
                if (pause > 0 && clonedServers.get().indexOf(bootingServer) != clonedServers.get().size()-1) {
                    try {
                        MCBootstrap.getLogger().info("Waiting for " + pause + " ms (delay)...");
                        Thread.sleep(pause);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }

    @Override
    public void stopObject() {
        MCBootstrap.getLogger().info("Stopping primary server '" + name + "', copies: " + copiesCount);
        clonedServers.get().forEach(bootingServer -> {
            if (bootingServer.isBooted())
                bootingServer.stopObject();
        });
    }

    public File getGenerationDirectory() {
        return generationDirectory;
    }

    public File getUniqueFiles() {
        return uniqueFiles;
    }

    public int getCopiesCount() {
        return copiesCount;
    }

    public int getFirstPort() {
        return firstPort;
    }

    public List<BootingServer> getClonedServers() {
        return clonedServers.get();
    }

    void clonePrimaryDirectory(BootingServer forObject) throws IOException {
        MCBootstrap.getLogger().info("Generation of clone clone of '" + name + "' primary for server '" + forObject.name + "' in " + forObject.directory.getAbsolutePath());
        long time = System.currentTimeMillis();
        File uniqueFilesFrom = uniqueFilesPolicy.getNextUniqueFolder(this, forObject);
        Function<Path, Path> relationMaker = path ->
                Paths.get(forObject.directory.getAbsolutePath() + "/" + directory.toPath().getParent().relativize(path));
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.equals(uniqueFiles.toPath())) return FileVisitResult.SKIP_SUBTREE;
                else {
                    Files.copy(dir, relationMaker.apply(dir));
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, relationMaker.apply(file));
                return FileVisitResult.CONTINUE;
            }

        });
        if (uniqueFilesFrom != null) {
            if (uniqueFilesFrom.exists()) {
                Files.walkFileTree(uniqueFilesFrom.toPath(), new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Files.copy(dir, relationMaker.apply(dir));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, relationMaker.apply(file));
                        return FileVisitResult.CONTINUE;
                    }

                });
            } else MCBootstrap.getLogger().debug("Unique files directory " + uniqueFilesFrom.getAbsolutePath() + " does not exist!");
        }
        MCBootstrap.getLogger().info("The clone '" + forObject.name + "' generated in " + (System.currentTimeMillis() - time) + " ms!");
    }

}