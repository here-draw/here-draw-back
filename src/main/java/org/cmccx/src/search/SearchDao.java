package org.cmccx.src.search;

import org.cmccx.src.search.model.GetPopularArtistsRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class SearchDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 최근 검색어 조회 **/
    public List<String> selectRecentKeywords(long userId) {
        String query = "SELECT keyword FROM recent_keyword WHERE user_id = ? ORDER BY updated_at DESC";
        return this.jdbcTemplate.queryForList(query, String.class, userId);
    }

    /** 최근 검색어 전체 삭제 **/
    public int deleteRecentKeywords(long userId) {
        String query = "DELETE FROM recent_keyword WHERE user_id = ?";
        return this.jdbcTemplate.update(query, userId);
    }

    /** 인기 작가 목록 조회 **/
    public List<GetPopularArtistsRes> selectPopularArtists() {
        String query = "SELECT user_id, nickname, profile_image " +
                        "FROM profile " +
                        "INNER JOIN (SELECT seller_id, created_at, COUNT(*) AS ranking FROM purchase_history GROUP BY seller_id) rankTB ON user_id = seller_id " +
                        "WHERE status = 'A' " +
                        "ORDER BY ranking DESC, rankTB.created_at DESC " +
                        "LIMIT 3";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetPopularArtistsRes(
                        rs.getLong("user_id"),
                        rs.getString("nickname"),
                        rs.getString("profile_image"))
        );
    }

}
