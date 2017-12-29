package me.litefine.mcbootstrap.extensions;

import me.litefine.mcbootstrap.main.MCBootstrap;
import me.litefine.mcbootstrap.main.Settings;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Created by LITEFINE IDEA on 29.12.2017.
 */
public class ExtensionsManager {

    private static final List<Extension> extensions = new ArrayList<>();

    public static void loadExtensions() {
        for (File file : Settings.getExtensionsFolder().listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    MCBootstrap.getLogger().info("Loading extension " + file.getName() + "...");
                    JarFile jarFile = new JarFile(file);
                    ZipEntry zipEntry = jarFile.getEntry("extension.info");
                    if (zipEntry != null) {
                        Properties properties = new Properties();
                        properties.load(jarFile.getInputStream(zipEntry));
                        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
                        Class<? extends Extension> extensionClass = Class.forName(properties.getProperty("mainClass"), true, loader).asSubclass(Extension.class);
                        Extension extension = extensionClass.newInstance();
                        extension.name = properties.getProperty("name");
                        extension.author = properties.getProperty("author");
                        extension.version = properties.getProperty("version");
                        extensions.add(extension);
                        MCBootstrap.getLogger().info("Extension " + extension.name + " v. " + extension.version + " of " + extension.author + " loaded into the system!");
                    } else MCBootstrap.getLogger().warn("Extension " + file.getName() + " doesn't contains 'extension.info' file!");
                } catch (NullPointerException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    MCBootstrap.getLogger().warn("Can't load extension file " + file.getName() + " - " + e.getMessage());
                }
            }
        }
    }

    public static void disableExtensions() {
        extensions.forEach(extension -> extension.getExecutorService().shutdown());
    }

    public static List<Extension> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

}