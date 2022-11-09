package org.cmccx.src.search;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.search.model.GetPopularArtistsRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SearchProvider searchProvider;
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchProvider searchProvider, SearchService searchService) {
        this.searchProvider = searchProvider;
        this.searchService = searchService;
    }

    /**
     * 인기 작가 목록 조회 API
     * [DELETE] /search/popular-artists
     * @return BaseResponse<List<GetPopularArtistsRes>>
     */
    @ResponseBody
    @GetMapping("/popular-artists")
    public BaseResponse<List<GetPopularArtistsRes>> getPopularArtists() throws BaseException {
        List<GetPopularArtistsRes> result = searchProvider.getPopularArtists();
        return new BaseResponse<>(result);
    }

    /**
     * 최근 검색어 조회 API
     * [GET] /search/recent-keywords
     * @return BaseResponse<List<String>>
     */
    @ResponseBody
    @GetMapping("/recent-keywords")
    public BaseResponse<List<String>> getRecentKeywords() throws BaseException {
        List<String> result = searchProvider.getRecentKeywords();
        return new BaseResponse<>(result);
    }

    /**
     * 최근 검색어 전체 삭제 API
     * [DELETE] /search/recent-keywords
     * @return BaseResponse<String>
     */
    @ResponseBody
    @DeleteMapping("/recent-keywords")
    public BaseResponse<String> removeRecentKeywords() throws BaseException {
        String result = searchService.removeRecentKeywords();
        return new BaseResponse<>(result);
    }
}
