package org.exthmui.minejlauncher.data.model;

import org.exthmui.minejlauncher.auth.AuthenticateResponse;
import org.exthmui.minejlauncher.auth.Profile;

import java.util.UUID;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser extends AuthenticateResponse {

    private String username;
    private String password;

    public LoggedInUser(String username, String password, UUID clientToken, String accessToken, Profile selectedProfile, Profile[] availableProfiles) {
        this.username = username;
        this.password = password;
        this.setClientToken(clientToken);
        this.setAccessToken(accessToken);
        this.setSelectedProfile(selectedProfile);
        this.setAvailableProfiles(availableProfiles);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
