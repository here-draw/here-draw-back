package org.cmccx.src.search;

import org.cmccx.config.BaseException;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class SearchService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SearchProvider searchProvider;
    private final SearchDao searchDao;
    private final JwtService jwtService;

    @Autowired
    public SearchService(SearchProvider searchProvider, SearchDao searchDao, JwtService jwtService) {
        this.searchProvider = searchProvider;
        this.searchDao = searchDao;
        this.jwtService = jwtService;
    }

    /** 최근 검색어 전체 삭제 **/
    public String removeRecentKeywords() throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 삭제
            searchDao.deleteRecentKeywords(userId);

            String message = "삭제되었습니다.";
            return message;

        } catch (BaseException e){
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("DeleteRecentKeywords Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
