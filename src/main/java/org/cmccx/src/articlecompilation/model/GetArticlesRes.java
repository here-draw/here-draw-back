package org.cmccx.src.articlecompilation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetArticlesRes {
    private long articleId;
    private long artistId;
    private String thumbnailImage;
    private String articleTitle;
    private String artistName;
    private String description;
}
