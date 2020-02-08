package org.exthmui.minejlauncher.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.R;
import org.exthmui.minejlauncher.misc.StringGenerator;
import org.exthmui.minejlauncher.misc.Utils;
import org.exthmui.minejlauncher.model.ComponentInfo;
import org.exthmui.minejlauncher.model.ComponentStatus;

import java.text.DateFormat;
import java.text.NumberFormat;

public class ComponentService extends Service {

    private static final String TAG = "ComponentService";

    public static final String ACTION_DOWNLOAD_CONTROL = "action_download_control";
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
    public static final String EXTRA_DOWNLOAD_CONTROL = "extra_download_control";
    public static final String ACTION_INSTALL_COMPONENT = "action_install_component";
    public static final String ACTION_INSTALL_STOP = "action_install_stop";

    public static final String ACTION_INSTALL_RESUME = "action_install_resume";

    private static final String ONGOING_NOTIFICATION_CHANNEL =
            "ongoing_notification_channel";

    public static final int DOWNLOAD_RESUME = 0;
    public static final int DOWNLOAD_PAUSE = 1;

    private static final int NOTIFICATION_ID = 10;

    private final IBinder mBinder = new LocalBinder();
    private boolean mHasClients;

    private BroadcastReceiver mBroadcastReceiver;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private NotificationCompat.BigTextStyle mNotificationStyle;

    private ComponentController mComponentController;

