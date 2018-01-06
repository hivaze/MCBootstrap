package me.litefine.mcbootstrap.console;

/**
 * Created by LITEFINE IDEA on 03.12.17.
 */
@FunctionalInterface
public interface CommandConsumer {

    void  execute(String[] args);

}