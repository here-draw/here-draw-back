package org.cmccx.src.art.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetArtsByUserRes {
    private int count;
    private List<ArtInfo> artList;
}
