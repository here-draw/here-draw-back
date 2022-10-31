package org.cmccx.src.articlecompilation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetArticleBannerRes {
    private long articleId;
    private String bannerImage;
}
