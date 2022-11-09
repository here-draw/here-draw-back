package org.cmccx.src.articlecompilation;

import org.cmccx.src.articlecompilation.model.GetArticleBannerRes;
import org.cmccx.src.articlecompilation.model.GetArticleByIdRes;
import org.cmccx.src.articlecompilation.model.GetArticlesRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ArticleCompilationDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 아티클 컴필레이션 유효성 확인 **/
    public boolean checkArticleId(long articleId) {
        String query = "SELECT EXISTS(SELECT 1 FROM article_compilation WHERE article_compilation_id = ? AND status <> 'I')";
        return this.jdbcTemplate.queryForObject(query, Boolean.class, articleId);
    }

    /** 홈: 아티클 컴필레이션 배너 이미지 조회 **/
    public List<GetArticleBannerRes> selectArticleBanners(){
        String query = "SELECT article_compilation_id, banner_image\n" +
                        "FROM article_compilation\n" +
                        "WHERE status = 'B'";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetArticleBannerRes(
                        rs.getLong("article_compilation_id"),
                        rs.getString("banner_image"))
        );
    }

    /** 아티클 컴필레이션 목록 조회 **/
    public List<GetArticlesRes> selectArticles() {
        String query = "SELECT article_compilation_id, user_id, thumbnail_image, title, article_compilation.name, LEFT(description, 90) AS description " +
                        "FROM article_compilation " +
                        "WHERE status <> 'I' " +
                        "ORDER BY created_at DESC";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetArticlesRes(
                        rs.getLong("article_compilation_id"),
                        rs.getLong("user_id"),
                        rs.getString("thumbnail_image"),
                        rs.getString("title"),
                        rs.getString("article_compilation.name"),
                        rs.getString("description"))
                );
    }

    /** 아티클 컴필레이션 상세 조회 **/
    public GetArticleByIdRes selectArticleById(long articleId) {
        String query = "SELECT user_id, artist_image, subheading, title, description " +
                        "FROM article_compilation " +
                        "WHERE article_compilation_id = ?";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetArticleByIdRes(
                        rs.getLong("user_id"),
                        rs.getString("artist_image"),
                        rs.getString("subheading"),
                        rs.getString("title"),
                        rs.getString("description")),
                articleId);
    }
}
