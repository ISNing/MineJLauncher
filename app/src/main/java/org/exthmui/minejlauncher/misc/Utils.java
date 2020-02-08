package org.exthmui.minejlauncher.misc;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.exthmui.minejlauncher.Constants;
import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.controller.ComponentsDbHelper;
import org.exthmui.minejlauncher.model.ComponentInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = "Utils";

    public static String getAPIUrl(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("api_url", Constants.AUTH_MOJANG_API_URL);
    }
    public static File getDownloadPath(Context context) {
        return context.getCacheDir();
    }
    public static boolean canInstall(ComponentInfo component) {
        return true;
    }
    public static String getNetworkType(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = (manager == null ? null : manager.getActiveNetworkInfo());
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE || networkInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
                    return "data";
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)  {
                    return "wifi";
                } else return "others";
            }
        }
        return null;
    }

    public static File appendSequentialNumber(final File file) {
        String name;
        String extension;
        int extensionPosition = file.getName().lastIndexOf(".");
        if (extensionPosition > 0) {
            name = file.getName().substring(0, extensionPosition);
            extension = file.getName().substring(extensionPosition);
        } else {
            name = file.getName();
            extension = "";
        }
        final File parent = file.getParentFile();
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            File newFile = new File(parent, name + "-" + i + extension);
            if (!newFile.exists()) {
                return newFile;
            }
        }
        throw new IllegalStateException();
    }

    public static void removeInstalledtFiles(File downloadPath) {
        File[] installedFiles = downloadPath.listFiles(
                (dir, name) -> name.endsWith(Constants.INSTALLED_FILE_EXT));
        if (installedFiles == null) {
            return;
        }
        for (File file : installedFiles) {
            file.delete();
        }
    }

    /**
     * Cleanup the org.jackhuang.hmcl.download directory, which is assumed to be a privileged location
     * the user can't access and that might have stale files. This can happen if
     * the data of the application are wiped.
     *
     * @param context
     */
    public static void cleanupDownloadsDir(Context context) {
        File downloadPath = getDownloadPath(context);
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);

        removeInstalledtFiles(downloadPath);

        final String DOWNLOADS_CLEANUP_DONE = "cleanup_done";
        if (preferences.getBoolean(DOWNLOADS_CLEANUP_DONE, false)) {
            return;
        }

        Log.d(TAG, "Cleaning " + downloadPath);
        if (!downloadPath.isDirectory()) {
            return;
        }
        File[] files = downloadPath.listFiles();
        if (files == null) {
            return;
        }

        // Ideally the database is empty when we get here
        ComponentsDbHelper dbHelper = new ComponentsDbHelper(context);
        List<String> knownPaths = new ArrayList<>();
        for (ComponentInfo component : dbHelper.getComponents()) {
            knownPaths.add(component.getFile().getAbsolutePath());
        }
        for (File file : files) {
            if (!knownPaths.contains(file.getAbsolutePath())) {
                Log.d(TAG, "Deleting " + file.getAbsolutePath());
                file.delete();
            }
        }

        preferences.edit().putBoolean(DOWNLOADS_CLEANUP_DONE, true).apply();
    }
}
