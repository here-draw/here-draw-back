package org.cmccx.src.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArtistInfo {
    private long artistId;
    private String profileImage;
    private String nickname;
    private boolean follow;
}
