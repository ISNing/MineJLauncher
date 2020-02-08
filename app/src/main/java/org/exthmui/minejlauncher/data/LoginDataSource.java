package org.exthmui.minejlauncher.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.preference.PreferenceManager;

import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.data.model.LoggedInUser;
import org.exthmui.minejlauncher.tasks.LoginTask;
import org.exthmui.minejlauncher.tasks.RefreshAuthTokenTask;
import org.exthmui.minejlauncher.ui.login.LoginActivity;

import java.io.IOException;
import java.sql.Ref;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private LoginTask logger;
    private RefreshAuthTokenTask refresher;

    private static LoginDataSource instance;

    public void startLogin(String username, String password, String apiurl) {
        logger = new LoginTask(LoginActivity.getLoginActivity());
        logger.execute(username, password, apiurl);
    }

    public Result<LoggedInUser> login() {
        try {
            String result = logger.getResult();
            LoggedInUser loggedInUser = logger.getLoggedInUser();
            if(result == null)
                PreferenceManager.getDefaultSharedPreferences(
                        LoginActivity.getLoginActivity().getApplicationContext()).edit().putBoolean("loggedin", true).apply();

            return result != null ?
                    new Result.Error(new Exception("result: "+result)) :
                    new Result.Success<>(loggedInUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }


    public Result<LoggedInUser> refresh(String accessToken, String clientToken, String username, String password) {

        try {
            refresher = new RefreshAuthTokenTask(MainActivity.getMainActivity());
            refresher.execute(accessToken, clientToken, username, password);
            String result = refresher.getResult();
            LoggedInUser loggedInUser = refresher.getLoggedInUser();
            return loggedInUser == null ?
                    new Result.Error(new Exception("RefreshToken failed,result:\n"+result)) :
                    new Result.Success<>(loggedInUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error RefreshingToken", e));
        }
    }

    public void logout() {
        PreferenceManager.getDefaultSharedPreferences(
                MainActivity.getMainActivity().getApplicationContext()).edit().putBoolean("loggedin", false).
                remove("auth_username").
                remove("auth_password").
                remove("auth_clientToken").
                remove("auth_accessToken").
                remove("auth_profile_name").
                putBoolean("loggedin", false).
                apply();
    }

    public LoginTask getLogger() {
        return logger;
    }

    public static LoginDataSource getInstance() {
        if(instance == null) {
            instance = new LoginDataSource();
        }
        return instance;
    }
}
