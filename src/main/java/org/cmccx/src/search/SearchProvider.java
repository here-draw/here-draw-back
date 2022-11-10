package org.cmccx.src.search;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.search.model.ArtistInfo;
import org.cmccx.src.search.model.GetArtistsByKeywordRes;
import org.cmccx.src.search.model.GetArtsByKeywordRes;
import org.cmccx.src.search.model.GetPopularArtistsRes;
import org.cmccx.utils.JwtService;
import org.cmccx.utils.ScrollPagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.RESPONSE_ERROR;

@Service
public class SearchProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SearchDao searchDao;
    private final JwtService jwtService;

    @Autowired
    public SearchProvider(SearchDao searchDao, JwtService jwtService) {
        this.searchDao = searchDao;
        this.jwtService = jwtService;
    }

    /** 최근 검색어 조회 **/
    public List<String> getRecentKeywords() throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            List<String> result = searchDao.selectRecentKeywords(userId);
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetRecentKeywords Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 인기 작가 목록 조회 **/
    public List<GetPopularArtistsRes> getPopularArtists() throws BaseException {
        try {
            List<GetPopularArtistsRes> result = searchDao.selectPopularArtists();
            return result;
        } catch (Exception e) {
            logger.error("GetPopularArtists Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 작품 검색 결과 조회 **/
    public GetArtsByKeywordRes getArtsByKeyword(String keyword, long artId, String date, int size) throws BaseException {
        try {
            List<ArtInfo> artInfoList = searchDao.selectArtsByKeyword(keyword, artId, date, size);
            ScrollPagination<ArtInfo> scrollInfo = ScrollPagination.of(artInfoList, size);
            GetArtsByKeywordRes result = GetArtsByKeywordRes.of(scrollInfo);

            return result;
        } catch (Exception e) {
            logger.error("GetArtsByKeyword", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 작품 검색 결과 조회 **/
    public GetArtistsByKeywordRes getArtistsByKeyword(String keyword, long artistId, int size) throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            // 작가 검색 결과 수
            int count = searchDao.getArtistsSearchCount(keyword);

            // 검색 결과
            List<ArtistInfo> artistInfoList = searchDao.selectArtistsByKeyword(userId, keyword, artistId, size);
            ScrollPagination<ArtistInfo> scrollInfo = ScrollPagination.of(artistInfoList, size);
            GetArtistsByKeywordRes result = GetArtistsByKeywordRes.of(scrollInfo);
            result.setCount(count);

            return result;
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetArtistsByKeyword", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 작가 검색 결과 개수 반환 **/
    public int getArtistSearchCount(String keyword) throws BaseException {
        try {
            int result = searchDao.getArtistsSearchCount(keyword);
            return result;
        } catch (Exception e) {
            logger.error("GetArtistSearchCount", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
