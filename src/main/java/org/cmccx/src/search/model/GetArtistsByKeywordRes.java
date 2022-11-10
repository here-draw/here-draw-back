package org.cmccx.src.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cmccx.utils.ScrollPagination;

import java.util.List;

@Getter
@Setter
public class GetArtistsByKeywordRes {
    private boolean hasNextScroll;
    private long artistIdCursor;
    private int count;
    private List<ArtistInfo> artistInfoList;

    public GetArtistsByKeywordRes(boolean hasNextScroll, long artistIdCursor, List<ArtistInfo> artistInfoList) {
        this.hasNextScroll = hasNextScroll;
        this.artistIdCursor = artistIdCursor;
        this.artistInfoList = artistInfoList;
    }

    public static GetArtistsByKeywordRes of(ScrollPagination<ArtistInfo> scrollInfo){
        if (scrollInfo.hasNextScroll()){
            return new GetArtistsByKeywordRes(
                    scrollInfo.hasNextScroll(),
                    scrollInfo.getNextCursor().getArtistId(),
                    scrollInfo.getCurrentScrollItems());
        }
        return new GetArtistsByKeywordRes(
                scrollInfo.hasNextScroll(),
                -1L,
                scrollInfo.getCurrentScrollItems());
    }
}
