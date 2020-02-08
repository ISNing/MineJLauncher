package org.exthmui.minejlauncher.ui.game_dl;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import org.exthmui.minejlauncher.R;
import org.exthmui.minejlauncher.download.DownloadClient;
import org.exthmui.minejlauncher.misc.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class GameDlViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private static final String TAG = "GameDlViewModel";

    public GameDlViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }


    private void downloadRuntimesList(final boolean manualRefresh) {
        final File jsonFile = Utils.getCachedUpdateList(this);
        final File jsonFileTmp = new File(jsonFile.getAbsolutePath() + UUID.randomUUID());
        final File jsonFileTmp2 = new File(jsonFile2.getAbsolutePath() + UUID.randomUUID());
        String url = Utils.getServerURL(this,true);
        String url2 = Utils.getServerURL(this,false);
        Log.d(TAG, "Checking " + url + " and " + url2);

        DownloadClient.DownloadCallback callback = new DownloadClient.DownloadCallback() {
            @Override
            public void onFailure(final boolean cancelled) {
                Log.e(TAG, "Could not org.jackhuang.hmcl.download updates list");
                runOnUiThread(() -> {
                    if (!cancelled) {
                        showSnackbar(R.string.snack_updates_check_failed, Snackbar.LENGTH_LONG);
                    }
                    refreshAnimationStop();
                });
            }

            @Override
            public void onResponse(int statusCode, String url,
                                   DownloadClient.Headers headers) {
            }

            @Override
            public void onSuccess(File destination) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Update list downloaded");
                    processNewJson(jsonFile, jsonFileTmp, manualRefresh, true);
                    refreshAnimationStop();
                });
            }
        };

        final DownloadClient downloadClient;
        try {
            downloadClient = new DownloadClient.Builder()
                    .setUrl(url)
                    .setDestination(jsonFileTmp)
                    .setDownloadCallback(callback)
                    .build();
        } catch (IOException exception) {
            Log.e(TAG, "Could not build org.jackhuang.hmcl.download client");
            showSnackbar(R.string.snack_updates_check_failed, Snackbar.LENGTH_LONG);
            return;
        }

        refreshAnimationStart();
        downloadClient.start();
    }
}