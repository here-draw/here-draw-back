package org.cmccx.src.articlecompilation;

import org.cmccx.utils.JwtService;
import org.cmccx.utils.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleCompilationService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArticleCompilationProvider articleCompilationProvider;
    private final ArticleCompilationDao articleCompilationDao;
    private final JwtService jwtService;
    private final S3Service s3Service;

    @Autowired
    public ArticleCompilationService(ArticleCompilationProvider articleCompilationProvider, ArticleCompilationDao articleCompilationDao, JwtService jwtService, S3Service s3Service) {
        this.articleCompilationProvider = articleCompilationProvider;
        this.articleCompilationDao = articleCompilationDao;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
    }
}
