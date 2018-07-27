package com.jamie.releaser;

import java.util.Base64;

public class GithubAuthentication {
    private String accessToken;
    private String username;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBasicToken() {
        return Base64.getEncoder().encodeToString((username + ':' + accessToken).getBytes());
    }
}
