package org.exthmui.minejlauncher.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.exthmui.minejlauncher.R;

import android.content.SharedPreferences;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Set;
/*
public class AdvancedSettings extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}*/
public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences mSharedPreferences;
    private ListPreference mDownloadServer;
    private EditTextPreference mDownloadUserAgent;
    private EditTextPreference mAuthUserAgent;
    private EditTextPreference mJvmMemory;
    private EditTextPreference mAppendJvmCommand;
    private EditTextPreference mAppendMinecraftCommand;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mDownloadServer = findPreference("download_server");
        mDownloadUserAgent = findPreference("download_useragent");
        mAuthUserAgent = findPreference("auth_useragent");
        mJvmMemory = findPreference("memory");
        mAppendJvmCommand = findPreference("append_jvm");
        mAppendMinecraftCommand = findPreference("append_minecraft");

        mDownloadServer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return OnPreferenceChange(preference, newValue);
            }
        });

        mDownloadUserAgent.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return OnPreferenceChange(preference, newValue);
            }
        });

        mAuthUserAgent.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return OnPreferenceChange(preference, newValue);
            }
        });

        mJvmMemory.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return OnPreferenceChange(preference, newValue);
            }
        });

        mAppendJvmCommand.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return OnPreferenceChange(preference, newValue);
            }
        });

        mAppendMinecraftCommand.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return OnPreferenceChange(preference, newValue);
            }
        });

        OnPreferenceChange(mDownloadServer, null);
        OnPreferenceChange(mDownloadUserAgent, null);
        OnPreferenceChange(mAuthUserAgent, null);
        OnPreferenceChange(mJvmMemory, null);
        OnPreferenceChange(mAppendJvmCommand, null);
        OnPreferenceChange(mAppendMinecraftCommand, null);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }



    private boolean OnPreferenceChange(Preference preference, @Nullable Object newValue) {
        try {
            if (preference == mDownloadServer) {
                String prefsValue = (String) newValue;
                if (prefsValue == null) {
                    prefsValue = mSharedPreferences.getString(preference.getKey(), prefsValue);
                }
                if (prefsValue.contains("official")) {
                    preference.setSummary(R.string.setting_download_server_official);
                } else if (prefsValue.contains("bmclapi")) {
                    preference.setSummary(R.string.setting_download_server_bmclapi);
                }
            }
            boolean toStringIsEmpty = newValue == null || newValue.toString().trim().isEmpty();
            if (preference == mDownloadUserAgent) {
                if(toStringIsEmpty){
                    mDownloadUserAgent.setText(getString(R.string.download_useragent));
                    return false;
                }else preference.setSummary((String)newValue);
            }
            if (preference == mAuthUserAgent) {
                if(toStringIsEmpty){
                    mAuthUserAgent.setText(getString(R.string.auth_useragent));
                    return false;
                }else preference.setSummary((String)newValue);
            }
            if (preference == mJvmMemory) {
                if(toStringIsEmpty){
                    mJvmMemory.setText(getString(R.string.running_memory));
                    return false;
                }else preference.setSummary((String)newValue+"M");
            }
            if (preference == mAppendJvmCommand) {
                if(toStringIsEmpty){
                    mAppendJvmCommand.setText("");
                    return false;
                }else preference.setSummary(" " + (String)newValue);
            }
            if (preference == mAppendMinecraftCommand) {
                if(toStringIsEmpty){
                    mAppendMinecraftCommand.setText("");
                    return false;
                }else preference.setSummary(" " + (String)newValue);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }
}