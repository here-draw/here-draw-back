package org.cmccx.src.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPopularArtistsRes {
    private long artistId;
    private String nickname;
    private String profileImage;
}
