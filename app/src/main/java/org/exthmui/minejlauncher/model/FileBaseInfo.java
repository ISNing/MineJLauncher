package org.exthmui.minejlauncher.model;

public interface FileBaseInfo {
    String getName();// The name of the pack
    String getId();
    String getProductId();
    String getPath();// Path of files
    String getDisplayVersion();// Version of the pack
    long getVersion();// Version of the pack
    int getPackType();// What package is this? A Minecraft or a Forge or ...
    String getIntroduction();

    ComponentStatus getStatus();// The status of the package
}
