package org.exthmui.minejlauncher.auth;

import java.util.UUID;

public class AuthenticateRequest {
	public String username;
	public String password;
	public AgentInfo agent;
	public UUID clientToken;
	public String apiUrl;
	public static class AgentInfo {
		public String name;
		public int version;
	}

	public AuthenticateRequest(String username, String password, UUID clientToken, String clientName, int clientVersion, String apiurl) {
		this.username = username;
		this.password = password;
		this.clientToken = clientToken;
		this.apiUrl = apiurl;
		this.agent = new AgentInfo();
		agent.name = clientName;
		agent.version = clientVersion;
	}
}
