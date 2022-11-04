package org.cmccx.src.art.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetArtByArtIdRes {
    private long artistId;
    private String artImage;
    private int width;
    private int height;
    private String title;
    private int price;
    private boolean exclusive;
    private int additionalCharge;
    private String simpleDescription;
    private String description;
    private int like;
    private List<String> filetype;
    private List<String> copyright;
    private List<String> tag;
    private boolean sales;
    private boolean myArt;

    public GetArtByArtIdRes(long userId, String artImage, int width, int height, String title, int price, String exclusive, int additionalCharge,String simpleDescription, String description, int like, String status) {
        this.artistId = userId;
        this.artImage = artImage;
        this.width = width;
        this.height = height;
        this.title = title;
        this.price = price;
        this.exclusive = isExclusive(exclusive);
        this.additionalCharge = additionalCharge;
        this.simpleDescription = simpleDescription;
        this.description = description;
        this.like = like;
        this.sales = isSales(status);
    }

    private boolean isSales(String status){
        if (status.equals("F")) {
            return false;
        }
        return true;
    }

    private boolean isExclusive(String status){
        if (status.equals("N")) {
            return false;
        }
        return true;
    }
}
