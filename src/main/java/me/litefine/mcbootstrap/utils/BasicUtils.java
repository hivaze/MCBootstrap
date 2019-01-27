package me.litefine.mcbootstrap.utils;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;
import me.litefine.mcbootstrap.objects.booting.BootingApplication;
import me.litefine.mcbootstrap.objects.booting.ParenthoodObject;
import org.fusesource.jansi.Ansi;

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

public class BasicUtils {

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
                if (!directory.toPath().equals(dir) || !justClear) Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }

        });
    }

    public static Ansi colorize(String string, Ansi.Color color) {
        return Ansi.ansi().fgBright(color).a(string).reset();
    }

    public static Ansi bolding(String string) {
        return Ansi.ansi().bold().a(string).reset();
    }

    public static String getServersString(ParenthoodObject group) {
        if (!group.getChildServers().isEmpty()) {
            StringBuilder builder = new StringBuilder("(");
            group.getChildServers().forEach(bootingServer -> {
                if (bootingServer.isBooted()) builder.append(colorize(bootingServer.getName(), Ansi.Color.GREEN));
                else builder.append(bootingServer.getName());
                builder.append(", ");
            });
            return builder.toString().substring(0, builder.length() - 2).concat(")");
        } else return "[]";
    }

    public static String millisToPattern(long millis) {
        Duration dur = Duration.ofMillis(millis);
        long daysDur = dur.toDays();
        long hoursDur = dur.minusDays(daysDur).toHours();
        long minutesDur = dur.minusDays(daysDur).minusHours(hoursDur).toMinutes();
        long secondsDur = dur.minusDays(daysDur).minusHours(hoursDur).minusMinutes(minutesDur).getSeconds();
        return String.format("%02dd %02dh %02dm %02ds", daysDur, hoursDur, minutesDur, secondsDur);
    }

    public static double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++) pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5d ? tmp + 1 : tmp) / pow;
    }

    public static String getScreenNameFor(BootingApplication object) {
        String pattern = Settings.getScreenNamePattern().trim();
        if (!pattern.isEmpty()) {
            pattern = pattern.replace("%name%", object.getName());
            pattern = pattern.replace("%priority%", object.getPriority().name());
        } else pattern = "MCB-" + object.getName();
        return pattern;
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