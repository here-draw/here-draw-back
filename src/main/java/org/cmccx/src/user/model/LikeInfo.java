package org.cmccx.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LikeInfo {
    private int followerCnt;
    private int followingCnt;
    private int likeCnt;
}
