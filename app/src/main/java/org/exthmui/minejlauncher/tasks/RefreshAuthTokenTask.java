package org.exthmui.minejlauncher.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.R;
import org.exthmui.minejlauncher.auth.RefreshResponse;
import org.exthmui.minejlauncher.auth.YggdrasilAuthenticator;
import org.exthmui.minejlauncher.data.model.LoggedInUser;

import java.util.UUID;

public class RefreshAuthTokenTask extends AsyncTask<String, Void, String> {

	final private static String TAG = "RefreshAuthTakenTask";

	private MainActivity activity;
	private YggdrasilAuthenticator authenticator;

	private LoggedInUser loggedInUser;
	private String result;

	private Gson gson = new Gson();
	public RefreshAuthTokenTask(MainActivity activity) {
		this.activity = activity;
	}

	public void onPreExecute() {
	}

	public String doInBackground(String... args) {
		try {
			authenticator = new YggdrasilAuthenticator(activity);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
			if(args[0] == ""){
				Log.e(TAG, "accessToken is empty");
				return "AccessToken is empty,please use LoginTask.";
			}
			if(args[1] == ""){
				Log.e(TAG, "clientId is empty");
				return "ClientToken is empty,please use LoginTask.";
			}
			RefreshResponse response = authenticator.refresh(args[0],
					UUID.fromString(args[1]));
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


			loggedInUser = new LoggedInUser(args[2], args[3], response.getClientToken(), response.getAccessToken(), response.getSelectedProfile(), response.getAvailableProfiles());

			prefs.edit().
				putString("auth_clientToken", response.getClientToken().toString()).
				putString("auth_accessToken", response.getAccessToken()).
				putString("auth_profile_name", response.getSelectedProfile().name).
				putString("auth_profile_id", response.getSelectedProfile().id).
				apply();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	public void onPostExecute(String result) {
		if (result == null) {
			// success
			activity.refreshedToken = true;
			Log.d(TAG, "Refresh succeeded");
		} else {
			Log.w(TAG, "Refresh failed");
		}
		this.result = result;
	}

	public LoggedInUser getLoggedInUser() {
		return loggedInUser;
	}

	public String getResult() {
		return result;
	}
}
