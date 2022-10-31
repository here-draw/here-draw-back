package org.cmccx.src.articlecompilation;

import org.cmccx.config.BaseException;
import org.cmccx.src.articlecompilation.model.GetArticleBannerRes;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.RESPONSE_ERROR;

@Service
public class ArticleCompilationProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArticleCompilationDao articleCompilationDao;
    private final JwtService jwtService;

    @Autowired
    public ArticleCompilationProvider(ArticleCompilationDao articleCompilationDao, JwtService jwtService) {
        this.articleCompilationDao = articleCompilationDao;
        this.jwtService = jwtService;
    }

    /** 홈: 아티클 컴필레이션 배너 이미지 조회 **/
    public List<GetArticleBannerRes> getArticleBanners() throws BaseException {
        try {
            return articleCompilationDao.selectArticleBanners();
        } catch (Exception e){
            logger.error("GetArticleBanners Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

}
