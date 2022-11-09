package org.cmccx.src.articlecompilation;

import org.cmccx.config.BaseException;
import org.cmccx.src.articlecompilation.model.GetArticleBannerRes;
import org.cmccx.src.articlecompilation.model.GetArticleByIdRes;
import org.cmccx.src.articlecompilation.model.GetArticlesRes;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.FAILED_ACCESS_ARTICLE;
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

    /** 아티클 컴필레이션 유효성 확인 **/
    public boolean checkArticleId(long articleId) throws BaseException {
        try {
            return articleCompilationDao.checkArticleId(articleId);
        } catch (Exception e){
            logger.error("CheckArticleId Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
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

    /** 아티클 컴필레이션 목록 조회 **/
    public List<GetArticlesRes> getArticles() throws BaseException {
        try {
            return articleCompilationDao.selectArticles();
        } catch (Exception e) {
            logger.error("GetArticles Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 아티클 컴필레이션 상세 조회 **/
    public GetArticleByIdRes getArticleById(long articleId) throws BaseException {
        try {
            // 아티클ID 유효성 확인
            boolean isValid = articleCompilationDao.checkArticleId(articleId);
            if (!isValid) {
                throw new BaseException(FAILED_ACCESS_ARTICLE);
            }

            return articleCompilationDao.selectArticleById(articleId);
        } catch (Exception e) {
            logger.error("GetArticleById Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
