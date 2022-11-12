package org.cmccx.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ArtistInfo {
    private String profileImg;
    private String nickname;
    private String description;
    private int followerCnt;
    private int likeCnt;
    private boolean isFollowing;
}
