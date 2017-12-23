package me.litefine.mcbootstrap.objects;

/**
 * Created by LITEFINE IDEA on 03.12.17.
 */
@FunctionalInterface
public interface CommandConsumer {

    void  execute(String[] args);

}