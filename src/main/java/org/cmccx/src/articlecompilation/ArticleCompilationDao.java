package org.cmccx.src.articlecompilation;

import org.cmccx.src.articlecompilation.model.GetArticleBannerRes;
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

}
