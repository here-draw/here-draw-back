package org.cmccx.src.art.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"artId", "artistId", "artImage", "width", "height", "title"})
public class ArtInfo {
    private long artId;

    private long artistId;

    private String artImage;

    @JsonIgnore
    private String date;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int width;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int height;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int price;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean like;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean sales;

    // 메인: 작품 조회
    public ArtInfo(long artId, long artistId, String artImage, int width, int height, String title, String date) {
        this.artId = artId;
        this.artistId = artistId;
        this.artImage = artImage;
        this.width = width;
        this.height = height;
        this.title = title;
        this.date = date;
    }

    // 추천 작품 조회
    public ArtInfo(long artId, long artistId, String artImage, int count) {
        this.artId = artId;
        this.artistId = artistId;
        this.artImage = artImage;
        this.like = isLike(count);
    }

    // 작가별 작품 조회
    public ArtInfo(long artId, long artistId, String artImage, String title, int price, int count, String status) {
        this.artId = artId;
        this.artistId = artistId;
        this.artImage = artImage;
        this.title = title;
        this.price = price;
        this.like = isLike(count);
        this.sales = isSales(status);
    }

    // 구매한 작품 조회
    public ArtInfo(long artId, long artistId, String artImage, String title, int price) {
        this.artId = artId;
        this.artistId = artistId;
        this.artImage = artImage;
        this.title = title;
        this.price = price;
    }

    // 최근 본 작품 조회
    public ArtInfo(long artId, long artistId, String artImage) {
        this.artId = artId;
        this.artistId = artistId;
        this.artImage = artImage;
    }

    private boolean isLike(int count){
        if (count == 0) {
            return false;
        }
        return true;
    }

    private boolean isSales(String status){
        if (status.equals("F") || status.equals("E")) {
            return false;
        }
        return true;
    }
}
