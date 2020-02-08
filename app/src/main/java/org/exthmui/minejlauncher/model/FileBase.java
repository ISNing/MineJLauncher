package org.exthmui.minejlauncher.model;

import org.exthmui.minejlauncher.controller.ComponentController;

import java.util.List;

public class FileBase implements FileBaseInfo {
    private String mName;
    private String mId;
    private String mProductId;
    private String mPath;
    private String mDisplayVersion;
    private long mVersion;
    private int mPackType;
    private ComponentStatus mStatus = ComponentStatus.UNKNOWN;
    private String mIntroduction;

    public FileBase() {
        super();
    }

    public FileBase(FileBaseInfo file) {
        super();
        this.mName = file.getName();
        this.mId = file.getId();
        this.mPath = file.getPath();
        this.mDisplayVersion = file.getDisplayVersion();
        this.mVersion = file.getVersion();
        this.mPackType = file.getPackType();
        this.mIntroduction = file.getIntroduction();
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setProductId(String productId) {
        this.mProductId = productId;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public void setDisplayVersion(String displayVersion) {
        this.mDisplayVersion = displayVersion;
    }

    public void setVersion(long version) {
        this.mVersion = version;
    }

    public void setPackType(int packType) {
        this.mPackType = packType;
    }

    public void setStatus(ComponentStatus status) {
        this.mStatus = status;
    }

    public void setIntroduction(String introduction){
        this.mIntroduction = introduction;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getProductId() {
        return mProductId;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public String getDisplayVersion() {
        return mDisplayVersion;
    }

    @Override
    public long getVersion() {
        return mVersion;
    }

    @Override
    public int getPackType() {
        return mPackType;
    }

    @Override
    public ComponentStatus getStatus() {
        return mStatus;
    }

    @Override
    public String getIntroduction() {
        return mIntroduction;
    }
}
