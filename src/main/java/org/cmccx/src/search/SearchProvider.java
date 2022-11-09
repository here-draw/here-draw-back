package org.cmccx.src.search;

import org.cmccx.config.BaseException;
import org.cmccx.src.search.model.GetPopularArtistsRes;
import org.cmccx.utils.JwtService;
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
}
