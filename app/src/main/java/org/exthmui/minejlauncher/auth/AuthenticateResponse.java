package org.exthmui.minejlauncher.auth;
import java.util.UUID;

public class AuthenticateResponse {
	private String accessToken;
	private UUID clientToken;
	private Profile[] availableProfiles;
	private Profile selectedProfile;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public UUID getClientToken() {
		return clientToken;
	}

	public void setClientToken(UUID clientToken) {
		this.clientToken = clientToken;
	}

	public Profile[] getAvailableProfiles() {
		return availableProfiles;
	}

	public void setAvailableProfiles(Profile[] availableProfiles) {
		this.availableProfiles = availableProfiles;
	}

	public Profile getSelectedProfile() {
		return selectedProfile;
	}

	public void setSelectedProfile(Profile selectedProfile) {
		this.selectedProfile = selectedProfile;
	}
}
