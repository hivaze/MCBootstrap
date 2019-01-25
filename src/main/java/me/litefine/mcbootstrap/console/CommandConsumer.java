package me.litefine.mcbootstrap.console;

@FunctionalInterface
public interface CommandConsumer {

    void  execute(String[] args);

}