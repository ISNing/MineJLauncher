package org.exthmui.minejlauncher.data;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.data.model.LoggedInUser;
import org.exthmui.minejlauncher.ui.login.LoginActivity;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                MainActivity.getMainActivity().getApplicationContext());
        // look at key:loggedin,load information and refresh accessToken
        if(prefs.getBoolean("loggedin", false)){
            refresh(prefs.getString("auth_accessToken", null),
                    prefs.getString("auth_clientToken", null),
                    prefs.getString("auth_username", null),
                    prefs.getString("auth_password", null));
        }
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        setLoggedInUser(null);
        PreferenceManager.getDefaultSharedPreferences(MainActivity.getMainActivity()).edit().remove("login_mode_flag");
        dataSource.logout();
        MainActivity.getMainActivity().refreshUserStatus();
    }

    public LoggedInUser getLoggedInUeser(){
        return user;
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public void startLogin(String username, String password, String apiurl) {
        // loginTask start
        dataSource.startLogin(username, password, apiurl);
    }

    public Result<LoggedInUser> login() {
        // handle loginTask finished
        Result<LoggedInUser> result = dataSource.login();
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }else {
            setLoggedInUser(null);
        }
        return result;
    }

    public Result<LoggedInUser> refresh(String accessToken, String clientToken, String username, String password) {
        Result<LoggedInUser> result = dataSource.refresh(accessToken, clientToken, username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }else {
            setLoggedInUser(null);
        }
        return result;
    }
}
