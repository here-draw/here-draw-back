package org.cmccx.src.articlecompilation;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.articlecompilation.model.GetArticleBannerRes;
import org.cmccx.src.articlecompilation.model.GetArticleByIdRes;
import org.cmccx.src.articlecompilation.model.GetArticlesRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleCompilationController {

    private final ArticleCompilationProvider articleCompilationProvider;
    private final ArticleCompilationService articleCompilationService;

    @Autowired
    public ArticleCompilationController(ArticleCompilationProvider articleCompilationProvider, ArticleCompilationService articleCompilationService) {
        this.articleCompilationProvider = articleCompilationProvider;
        this.articleCompilationService = articleCompilationService;
    }

    /**
     * 홈 배너 목록 조회 API
     * [GET] /articles/banners
     * @return BaseResponse<List<GetArticleBannerRes>>
     */
    @ResponseBody
    @GetMapping("/banners")
    public BaseResponse<List<GetArticleBannerRes>> getArticleBanners() throws BaseException {
        List<GetArticleBannerRes> result = articleCompilationProvider.getArticleBanners();
        return new BaseResponse<>(result);
    }

    /**
     * 아티클 컴필레이션 목록 조회 API
     * [GET] /articles
     * @return BaseResponse<List<GetArticlesRes>>
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetArticlesRes>> getArticles() throws BaseException {
        List<GetArticlesRes> result = articleCompilationProvider.getArticles();
        return new BaseResponse<>(result);
    }

    /**
     * 아티클 컴필레이션 상세 조회 API
     * [GET] /articles/{article-id}
     * @return BaseResponse<List<GetArticlesRes>>
     */
    @ResponseBody
    @GetMapping("/{article-id}")
    public BaseResponse<GetArticleByIdRes> getArticleById(@PathVariable("article-id") long articleId) throws BaseException {
        GetArticleByIdRes result = articleCompilationProvider.getArticleById(articleId);
        return new BaseResponse<>(result);
    }
}
