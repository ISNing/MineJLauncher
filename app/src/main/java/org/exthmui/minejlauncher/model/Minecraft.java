package org.exthmui.minejlauncher.model;

import java.io.File;
import java.util.List;

public class Minecraft extends FileBase implements MinecraftInfo {
    private String mType;
    private String mPath;
    private File mJar;
    private File mJson;
    private String mJREId;
    private List<String> mClassPaths;
    private List<String> mLunchArgs;

    public void setType(String mType) {
        this.mType = mType;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public void setJar(File mJar) {
        this.mJar = mJar;
    }

    public void setJson(File mJson) {
        this.mJson = mJson;
    }

    public void setJREId(String mJREId) {
        this.mJREId = mJREId;
    }

    public void setClassPaths(List<String> mClassPaths) {
        this.mClassPaths = mClassPaths;
    }

    public void setLunchArgs(List<String> mLunchArgs) {
        this.mLunchArgs = mLunchArgs;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public File getJar() {
        return mJar;
    }

    @Override
    public File getJson() {
        return mJson;
    }

    @Override
    public String getJREId() {
        return mJREId;
    }

    @Override
    public List<String> getClassPaths() {
        return mClassPaths;
    }

    @Override
    public List<String> getLunchArgs() {
        return mLunchArgs;
    }
}
