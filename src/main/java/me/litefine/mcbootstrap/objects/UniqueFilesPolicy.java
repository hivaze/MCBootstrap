package me.litefine.mcbootstrap.objects;

import me.litefine.mcbootstrap.objects.booting.BootingServer;
import me.litefine.mcbootstrap.objects.booting.PrimaryBootingServer;

import java.io.File;

/**
 * Created by LITEFINE IDEA on 21.12.17.
 */
public interface UniqueFilesPolicy {

    File getNextUniqueFolder(PrimaryBootingServer primaryBootingServer, BootingServer forObject);

}