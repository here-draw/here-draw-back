package org.cmccx.src.articlecompilation;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.articlecompilation.model.GetArticleBannerRes;
import org.cmccx.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
}
