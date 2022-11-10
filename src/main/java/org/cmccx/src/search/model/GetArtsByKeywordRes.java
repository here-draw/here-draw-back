package org.cmccx.src.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.utils.ScrollPagination;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetArtsByKeywordRes {
    private boolean hasNextScroll;
    private long artIdCursor;
    private String dateCursor;
    private List<ArtInfo> artInfoList;

    public static GetArtsByKeywordRes of(ScrollPagination<ArtInfo> scrollInfo){
        if (scrollInfo.hasNextScroll()){
            return new GetArtsByKeywordRes(
                    scrollInfo.hasNextScroll(),
                    scrollInfo.getNextCursor().getArtId(),
                    scrollInfo.getNextCursor().getDate(),
                    scrollInfo.getCurrentScrollItems());
        }
        return new GetArtsByKeywordRes(
                scrollInfo.hasNextScroll(),
                -1L,
                "",
                scrollInfo.getCurrentScrollItems());
    }
}
