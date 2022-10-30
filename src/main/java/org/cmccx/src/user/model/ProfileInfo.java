package org.cmccx.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileInfo {
    private String profileImage;
    private String nickname;
    private String description;
}
