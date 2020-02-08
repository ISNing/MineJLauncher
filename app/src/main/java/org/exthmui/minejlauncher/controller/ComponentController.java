package org.exthmui.minejlauncher.controller;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.exthmui.minejlauncher.MyApplication;
import org.exthmui.minejlauncher.R;
import org.exthmui.minejlauncher.download.DownloadClient;
import org.exthmui.minejlauncher.misc.Utils;
import org.exthmui.minejlauncher.model.Component;
import org.exthmui.minejlauncher.model.ComponentInfo;
import org.exthmui.minejlauncher.model.ComponentStatus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentController {

    public static final String ACTION_DOWNLOAD_PROGRESS = "action_download_progress";
    public static final String ACTION_INSTALL_PROGRESS = "action_install_progress";
    public static final String ACTION_COMPONENT_DELETED = "action_component_deleted";
    public static final String ACTION_COMPONENT_STATUS = "action_component_status_change";
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";

    private final String TAG = "ComponentController";

    private static ComponentController sDownloadController;

    private static final int MAX_REPORT_INTERVAL_MS = 1000;

    private final Context mContext;
    private final LocalBroadcastManager mBroadcastManager;
    private final ComponentsDbHelper mComponentsDbHelper;
    private final WakeLock mWakeLock;

    private final File mDownloadRoot;

    private int mActiveDownloads = 0;
    private Set<String> mVerifyingComponents = new HashSet<>();

    public static synchronized ComponentController getInstanceReceiver(Context context) {
        return ComponentController.getInstance(context);
    }
    public static synchronized ComponentController getInstance() {
        return sDownloadController;
    }

    protected static synchronized ComponentController getInstance(Context context) {
        if (sDownloadController == null) {
            sDownloadController = new ComponentController(context);
        }
        return sDownloadController;
    }

    private ComponentController(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mComponentsDbHelper = new ComponentsDbHelper(context);
        mDownloadRoot = Utils.getDownloadPath(context);
        mContext = context.getApplicationContext();
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getString(R.string.app_name));
        mWakeLock.setReferenceCounted(false);

        Utils.cleanupDownloadsDir(context);

        for (Component component : mComponentsDbHelper.getComponents()) {
            addComponent(component, false);
        }
    }

    private class DownloadEntry {
        final Component mComponent;
        DownloadClient mDownloadClient;
        private DownloadEntry(Component component) {
            mComponent = component;
        }
    }

    private Map<String, DownloadEntry> mComponents = new HashMap<>();

    void notifyComponentChange(String id) {
        Intent intent = new Intent();
        intent.setAction(ACTION_COMPONENT_STATUS);
        intent.putExtra(EXTRA_DOWNLOAD_ID, id);
        mBroadcastManager.sendBroadcast(intent);
    }

    void notifyComponentDelete(String id) {
        Intent intent = new Intent();
        intent.setAction(ACTION_COMPONENT_DELETED);
        intent.putExtra(EXTRA_DOWNLOAD_ID, id);
        mBroadcastManager.sendBroadcast(intent);
    }

    void notifyDownloadProgress(String id) {
        Intent intent = new Intent();
        intent.setAction(ACTION_DOWNLOAD_PROGRESS);
        intent.putExtra(EXTRA_DOWNLOAD_ID, id);
        mBroadcastManager.sendBroadcast(intent);
    }

    void notifyInstallProgress(String id) {
        Intent intent = new Intent();
        intent.setAction(ACTION_INSTALL_PROGRESS);
        intent.putExtra(EXTRA_DOWNLOAD_ID, id);
        mBroadcastManager.sendBroadcast(intent);
    }

    private void addDownloadClient(DownloadEntry entry, DownloadClient downloadClient) {
        if (entry.mDownloadClient != null) {
            return;
        }
        entry.mDownloadClient = downloadClient;
        mActiveDownloads++;
    }

    private void removeDownloadClient(DownloadEntry entry) {
        if (entry.mDownloadClient == null) {
            return;
        }
        entry.mDownloadClient = null;
        mActiveDownloads--;
    }

    private void tryReleaseWakelock() {
        if (!hasActiveDownloads()) {
            mWakeLock.release();
        }
    }

    private DownloadClient.DownloadCallback getDownloadCallback(final String id) {
        return new DownloadClient.DownloadCallback() {

            @Override
            public void onResponse(int statusCode, String url, DownloadClient.Headers headers) {
                final Component component = mComponents.get(id).mComponent;
                String contentLength = headers.get("Content-Length");
                if (contentLength != null) {
                    try {
                        long size = Long.parseLong(contentLength);
                        if (component.getFileSize() < size) {
                            component.setFileSize(size);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Could not get content-length");
                    }
                }
                new Thread(() -> mComponentsDbHelper.addComponentWithOnConflict(component,
                        SQLiteDatabase.CONFLICT_REPLACE)).start();
                notifyComponentChange(id);
            }

            @Override
            public void onSuccess(File destination) {
                Log.d(TAG, "Download complete");
                Component component = mComponents.get(id).mComponent;
                component.setStatus(ComponentStatus.VERIFYING);
                removeDownloadClient(mComponents.get(id));
                verifyComponentAsync(id);
                notifyComponentChange(id);
                tryReleaseWakelock();
            }

            @Override
            public void onFailure(boolean cancelled) {
                Component component = mComponents.get(id).mComponent;
                if (cancelled) {
                    Log.d(TAG, "Download cancelled");
                    // Already notified
                } else {
                    Log.e(TAG, "Download failed");
                    removeDownloadClient(mComponents.get(id));
                    component.setStatus(ComponentStatus.PAUSED_ERROR);
                    notifyComponentChange(id);
                }
            }
        };
    }

    private DownloadClient.ProgressListener getProgressListener(final String id) {
        return new DownloadClient.ProgressListener() {
            private long mLastComponent = 0;
            private int mProgress = 0;

            @Override
            public void update(long bytesRead, long contentLength, long speed, long eta,
                               boolean done) {
                Component component = mComponents.get(id).mComponent;
                if (contentLength <= 0) {
                    if (component.getFileSize() <= 0) {
                        return;
                    } else {
                        contentLength = component.getFileSize();
                    }
                }
                if (contentLength <= 0) {
                    return;
                }
                final long now = SystemClock.elapsedRealtime();
                int progress = Math.round(bytesRead * 100 / contentLength);
                if (progress != mProgress || mLastComponent - now > MAX_REPORT_INTERVAL_MS) {
                    mProgress = progress;
                    mLastComponent = now;
                    component.setProgress(progress);
                    component.setEta(eta);
                    component.setSpeed(speed);
                    notifyDownloadProgress(id);
                }
            }
        };
    }

    private void verifyComponentAsync(final String id) {
        mVerifyingComponents.add(id);
        new Thread(() -> {
            Component component = mComponents.get(id).mComponent;
            File file = component.getFile();
            if (file.exists() && verifyPackage(file)) {
                file.setReadable(true, false);
                component.setPersistentStatus(ComponentStatus.Persistent.VERIFIED);
                mComponentsDbHelper.changeComponentStatus(component);
                component.setStatus(ComponentStatus.VERIFIED);
                installComponentAsync(id);
            } else {
                component.setPersistentStatus(ComponentStatus.Persistent.UNKNOWN);
                mComponentsDbHelper.removeComponent(id);
                component.setProgress(0);
                component.setStatus(ComponentStatus.VERIFICATION_FAILED);
            }
            mVerifyingComponents.remove(id);
            notifyComponentChange(id);
        }).start();
    }

    private void installComponentAsync(final String id) {
        new Thread(() -> {
            Component component = mComponents.get(id).mComponent;
            File file = component.getFile();
            if (file.exists() && component.getStatus() == ComponentStatus.VERIFIED) {
                try {
                    component.setStatus(ComponentStatus.INSTALLING);
                    component.setPersistentStatus(ComponentStatus.Persistent.UNKNOWN);
                    ComponentInstaller.getInstance(MyApplication.getContext(), this).install(id);
                } catch (Exception e) {
                    component.setPersistentStatus(ComponentStatus.Persistent.UNKNOWN);
                    component.setProgress(0);
                    component.setStatus(ComponentStatus.INSTALLATION_FAILED);
                }
            } else{
                component.setPersistentStatus(ComponentStatus.Persistent.UNKNOWN);
                component.setProgress(0);
                component.setStatus(ComponentStatus.INSTALLATION_FAILED);
            }
            notifyComponentChange(id);
        }).start();
    }

    private boolean verifyPackage(File file) {
        try {
            android.os.RecoverySystem.verifyPackage(file, null, null);
            Log.e(TAG, "Verification successful");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Verification failed", e);
            if (file.exists()) {
                file.delete();
            } else {
                // The org.jackhuang.hmcl.download was probably stopped. Exit silently
                Log.e(TAG, "Error while verifying the file", e);
            }
            return false;
        }
    }

    private boolean fixComponentStatus(Component component) {
        switch (component.getPersistentStatus()) {
            case ComponentStatus.Persistent.VERIFIED:
            case ComponentStatus.Persistent.INCOMPLETE:
                if (component.getFile() == null || !component.getFile().exists()) {
                    component.setStatus(ComponentStatus.UNKNOWN);
                    return false;
                } else if (component.getFileSize() > 0) {
                    component.setStatus(ComponentStatus.PAUSED);
                    int progress = Math.round(
                            component.getFile().length() * 100 / component.getFileSize());
                    component.setProgress(progress);
                }
                break;
        }
        return true;
    }

    public void setComponentsNotAvailableOnline(List<String> ids) {
        for (String id : ids) {
            DownloadEntry component = mComponents.get(id);
            if (component != null) {
                component.mComponent.setAvailableOnline(false);
            }
        }
    }

    public void setComponentsAvailableOnline(List<String> ids, boolean purgeList) {
        List<String> toRemove = new ArrayList<>();
        for (DownloadEntry entry : mComponents.values()) {
            boolean online = ids.contains(entry.mComponent.getId());
            entry.mComponent.setAvailableOnline(online);
            if (!online && purgeList &&
                    entry.mComponent.getPersistentStatus() == ComponentStatus.Persistent.UNKNOWN) {
                toRemove.add(entry.mComponent.getId());
            }
        }
        for (String id : toRemove) {
            if(getComponent(id).getStatus() == ComponentStatus.INSTALLED && ! ids.contains(getSameProductComponent(id).getId())) continue;
            Log.d(TAG, id + " no longer available online, deleting");
            mComponents.remove(id);
            notifyComponentDelete(id);
        }
    }

    public boolean addComponent(ComponentInfo component) {
        return addComponent(component, true);
    }

    private boolean addComponent(final ComponentInfo componentInfo, boolean availableOnline) {
        Log.d(TAG, "Adding org.jackhuang.hmcl.download: " + componentInfo.getId());
        if (mComponents.containsKey(componentInfo.getId())) {
            Log.d(TAG, "Download (" + componentInfo.getId() + ") already added");
            Component componentAdded = mComponents.get(componentInfo.getId()).mComponent;
            componentAdded.setAvailableOnline(availableOnline && componentAdded.getAvailableOnline());
            componentAdded.setDownloadUrl(componentInfo.getDownloadUrl());
            return false;
        }
        Component component = new Component(componentInfo);
        if (!fixComponentStatus(component) && !availableOnline) {
            component.setPersistentStatus(ComponentStatus.Persistent.UNKNOWN);
            deleteComponentAsync(component);
            Log.d(TAG, component.getId() + " had an invalid status and is not online");
            return false;
        }
        component.setAvailableOnline(availableOnline);
        mComponents.put(component.getId(), new DownloadEntry(component));
        return true;
    }

    public boolean startDownload(String id) {
        if (id == "" || id == null){
            Log.w(TAG, "The component's id is invalid.");
            return false;
        }
        Log.d(TAG, "Starting " + id);
        if (!mComponents.containsKey(id) || isDownloading(id)) {
            return false;
        }
        Component component = mComponents.get(id).mComponent;
        File destination = new File(mDownloadRoot, component.getName());
        if (destination.exists()) {
            destination = Utils.appendSequentialNumber(destination);
            Log.d(TAG, "Changing name with " + destination.getName());
        }
        component.setFile(destination);
        DownloadClient downloadClient;
        try {
            downloadClient = new DownloadClient.Builder()
                    .setUrl(component.getDownloadUrl())
                    .setDestination(component.getFile())
                    .setDownloadCallback(getDownloadCallback(id))
                    .setProgressListener(getProgressListener(id))
                    .setUseDuplicateLinks(true)
                    .build();
        } catch (IOException exception) {
            Log.e(TAG, "Could not build org.jackhuang.hmcl.download client");
            component.setStatus(ComponentStatus.PAUSED_ERROR);
            notifyComponentChange(id);
            return false;
        }
        addDownloadClient(mComponents.get(id), downloadClient);
        component.setStatus(ComponentStatus.STARTING);
        notifyComponentChange(id);
        downloadClient.start();
        mWakeLock.acquire();
        return true;
    }

    public boolean resumeDownload(String id) {
        Log.d(TAG, "Resuming " + id);
        if (!mComponents.containsKey(id) || isDownloading(id)) {
            return false;
        }
        Component component = mComponents.get(id).mComponent;
        File file = component.getFile();
        if (file == null || !file.exists()) {
            Log.e(TAG, "The destination file of " + id + " doesn't exist, can't resume");
            component.setStatus(ComponentStatus.PAUSED_ERROR);
            notifyComponentChange(id);
            return false;
        }
        if (file.exists() && component.getFileSize() > 0 && file.length() >= component.getFileSize()) {
            Log.d(TAG, "File already downloaded, starting verification");
            component.setStatus(ComponentStatus.VERIFYING);
            verifyComponentAsync(id);
            notifyComponentChange(id);
        } else {
            DownloadClient downloadClient;
            try {
                downloadClient = new DownloadClient.Builder()
                        .setUrl(component.getDownloadUrl())
                        .setDestination(component.getFile())
                        .setDownloadCallback(getDownloadCallback(id))
                        .setProgressListener(getProgressListener(id))
                        .setUseDuplicateLinks(true)
                        .build();
            } catch (IOException exception) {
                Log.e(TAG, "Could not build org.jackhuang.hmcl.download client");
                component.setStatus(ComponentStatus.PAUSED_ERROR);
                notifyComponentChange(id);
                return false;
            }
            addDownloadClient(mComponents.get(id), downloadClient);
            component.setStatus(ComponentStatus.STARTING);
            notifyComponentChange(id);
            downloadClient.resume();
            mWakeLock.acquire();
        }
        return true;
    }

    public boolean pauseDownload(String id) {
        Log.d(TAG, "Pausing " + id);
        if (!isDownloading(id)) {
            return false;
        }

        DownloadEntry entry = mComponents.get(id);
        entry.mDownloadClient.cancel();
        removeDownloadClient(entry);
        entry.mComponent.setStatus(ComponentStatus.PAUSED);
        entry.mComponent.setEta(0);
        entry.mComponent.setSpeed(0);
        notifyComponentChange(id);
        return true;
    }

    private void deleteComponentAsync(final Component component) {
        new Thread(() -> {
            File file = component.getFile();
            if (file.exists() && !file.delete()) {
                Log.e(TAG, "Could not delete " + file.getAbsolutePath());
            }
            mComponentsDbHelper.removeComponent(component.getId());
        }).start();
    }
    /*
    public boolean deleteComponent(String id) {
        Log.d(TAG, "Cancelling " + id);
        if (!mComponents.containsKey(id) || isDownloading(id)) {
            return false;
        }
        Component component = mComponents.get(id).mComponent;
        component.setStatus(ComponentStatus.UNKNOWN);
        component.setProgress(0);
        component.setPersistentStatus(ComponentStatus.Persistent.UNKNOWN);
        deleteComponentAsync(component);

        if (!component.getAvailableOnline()) {
            Log.d(TAG, "Download no longer available online, deleting");
            mComponents.remove(id);
            notifyComponentDelete(id);
        } else {
            notifyComponentChange(id);
        }

        return true;
    }*/

    public boolean removeComponent(String id) {
        Log.d(TAG, "Removing " + id);
        if (!isInstalled(id)) {
            return false;
        }
        Component component = getActualComponent(id);
        component.setStatus(ComponentStatus.UNKNOWN);
        component.setProgress(0);
        component.setPersistentStatus(ComponentStatus.Persistent.UNKNOWN);
        ComponentInstaller.getInstance(mContext, this).remove(id);
        notifyComponentChange(id);

        return true;
    }

    public Set<String> getIds() {
        return mComponents.keySet();
    }

    public List<ComponentInfo> getComponents() {
        List<ComponentInfo> components = new ArrayList<>();
        for (DownloadEntry entry : mComponents.values()) {
            components.add(entry.mComponent);
        }
        return components;
    }

    public ComponentInfo getComponent(String id) {
        DownloadEntry entry = mComponents.get(id);
        return entry != null ? entry.mComponent : null;
    }

    Component getActualComponent(String id) {
        DownloadEntry entry = mComponents.get(id);
        return entry != null ? entry.mComponent : null;
    }

    public ComponentInfo getSameProductComponent(String productId) {
        for(ComponentInfo component : getComponents()) {
            if(component.getProductId() == productId) return component;
        }
        return null;
    }
    public ComponentInfo getSameProductComponent(List<ComponentInfo> components, String productId) {
        for(ComponentInfo component : components) {
            if(component.getProductId() == productId) return component;
        }
        return null;
    }

    public boolean isDownloading(String id) {
        return mComponents.containsKey(id) &&
                mComponents.get(id).mDownloadClient != null;
    }

    public boolean isInstalled(String id) {
        return getComponent(id).getStatus() == ComponentStatus.INSTALLED;
    }

    public boolean hasActiveDownloads() {
        return mActiveDownloads > 0;
    }

    public boolean isVerifyingComponent() {
        return mVerifyingComponents.size() > 0;
    }

    public boolean isVerifyingComponent(String id) {
        return mVerifyingComponents.contains(id);
    }

    public boolean isInstallingComponent() {
        return ComponentInstaller.isInstalling();
    }

    public boolean isInstallingComponent(String id) {
        return ComponentInstaller.isInstalling(id);
    }
}
