package org.cmccx.src.art.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cmccx.utils.ScrollPagination;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetArtsRes {
    private boolean hasNextScroll;
    private long artIdCursor;
    private String dateCursor;
    private List<ArtInfo> artInfoList;

    public static GetArtsRes of(ScrollPagination<ArtInfo> scrollInfo){
        if (scrollInfo.hasNextScroll()){
            return new GetArtsRes(
                    scrollInfo.hasNextScroll(),
                    scrollInfo.getNextCursor().getArtId(),
                    scrollInfo.getNextCursor().getDate(),
                    scrollInfo.getCurrentScrollItems());
        }
        return new GetArtsRes(
                scrollInfo.hasNextScroll(),
                -1L,
                "",
                scrollInfo.getCurrentScrollItems());
    }
}
