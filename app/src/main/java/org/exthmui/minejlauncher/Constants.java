package org.exthmui.minejlauncher;

import android.content.Context;

import androidx.preference.PreferenceManager;

import java.nio.charset.Charset;

public class Constants {
    private static final Context context = MainActivity.getMainActivity().getApplicationContext();

    public static final int PACKTYPE_DEPENDING = 0;
    public static final int PACKTYPE_MINECRAFT = 1;
    public static final int PACKTYPE_FORGE = 2;
    public static final int PACKTYPE_OPTFINE = 3;

    public static final int LOGIN_FLAG_OFFICIAL = 0;
    public static final int LOGIN_FLAG_AUTHLIB_INJECTOR = 1;
    public static final int LOGIN_FLAG_OFFLINE = 2;

    public static String DOWNLOAD_USER_AGENT = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("download_useragent", context.getString(R.string.download_useragent));

    public static String AUTH_USER_AGENT = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("auth_useragent", context.getString(R.string.auth_useragent));

    public static final String AUTH_MOJANG_API_URL = context.getString(R.string.auth_mojang_api_url);


    public static final Charset utf8 = Charset.forName("UTF-8");

    public static final String PREF_MOBILE_DATA_WARNING = "pref_mobile_data_warning";

    public static final String INSTALLED_FILE_EXT = ".installed";

    public static final String PREF_INSTALL_OLD_VERSION = "install_old_version";
    public static final String PREF_INSTALL_NEW_VERSION = "install_new_version";
    public static final String PREF_INSTALL_PACKAGE_PATH = "install_package_path";
    public static final String PREF_INSTALL_AGAIN = "install_again";
    public static final String PREF_INSTALL_NOTIFIED = "install_notified";
}
