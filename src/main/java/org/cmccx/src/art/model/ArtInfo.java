package org.cmccx.src.art.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class ArtInfo {
    private long artId;

    private String artImage;

    @JsonIgnore
    private String date;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int price;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean like;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean sales;

    public ArtInfo(long artId, String artImage, String title, String date) {
        this.artId = artId;
        this.artImage = artImage;
        this.title = title;
        this.date = date;
    }

    public ArtInfo(long artId, String artImage, int count) {
        this.artId = artId;
        this.artImage = artImage;
        this.like = isLike(count);
    }

    public ArtInfo(long artId, String artImage, String title, int price, int count, String status){
        this.artId = artId;
        this.artImage = artImage;
        this.title = title;
        this.price = price;
        this.like = isLike(count);
        this.sales = isSales(status);
    }

    private boolean isLike(int count){
        if (count == 0) {
            return false;
        }
        return true;
    }

    private boolean isSales(String status){
        if (status.equals("F")) {
            return false;
        }
        return true;
    }
}
