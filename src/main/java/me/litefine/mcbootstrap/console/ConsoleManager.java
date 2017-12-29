package me.litefine.mcbootstrap.console;

import jline.console.ConsoleReader;
import me.litefine.mcbootstrap.main.MCBootstrap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.IOException;

/**
 * Created by LITEFINE IDEA on 27.11.17.
 */
public class ConsoleManager {

    private static ConsoleReader reader;
    private static Thread consoleThread;

    public static void setup() throws IOException {
        reader = new ConsoleReader(System.in, System.out);
        reader.setExpandEvents(false);
        CommandsManager.registerCommands();
        consoleThread = new Thread(() -> {
            while (true) {
                try {
                    String command = reader.readLine();
                    CommandsManager.execureCommand(command);
                } catch (Exception e) {
                    MCBootstrap.getLogger().error("An error in console reader - " + e.getMessage());
                }
            }
        }, "Console Thread");
        System.setOut(IoBuilder.forLogger(MCBootstrap.getLogger()).setLevel(Level.INFO).buildPrintStream());
    }

    public static ConsoleReader getReader() {
        return reader;
    }

    public static Thread getConsoleThread() {
        return consoleThread;
    }

}