package org.cmccx.src.user.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleToken {
    private String access_token;
    private String expires_in;
    private String id_token;
    private String refresh_token;
    private String token_type;
    private String error;

    public String getAccessToken() {
        return access_token;
    }
}