    @Override
    public void onCreate() {
        super.onCreate();

        mComponentController = ComponentController.getInstance(this);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(
                ONGOING_NOTIFICATION_CHANNEL,
                getString(R.string.ongoing_channel_title),
                NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(notificationChannel);
        mNotificationBuilder = new NotificationCompat.Builder(this,
                ONGOING_NOTIFICATION_CHANNEL);
        mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mNotificationBuilder.setShowWhen(false);
        mNotificationStyle = new NotificationCompat.BigTextStyle();
        mNotificationBuilder.setStyle(mNotificationStyle);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(intent);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String downloadId = intent.getStringExtra(ComponentController.EXTRA_DOWNLOAD_ID);
                if (ComponentController.ACTION_COMPONENT_STATUS.equals(intent.getAction())) {
                    ComponentInfo component = mComponentController.getComponent(downloadId);
                    setNotificationTitle(component);
                    Bundle extras = new Bundle();
                    extras.putString(ComponentController.EXTRA_DOWNLOAD_ID, downloadId);
                    mNotificationBuilder.setExtras(extras);
                    handleComponentStatusChange(component);
                } else if (ComponentController.ACTION_DOWNLOAD_PROGRESS.equals(intent.getAction())) {
                    ComponentInfo component = mComponentController.getComponent(downloadId);
                    handleDownloadProgressChange(component);
                } else if (ComponentController.ACTION_INSTALL_PROGRESS.equals(intent.getAction())) {
                    ComponentInfo component = mComponentController.getComponent(downloadId);
                    setNotificationTitle(component);
                    handleInstallProgress(component);
                } /*else if (ComponentController.ACTION_COMPONENT_DELETED.equals(intent.getAction())) {
                    Bundle extras = mNotificationBuilder.getExtras();
                    if (extras != null && downloadId.equals(
                            extras.getString(ComponentController.EXTRA_DOWNLOAD_ID))) {
                        mNotificationBuilder.setExtras(null);
                        mNotificationManager.cancel(NOTIFICATION_ID);
                    }
                }*/
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ComponentController.ACTION_DOWNLOAD_PROGRESS);
        intentFilter.addAction(ComponentController.ACTION_INSTALL_PROGRESS);
        intentFilter.addAction(ComponentController.ACTION_COMPONENT_STATUS);
        //intentFilter.addAction(ComponentController.ACTION_COMPONENT_DELETED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public ComponentService getService() {
            return ComponentService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mHasClients = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mHasClients = false;
        tryStopSelf();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting service");

        if (intent == null || intent.getAction() == null) {
        } else if (ACTION_DOWNLOAD_CONTROL.equals(intent.getAction())) {
            String downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID);
            int action = intent.getIntExtra(EXTRA_DOWNLOAD_CONTROL, -1);
            if (action == DOWNLOAD_RESUME) {
                mComponentController.resumeDownload(downloadId);
            } else if (action == DOWNLOAD_PAUSE) {
                mComponentController.pauseDownload(downloadId);
            } else {
                Log.e(TAG, "Unknown org.jackhuang.hmcl.download action");
            }
        } else if (ACTION_INSTALL_COMPONENT.equals(intent.getAction())) {
            String downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID);
            ComponentInfo component = mComponentController.getComponent(downloadId);
            if (component.getPersistentStatus() != ComponentStatus.Persistent.VERIFIED) {
                throw new IllegalArgumentException(component.getId() + " is not verified");
            }
            try {
                ComponentInstaller installer = ComponentInstaller.getInstance(this,
                            mComponentController);
                installer.install(downloadId);
            } catch (Exception e) {
                Log.e(TAG, "Could not install component", e);
                mComponentController.getActualComponent(downloadId)
                        .setStatus(ComponentStatus.INSTALLATION_FAILED);
                mComponentController.notifyComponentChange(downloadId);
            }
        } else if (ACTION_INSTALL_STOP.equals(intent.getAction())) {
            if (ComponentInstaller.isInstalling()) {
                ComponentInstaller installer = ComponentInstaller.getInstance(this,
                        mComponentController);
                installer.cancel();
            }
        }
        return ComponentInstaller.isInstalling() ? START_STICKY : START_NOT_STICKY;
    }

    public ComponentController getComponentController() {
        return mComponentController;
    }

    private void tryStopSelf() {
        if (!mHasClients && !mComponentController.hasActiveDownloads() &&
                !mComponentController.isInstallingComponent()) {
            Log.d(TAG, "Service no longer needed, stopping");
            stopSelf();
        }
    }

    private void handleComponentStatusChange(ComponentInfo component) {
        switch (component.getStatus()) {
            case STARTING: {
                mNotificationBuilder.mActions.clear();
                mNotificationBuilder.setProgress(0, 0, true);
                mNotificationStyle.setSummaryText(null);
                String text = getString(R.string.download_starting_notification);
                mNotificationStyle.bigText(text);
                mNotificationBuilder.setStyle(mNotificationStyle);
                mNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(true);
                mNotificationBuilder.setAutoCancel(false);
                startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                break;
            }
            case DOWNLOADING: {
                String text = getString(R.string.downloading_notification);
                mNotificationStyle.bigText(text);
                mNotificationBuilder.setStyle(mNotificationStyle);
                mNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
                mNotificationBuilder.addAction(android.R.drawable.ic_media_pause,
                        getString(R.string.pause_button),
                        getPausePendingIntent(component.getId()));
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(true);
                mNotificationBuilder.setAutoCancel(false);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                break;
            }
            case PAUSED: {
                stopForeground(STOP_FOREGROUND_DETACH);
                // In case we pause before the first progress component
                mNotificationBuilder.setProgress(100, component.getProgress(), false);
                mNotificationBuilder.mActions.clear();
                String text = getString(R.string.download_paused_notification);
                mNotificationStyle.bigText(text);
                mNotificationBuilder.setStyle(mNotificationStyle);
                mNotificationBuilder.setSmallIcon(R.drawable.ic_pause);
                mNotificationBuilder.addAction(android.R.drawable.ic_media_play,
                        getString(R.string.resume_button),
                        getResumePendingIntent(component.getId()));
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(false);
                mNotificationBuilder.setAutoCancel(false);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                tryStopSelf();
                break;
            }
            case PAUSED_ERROR: {
                stopForeground(STOP_FOREGROUND_DETACH);
                int progress = component.getProgress();
                // In case we pause before the first progress component
                mNotificationBuilder.setProgress(progress > 0 ? 100 : 0, progress, false);
                mNotificationBuilder.mActions.clear();
                String text = getString(R.string.download_paused_error_notification);
                mNotificationStyle.bigText(text);
                mNotificationBuilder.setStyle(mNotificationStyle);
                mNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_warning);
                mNotificationBuilder.addAction(android.R.drawable.ic_media_play,
                        getString(R.string.resume_button),
                        getResumePendingIntent(component.getId()));
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(false);
                mNotificationBuilder.setAutoCancel(false);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                tryStopSelf();
                break;
            }
            case VERIFYING: {
                mNotificationBuilder.setProgress(0, 0, true);
                mNotificationStyle.setSummaryText(null);
                mNotificationBuilder.setStyle(mNotificationStyle);
                mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mNotificationBuilder.mActions.clear();
                String text = getString(R.string.verifying_download_notification);
                mNotificationStyle.bigText(text);
                mNotificationBuilder.setTicker(text);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                break;
            }
            /*case VERIFIED: {
                stopForeground(STOP_FOREGROUND_DETACH);
                mNotificationBuilder.setStyle(null);
                mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mNotificationBuilder.setProgress(0, 0, false);
                String text = getString(R.string.download_completed_notification);
                mNotificationBuilder.setContentText(text);
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(false);
                mNotificationBuilder.setAutoCancel(true);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                tryStopSelf();
                break;
            }*/
            case VERIFICATION_FAILED: {
                stopForeground(STOP_FOREGROUND_DETACH);
                mNotificationBuilder.setStyle(null);
                mNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_warning);
                mNotificationBuilder.setProgress(0, 0, false);
                String text = getString(R.string.verification_failed_notification);
                mNotificationBuilder.setContentText(text);
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(false);
                mNotificationBuilder.setAutoCancel(true);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                tryStopSelf();
                break;
            }
            case INSTALLING: {
                mNotificationBuilder.mActions.clear();
                mNotificationBuilder.setStyle(mNotificationStyle);
                mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mNotificationBuilder.setProgress(0, 0, false);
                mNotificationStyle.setSummaryText(null);
                String text = getString(R.string.installing_component);
                mNotificationStyle.bigText(text);
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(true);
                mNotificationBuilder.setAutoCancel(false);
                startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                break;
            }
            case INSTALLED: {
                stopForeground(STOP_FOREGROUND_DETACH);
                mNotificationBuilder.mActions.clear();
                mNotificationBuilder.setStyle(null);
                mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mNotificationBuilder.setProgress(0, 0, false);
                String text = getString(R.string.installing_component_finished);
                mNotificationBuilder.setContentText(text);
                mNotificationBuilder.addAction(R.mipmap.ic_launcher,
                        getString(R.string.lunch_button),
                        getLunchPendingIntent());
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(false);
                mNotificationBuilder.setAutoCancel(true);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                tryStopSelf();
                break;
            }
            case INSTALLATION_FAILED: {
                stopForeground(STOP_FOREGROUND_DETACH);
                mNotificationBuilder.setStyle(null);
                mNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_warning);
                mNotificationBuilder.setProgress(0, 0, false);
                String text = getString(R.string.installing_component_error);
                mNotificationBuilder.setContentText(text);
                mNotificationBuilder.setTicker(text);
                mNotificationBuilder.setOngoing(false);
                mNotificationBuilder.setAutoCancel(true);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                tryStopSelf();
                break;
            }
            /*case INSTALLATION_CANCELLED: {
                stopForeground(true);
                tryStopSelf();
                break;
            }*/
        }
    }

    private void handleDownloadProgressChange(ComponentInfo component) {
        int progress = component.getProgress();
        mNotificationBuilder.setProgress(100, progress, false);

        String percent = NumberFormat.getPercentInstance().format(progress / 100.f);
        mNotificationStyle.setSummaryText(percent);

        setNotificationTitle(component);

        String speed = Formatter.formatFileSize(this, component.getSpeed());
        CharSequence eta = StringGenerator.formatETA(this, component.getEta() * 1000);
        mNotificationStyle.bigText(
                getString(R.string.text_download_speed, eta, speed));

        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void handleInstallProgress(ComponentInfo component) {
        setNotificationTitle(component);
        int progress = component.getInstallProgress();
        mNotificationBuilder.setProgress(100, progress, false);
        String percent = NumberFormat.getPercentInstance().format(progress / 100.f);
        mNotificationStyle.setSummaryText(percent);
        mNotificationStyle.bigText(getString(R.string.installing_component));
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void setNotificationTitle(ComponentInfo component) {
        mNotificationStyle.setBigContentTitle(component.getName() + " " + component.getDisplayVersion());
        mNotificationBuilder.setContentTitle(component.getIntroduction());
    }

    private PendingIntent getResumePendingIntent(String downloadId) {
        final Intent intent = new Intent(this, ComponentService.class);
        intent.setAction(ACTION_DOWNLOAD_CONTROL);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        intent.putExtra(EXTRA_DOWNLOAD_CONTROL, DOWNLOAD_RESUME);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPausePendingIntent(String downloadId) {
        final Intent intent = new Intent(this, ComponentService.class);
        intent.setAction(ACTION_DOWNLOAD_CONTROL);
        intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
        intent.putExtra(EXTRA_DOWNLOAD_CONTROL, DOWNLOAD_PAUSE);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getLunchPendingIntent() {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(null);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

