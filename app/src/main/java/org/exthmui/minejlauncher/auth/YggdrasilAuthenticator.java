package org.exthmui.minejlauncher.auth;

import android.content.Context;

import androidx.preference.PreferenceManager;

import java.util.*;
import java.net.*;
import java.io.*;

import com.google.gson.Gson;

import org.exthmui.minejlauncher.Constants;
import org.exthmui.minejlauncher.MainActivity;
import org.exthmui.minejlauncher.R;
import org.exthmui.minejlauncher.misc.IoUtil;
import org.exthmui.minejlauncher.misc.Utils;

public class YggdrasilAuthenticator {

    final private static Context context = MainActivity.getMainActivity().getApplicationContext();
    private static final String TAG = "YggdrasilAuthenticator";

    private String api_url = Utils.getAPIUrl(context);
    private String clientName = "Minecraft";
    private int clientVersion = 1;
    private Gson gson = new Gson();

    public YggdrasilAuthenticator(Context context){

    }

    private <T> T makeRequest(String endpoint, Object inputObject, Class<T> responseClass, String apiurl) throws IOException {
        this.api_url = apiurl;
        return makeRequest(endpoint, inputObject, responseClass);
    }

    private <T> T makeRequest(String endpoint, Object inputObject, Class<T> responseClass) throws IOException {
        InputStream is = null;
        HttpURLConnection conn;
        byte[] buf = new byte[0x4000];
        int statusCode = -1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String requestJson = gson.toJson(inputObject);
        URL url;

        try {
            url = new URL(api_url + "/" + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("auth_useragent", context.getString(R.string.auth_useragent)));
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();
            OutputStream os = null;
            try {
                os = conn.getOutputStream();
                os.write(requestJson.getBytes(Constants.utf8));
            } finally {
                if (os != null) os.close();
            }
            statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                is = conn.getErrorStream();
            } else {
                is = conn.getInputStream();
            }

            IoUtil.pipe(is, bos, buf);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }

        String outString = new String(bos.toByteArray(), Constants.utf8);

        if (statusCode != 200) {
            MainActivity.getMainActivity().authStatusCode = statusCode;
            //throw new RuntimeException("Status:" + statusCode + " Json:" + outString);
            return null;
        } else {
            T outResult = gson.fromJson(outString, responseClass);
            return outResult;
        }
    }

    public AuthenticateResponse authenticate(String username, String password, UUID clientId, String apiurl) throws IOException {
        AuthenticateRequest request = new AuthenticateRequest(username, password, clientId, clientName, clientVersion, apiurl);
        return makeRequest("authenticate", request, AuthenticateResponse.class);
    }

    public RefreshResponse refresh(String authToken, UUID clientId/*, Profile activeProfile*/) throws IOException {
        RefreshRequest request = new RefreshRequest(authToken, clientId/*, activeProfile*/);
        return makeRequest("refresh", request, RefreshResponse.class);
    }
}
