package org.cmccx.src.art.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetArtByArtIdRes {
    private long artistId;
    private String artImage;
    private String title;
    private int price;
    private String simpleDescription;
    private String description;
    private int like;
    private List<String> filetype;
    private List<String> copyright;
    private List<String> tag;

    public GetArtByArtIdRes(long userId, String artImage, String title, int price, String simpleDescription, String description, int like) {
        this.artistId = userId;
        this.artImage = artImage;
        this.title = title;
        this.price = price;
        this.simpleDescription = simpleDescription;
        this.description = description;
        this.like = like;
    }
}
