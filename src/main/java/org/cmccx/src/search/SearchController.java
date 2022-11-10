package org.cmccx.src.search;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.search.model.GetArtistsByKeywordRes;
import org.cmccx.src.search.model.GetArtsByKeywordRes;
import org.cmccx.src.search.model.GetPopularArtistsRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/search")
@Validated
public class SearchController {
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
     * 작품 검색 결과 조회 API
     * [GET] /search/arts?keyword={keyword}&id={id}&date={date}&size={size}
     * @return BaseResponse<GetArtsByKeywordRes>
     */
    @ResponseBody
    @GetMapping("/arts")
    public BaseResponse<GetArtsByKeywordRes> getArtsByKeyword(@RequestParam(value = "keyword") @NotBlank(message = "두 글자 이상 입력하세요.")  @Size(min = 2, message = "두 글자 이상 입력하세요.") String keyword,
                                                              @RequestParam(value = "id", defaultValue = "0") long artId,
                                                              @RequestParam(value = "date", defaultValue = "") String date,
                                                              @RequestParam(value = "size", defaultValue = "10000") int size) throws BaseException {
        // 다음날로 날짜 지정
        if (date.isEmpty()){
            date = LocalDate.now().plusDays(1).toString();
        }

        GetArtsByKeywordRes result = searchProvider.getArtsByKeyword(keyword, artId, date, size);
        return new BaseResponse<>(result);
    }

    /**
     * 작가 검색 결과 조회 API
     * [GET] /search/artists?keyword={keyword}&id={id}&size={size}
     * @return BaseResponse<GetArtistsByKeywordRes>
     */
    @ResponseBody
    @GetMapping("/artists")
    public BaseResponse<GetArtistsByKeywordRes> getArtistsByKeyword(@RequestParam(value = "keyword") @NotBlank(message = "두 글자 이상 입력하세요.") @Size(min = 2, message = "두 글자 이상 입력하세요.") String keyword,
                                                                 @RequestParam(value = "id", defaultValue = "0") long artistId,
                                                                 @RequestParam(value = "size", defaultValue = "10000") int size) throws BaseException {
        GetArtistsByKeywordRes result = searchProvider.getArtistsByKeyword(keyword, artistId, size);
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
