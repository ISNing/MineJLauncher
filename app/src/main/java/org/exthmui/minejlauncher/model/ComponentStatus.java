package org.exthmui.minejlauncher.model;

public enum ComponentStatus {
    UNKNOWN,
    STARTING,
    DOWNLOADING,
    DOWNLOADED,
    PAUSED,
    PAUSED_ERROR,
    VERIFYING,
    VERIFIED,
    VERIFICATION_FAILED,
    INSTALLING,
    INSTALLED,
    INSTALLATION_FAILED;

    public static final class Persistent {
        public static final int UNKNOWN = 0;
        public static final int INCOMPLETE = 1;
        public static final int VERIFIED = 2;
    }
}
