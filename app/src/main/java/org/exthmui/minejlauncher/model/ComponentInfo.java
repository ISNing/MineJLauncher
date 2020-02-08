package org.exthmui.minejlauncher.model;

import java.io.File;

public interface ComponentInfo extends FileBaseInfo {
    String getDownloadUrl();// URL of the file
    int getPersistentStatus();
    File getFile();
    long getFileSize();
    int getProgress();
    long getEta();
    long getSpeed();
    int getInstallProgress();
    boolean getAvailableOnline();
    boolean getFinalizing();
}
