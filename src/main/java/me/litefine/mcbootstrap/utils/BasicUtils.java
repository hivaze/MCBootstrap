package me.litefine.mcbootstrap.utils;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.objects.booting.BootingServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by LITEFINE IDEA on 03.12.17.
 */
public class BasicUtils {

    public static final Random RANDOM = new Random();

    public static void deleteDirectory(File directory, boolean justClear) throws IOException {
        MCBootstrap.getLogger().debug("Directory removal " + directory.getAbsolutePath());
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (directory.toPath().equals(dir) && !justClear) Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }

        });
    }

    public static String millisToPattern(long millis) {
        Duration dur = Duration.ofMillis(millis);
        long hoursDur   = dur.toHours();
        long minutesDur = dur.minusHours(hoursDur).toMinutes();
        long secondsDur = dur.minusHours(hoursDur).minusMinutes(minutesDur).getSeconds();
        return String.format("%03d:%02d:%02d", hoursDur, minutesDur, secondsDur);
    }

    public static double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++) pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5d ? tmp + 1 : tmp) / pow;
    }

    public static String join(Collection collection, String separator) {
        StringBuilder builder = new StringBuilder();
        for (Iterator iterator = collection.iterator(); iterator.hasNext(); builder.append(iterator.next())) {
            if (builder.length() != 0) builder.append(separator);
        }
        return builder.toString();
    }

    public static String getScreenNameFor(BootingServer object) {
        String pattern = Settings.getScreenNamePattern().trim();
        if (!pattern.isEmpty()) {
            pattern = pattern.replace("%name%", object.getName());
            pattern = pattern.replace("%priority%", object.getPriority().name());
        } else pattern = "MCB-" + object.getName();
        return pattern;
    }

    public static String removeExtraSpaces(String string) {
        return string.trim().replaceAll("\\s+", " ");
    }

    public static String[] getArguments(String string) {
        String[] split = string.split("\\s");
        return Arrays.copyOfRange(split, 1, split.length);
    }

    public static void copyResourceFile(File into) throws IOException {
        InputStreamReader localConfig = new InputStreamReader(MCBootstrap.class.getResourceAsStream("/" + into.getName()));
        FileWriter fw = new FileWriter(into);
        while (localConfig.ready()) fw.write(localConfig.read());
        localConfig.close(); fw.close();
    }

}