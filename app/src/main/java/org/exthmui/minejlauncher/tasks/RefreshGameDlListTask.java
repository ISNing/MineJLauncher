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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class RefreshGameDlListTask extends AsyncTask<String, Void, String> {

	final private static String TAG = "RefreshAuthTakenTask";

	private MainActivity activity;
	private YggdrasilAuthenticator authenticator;

	private LoggedInUser loggedInUser;
	private String result;

	private Gson gson = new Gson();
	public RefreshGameDlListTask(MainActivity activity) {
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

/*
			private void downloadUpdatesList(final boolean manualRefresh) {
				final File jsonFile = Utils.getCachedUpdateList(this);
				final File jsonFile2 = Utils.getCachedNoticeList(this);
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

				DownloadClient.DownloadCallback callback2 = new DownloadClient.DownloadCallback() {
					@Override
					public void onFailure(final boolean cancelled) {
						Log.e(TAG, "Could not org.jackhuang.hmcl.download updates list");
						runOnUiThread(() -> {
							if (!cancelled) {
								showSnackbar(R.string.snack_notices_check_failed, Snackbar.LENGTH_LONG);
							}
							refreshAnimationStop();
						});
					}

					@Override
					public void onResponse(int statusCode, String url, DownloadClient. Headers headers) {
					}

					@Override
					public void onSuccess(File destination) {
						runOnUiThread(() -> {
							Log.d(TAG, "Notice list downloaded");
							processNewJson(jsonFile2, jsonFileTmp2, manualRefresh, false);
							refreshAnimationStop();
						});
					}
				};

				final DownloadClient downloadClient;
				final DownloadClient downloadClient2;
				try {
					downloadClient = new DownloadClient.Builder()
							.setUrl(url)
							.setDestination(jsonFileTmp)
							.setDownloadCallback(callback)
							.build();
					downloadClient2 = new DownloadClient.Builder()
							.setUrl(url2)
							.setDestination(jsonFileTmp2)
							.setDownloadCallback(callback2)
							.build();
				} catch (IOException exception) {
					Log.e(TAG, "Could not build org.jackhuang.hmcl.download client");
					showSnackbar(R.string.snack_updates_check_failed, Snackbar.LENGTH_LONG);
					return;
				}

				refreshAnimationStart();
				downloadClient.start();
				downloadClient2.start();
			}*/
			loggedInUser = new LoggedInUser(args[2], args[3], response.getClientToken(), response.getAccessToken(), response.getSelectedProfile(), response.getAvailableProfiles());
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
