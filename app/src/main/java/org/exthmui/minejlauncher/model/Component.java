package org.exthmui.minejlauncher.model;

import java.io.File;

public class Component extends FileBase implements ComponentInfo {
    private int mPersistentStatus = ComponentStatus.Persistent.UNKNOWN;
    private File mFile;
    private long mFileSize;
    private String mUrl;
    private int mProgress;
    private long mEta;
    private long mSpeed;
    private int mInstallProgress;
    private boolean mAvailableOnline;
    private boolean mIsFinalizing;

    public Component() {
    }

    public Component(FileBaseInfo fileBase) {
        super(fileBase);
    }

    public Component(ComponentInfo component) {
        super(component);
        mPersistentStatus = component.getPersistentStatus();
        mFile = component.getFile();
        mFileSize = component.getFileSize();
        mUrl = component.getDownloadUrl();
        mProgress = component.getProgress();
        mEta = component.getEta();
        mSpeed = component.getSpeed();
        mInstallProgress = component.getInstallProgress();
        mAvailableOnline = component.getAvailableOnline();
        mIsFinalizing = component.getFinalizing();
    }

    @Override
    public int getPersistentStatus() {
        return mPersistentStatus;
    }

    public void setPersistentStatus(int status) {
        mPersistentStatus = status;
    }

    @Override
    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        mFile = file;
    }

    @Override
    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long mFileSize) {
        this.mFileSize = mFileSize;
    }

    @Override
    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    @Override
    public long getEta() {
        return mEta;
    }

    public void setEta(long eta) {
        mEta = eta;
    }

    @Override
    public long getSpeed() {
        return mSpeed;
    }

    public void setSpeed(long speed) {
        mSpeed = speed;
    }

    @Override
    public int getInstallProgress() {
        return mInstallProgress;
    }

    public void setInstallProgress(int progress) {
        mInstallProgress = progress;
    }

    @Override
    public boolean getAvailableOnline() {
        return mAvailableOnline;
    }

    public void setAvailableOnline(boolean availableOnline) {
        mAvailableOnline = availableOnline;
    }

    @Override
    public boolean getFinalizing() {
        return mIsFinalizing;
    }

    public void setFinalizing(boolean finalizing) {
        mIsFinalizing = finalizing;
    }

    @Override
    public String getDownloadUrl() {
        return mUrl;
    }

    public void setDownloadUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
