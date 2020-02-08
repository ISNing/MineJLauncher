/*
 * Copyright (C) 2020 The exTHmUI Project, Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exthmui.minejlauncher.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.exthmui.minejlauncher.Constants;
import org.exthmui.minejlauncher.misc.FileUtils;
import org.exthmui.minejlauncher.misc.Utils;
import org.exthmui.minejlauncher.misc.ZipUtil;
import org.exthmui.minejlauncher.model.ComponentInfo;
import org.exthmui.minejlauncher.model.ComponentStatus;

import java.io.File;
import java.io.IOException;

class ComponentInstaller {

    private static final String TAG = "ComponentInstaller";

    private static ComponentInstaller sInstance = null;
    private static String sInstallingComponent = null;

    private Thread mPrepareInstallThread;
    private volatile boolean mCanCancel;

    private final Context mContext;
    private final ComponentController mComponentController;

    private ComponentInstaller(Context context, ComponentController controller) {
        mContext = context.getApplicationContext();
        mComponentController = controller;
    }

    static synchronized ComponentInstaller getInstance(Context context,
                                                    ComponentController ComponentController) {
        if (sInstance == null) {
            sInstance = new ComponentInstaller(context, ComponentController);
        }
        return sInstance;
    }

    static synchronized boolean isInstalling() {
        return sInstallingComponent != null;
    }

    static synchronized boolean isInstalling(String id) {
        return sInstallingComponent != null && sInstallingComponent.equals(id);
    }

    void install(String id) {
        if (isInstalling()) {
            Log.e(TAG, "Already installing an component");
            return;
        }

        ComponentInfo component = mComponentController.getComponent(id);
        /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        preferences.edit()
                .putLong(Constants.PREF_INSTALL_OLD_VERSION, mComponentController.getSameProductComponent(mComponentController.getComponent(id).getProductId()).getVersion())
                .putLong(Constants.PREF_INSTALL_NEW_VERSION, component.getVersion())
                .putString(Constants.PREF_INSTALL_PACKAGE_PATH, component.getFile().getAbsolutePath())
                //.putBoolean(Constants.PREF_INSTALL_AGAIN, isReinstalling)
                .putBoolean(Constants.PREF_INSTALL_NOTIFIED, false)
                .apply();*/

        if (mComponentController.isInstalled(mComponentController.getSameProductComponent(mComponentController.getComponent(id).getProductId()).getId())) {
            // remove old version of the same product if install
            this.removePackage(mComponentController.getSameProductComponent(mComponentController.getComponent(id).getProductId()).getId());
            install(id);
        } else {
            installPackage(component.getFile(), id);
        }
    }

    void remove(String id) {
        if (isInstalling()) {
            Log.e(TAG, "Already installing an component");
            return;
        }
        if (mComponentController.isInstalled(id)) {
            Log.e(TAG, "The component has not installed");
            return;
        }
        this.removePackage(id);
    }

    private void installPackage(File file, String id) {
        sInstallingComponent = id;
        try {
            ZipUtil.UnZipFolder(file.getAbsolutePath(),
                    mContext.getFilesDir() + "/" + mComponentController.getActualComponent(id).getId());
            mComponentController.getActualComponent(id).setPath(
                    mContext.getFilesDir() + "/" + mComponentController.getActualComponent(id).getId());
            mComponentController.getActualComponent(id).setStatus(ComponentStatus.INSTALLED);
        } catch (Exception e) {
            Log.e(TAG, "Could not install component", e);
            mComponentController.getActualComponent(id)
                    .setStatus(ComponentStatus.INSTALLATION_FAILED);
            mComponentController.notifyComponentChange(id);
        }
    }

    private void removePackage(String id) {
        sInstallingComponent = id;
        try {
            new File(mComponentController.getComponent(id).getPath()).delete();
        } catch (Exception e) {
            Log.e(TAG, "Could not remove component", e);
            mComponentController.getActualComponent(id).setStatus(ComponentStatus.INSTALLED);
        }
    }
    /*private synchronized void prepareForUncryptAndInstall(ComponentInfo component) {
        String filePath = component.getFile().getAbsolutePath();
        File uncryptFile = new File(filePath);

        Runnable copyComponentRunnable = new Runnable() {
            private long mLastComponent = -1;

            FileUtils.ProgressCallBack mProgressCallBack = new FileUtils.ProgressCallBack() {
                @Override
                public void update(int progress) {
                    long now = SystemClock.elapsedRealtime();
                    if (mLastComponent < 0 || now - mLastComponent > 500) {
                        mComponentController.getActualComponent(component.getId())
                                .setInstallProgress(progress);
                        mComponentController.notifyInstallProgress(component.getId());
                        mLastComponent = now;
                    }
                }
            };

            @Override
            public void run() {
                try {
                    mCanCancel = true;
                    FileUtils.copyFile(component.getFile(), uncryptFile, mProgressCallBack);
                    mCanCancel = false;
                    if (mPrepareInstallThread.isInterrupted()) {
                        mComponentController.getActualComponent(component.getId())
                                .setStatus(ComponentStatus.INSTALLATION_CANCELLED);
                        mComponentController.getActualComponent(component.getId())
                                .setInstallProgress(0);
                        uncryptFile.delete();
                    } else {
                        installPackage(uncryptFile, component.getId());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Could not copy component", e);
                    uncryptFile.delete();
                    mComponentController.getActualComponent(component.getId())
                            .setStatus(ComponentStatus.INSTALLATION_FAILED);
                } finally {
                    synchronized (ComponentInstaller.this) {
                        mCanCancel = false;
                        mPrepareInstallThread = null;
                        sInstallingComponent = null;
                    }
                    mComponentController.notifyComponentChange(component.getId());
                }
            }
        };

        mPrepareInstallThread = new Thread(copyComponentRunnable);
        mPrepareInstallThread.start();
        sInstallingComponent = component.getId();
        mCanCancel = false;

        mComponentController.getActualComponent(component.getId())
                .setStatus(ComponentStatus.INSTALLING);
        mComponentController.notifyComponentChange(component.getId());
    }*/

    public synchronized void cancel() {
        if (!mCanCancel) {
            Log.d(TAG, "Nothing to cancel");
            return;
        }
        mPrepareInstallThread.interrupt();
    }
}
