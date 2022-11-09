package org.cmccx.src.articlecompilation.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetArticleByIdRes {
    private long artistId;
    private String mainImage;
    private String subHeading;
    private String articleTitle;
    private String description;
}
