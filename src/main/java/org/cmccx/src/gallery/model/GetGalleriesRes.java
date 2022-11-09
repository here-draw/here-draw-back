package org.cmccx.src.gallery.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetGalleriesRes {
    private long galleryId;
    private List<String> galleryImages;
    private String galleryName;
    private int galleryCount;

    public GetGalleriesRes(long galleryId, String galleryName, int galleryCount) {
        this.galleryId = galleryId;
        this.galleryName = galleryName;
        this.galleryCount = galleryCount;
    }
}
