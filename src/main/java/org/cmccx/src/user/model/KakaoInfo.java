package org.cmccx.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KakaoInfo {
    private String email;
    private String profileImage;

    public KakaoInfo() {
        this.email = null;
        this.profileImage = null;
    }
}
