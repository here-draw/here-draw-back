package org.cmccx.src.user.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonPropertyOrder({"profileImg","nickname","description","followerCnt","likeCnt","isFollowing","hasArticle"})
public class ArtistInfo {
    private String profileImg;
    private String nickname;
    private String description;
    private int followerCnt;
    private int likeCnt;
    private Boolean isFollowing;
    private Boolean hasArticle;
}
