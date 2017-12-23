package me.litefine.mcbootstrap.utils;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import me.litefine.mcbootstrap.main.Settings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * Created by LITEFINE IDEA on 02.12.17.
 */
public class YamlUtils {

    private static final YamlConfig yamlConfig = new YamlConfig();
    private static SoftReference<YamlReader> reader = new SoftReference<>(null);

    static {
        yamlConfig.setAllowDuplicates(false);
        yamlConfig.readConfig.setIgnoreUnknownProperties(true);
    }

    public static Object readObject() throws YamlException, FileNotFoundException {
        return YamlUtils.getReader().read();
    }

    public static <T> T readObject(Class<T> type) throws YamlException, FileNotFoundException {
        return YamlUtils.getReader().read(type);
    }

    public static boolean getBooleanValue(String path, Map source) {
        Object getted = getValue(path, source);
        return Boolean.valueOf(String.valueOf(getted));
    }

    public static String getStringValue(String path, Map source) {
        Object getted = getValue(path, source);
        return String.valueOf(getted);
    }

    public static long getLongValue(String path, Map source) {
        Object getted = getValue(path, source);
        return Long.parseLong(String.valueOf(getted));
    }

    public static Map getMapValue(String path, Map source) {
        Object getted = getValue(path, source);
        return (Map) getted;
    }

    public static Object getValue(String path, Map source) {
        String[] paths = path.split("\\.");
        for (int number = 0; number < paths.length; number++) {
            path = paths[number];
            if (number != paths.length-1 && source.get(path) instanceof Map)
                source = (Map) source.get(path);
        }
        return source.get(path);
    }

    private static YamlReader getReader() throws FileNotFoundException {
        if (reader.get() == null)
            reader = new SoftReference<>(new YamlReader(new FileReader(Settings.getConfigFile()), yamlConfig));
        return reader.get();
    }

}