package org.exthmui.minejlauncher.model;

import java.io.File;
import java.util.List;

public interface MinecraftInfo extends FileBaseInfo {
    String getType();// Minecraft release type
    String getId();
    String getPath();
    File getJar();
    File getJson();
    String getJREId();
    List<String> getClassPaths();
    List<String> getLunchArgs();
}
