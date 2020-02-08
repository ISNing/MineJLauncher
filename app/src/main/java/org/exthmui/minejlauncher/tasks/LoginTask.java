package org.exthmui.minejlauncher.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.R;
import org.exthmui.minejlauncher.auth.AuthenticateResponse;
import org.exthmui.minejlauncher.auth.YggdrasilAuthenticator;
import org.exthmui.minejlauncher.data.model.LoggedInUser;
import org.exthmui.minejlauncher.model.OnVariableChangeListener;
import org.exthmui.minejlauncher.ui.login.LoginActivity;

import java.util.UUID;

public class LoginTask extends AsyncTask<String, Void, String> {

    final private static String TAG="LoginTask";

    private LoginActivity activity;
    private YggdrasilAuthenticator authenticator;

    private LoggedInUser loggedInUser;
    private String result;

    private OnVariableChangeListener onResultChangeListener;

    public LoginTask(LoginActivity activity) {
        this.activity = activity;
    }

    private UUID getClientId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String out = prefs.getString("auth_clientToken", null);
        // TODO:I don't know what's this...
        boolean needsRegenUUID = prefs.getBoolean("auth_importedCredentials", false);
        UUID retval;
        if (out == null || needsRegenUUID) {
            retval = UUID.randomUUID();
            prefs.edit().putString("auth_clientToken", retval.toString()).
                    putBoolean("auth_importedCredentials", false).
                    apply();
        } else {
            retval = UUID.fromString(out);
        }
        return retval;
    }

    public void onPreExecute() {
        Log.e(TAG,"running onPreExecute");
        authenticator = new YggdrasilAuthenticator(activity);
        activity.getUsernameEditText().setVisibility(View.INVISIBLE);
        activity.getPasswordEditText().setVisibility(View.INVISIBLE);
        activity.getLoginButton().setEnabled(false);
        activity.getLoginButton().setVisibility(View.INVISIBLE);
        activity.getAuthModeChooser().setVisibility(View.INVISIBLE);
        activity.getAuthModeText().setVisibility(View.INVISIBLE);

        activity.getLoadingProgressBar().setVisibility(View.VISIBLE);


        //activity.importCredentialsButton.setEnabled(false);
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public String doInBackground(String... args) {
        Log.e(TAG,"running doInBackground");
        try {
            AuthenticateResponse response = authenticator.authenticate(args[0], args[1], getClientId(), args[2]);
            if (MainActivity.getMainActivity().authStatusCode != 200) {
                Log.w(TAG, "Auth StatusCode:" + MainActivity.getMainActivity().authStatusCode);
                return "Unexpected returning,StatusCode:" + MainActivity.getMainActivity().authStatusCode;
            }
            if (response == null) {
                Log.w(TAG, "Response is null");
                return "Response is null?";
            }
            if (response.getSelectedProfile() == null) {
                Log.w(TAG, "selectedProfile is null");
                return activity.getResources().getString(R.string.login_is_demo_account);
            }

            loggedInUser = new LoggedInUser(args[0], args[1], response.getClientToken(), response.getAccessToken(), response.getSelectedProfile(), response.getAvailableProfiles());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            prefs.edit().
                    putString("auth_username", args[0]).
                    putString("auth_password", args[1]).
                    putString("auth_clientToken", response.getClientToken().toString()).
                    putString("auth_accessToken", response.getAccessToken()).
                    putString("auth_profile_name", response.getSelectedProfile().name).
                    apply();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public void onPostExecute(String result) {
        Log.e(TAG,"running onPostExecute");
        activity.getLoadingProgressBar().setVisibility(View.GONE);
        activity.getLoginButton().setEnabled(true);
        if (result == null) {
            // success
            MainActivity.getMainActivity().refreshedToken = true;
            Log.d(TAG, "Login succeeded");
        } else {
            MainActivity.getMainActivity().refreshedToken = false;
            activity.getUsernameEditText().setVisibility(View.VISIBLE);
            activity.getPasswordEditText().setVisibility(View.VISIBLE);
            activity.getLoginButton().setEnabled(false);
            activity.getLoginButton().setVisibility(View.VISIBLE);
            activity.getAuthModeChooser().setVisibility(View.VISIBLE);
            activity.getAuthModeText().setVisibility(View.VISIBLE);
            activity.getUsernameEditText().setText("");
            activity.getPasswordEditText().setText("");
            activity.getLoadingProgressBar().setVisibility(View.GONE);
            Log.w(TAG, "Login failed"+result);
       }
        setResult(result);
    }

    public LoggedInUser getLoggedInUser() {
        return loggedInUser;
    }

    public String getResult() {
        return result;
    }

    public OnVariableChangeListener getOnResultChangeListener() {
        return onResultChangeListener;
    }

    public void setOnResultChangeListener(OnVariableChangeListener onResultChangeListener) {
        this.onResultChangeListener = onResultChangeListener;
    }

    public void setResult(String result) {
        this.result = result;
        this.onResultChangeListener.OnVariableChange();
    }
}
