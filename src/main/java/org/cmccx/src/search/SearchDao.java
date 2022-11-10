package org.cmccx.src.search;

import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.search.model.ArtistInfo;
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

    /** 작품 검색 **/
    public List<ArtInfo> selectArtsByKeyword(String keyword, long artId, String date, int size) {
        String query = "SELECT art_id, user_id, art_image, image_width, image_height, title, created_at " +
                        "FROM art " +
                        "WHERE (MATCH(title) AGAINST(? IN BOOLEAN MODE) AND status IN ('S', 'F', 'E')) " +
                        "AND ((created_at = ? AND art_id > ?) " +
                        "OR created_at < ?) " +
                        "ORDER BY created_at DESC, art_id " +
                        "LIMIT ?";

        Object[] params = new Object[] {keyword, date, artId, date, size + 1};

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ArtInfo(
                        rs.getLong("art_id"),
                        rs.getLong("user_id"),
                        rs.getString("art_image"),
                        rs.getInt("image_width"),
                        rs.getInt("image_height"),
                        rs.getString("title"),
                        rs.getString("created_at")),
                params);
    }

    /** 작가 검색 **/
    public List<ArtistInfo> selectArtistsByKeyword(long userId, String keyword, long artistId, int size) {
        String query = "SELECT  user_id, profile_image, nickname, IF(follower_id = ? AND f.status = 'A', TRUE, FALSE) AS likes " +
                        "FROM profile " +
                        "LEFT JOIN follow f on profile.user_id = f.target_user_id " +
                        "WHERE (MATCH(nickname) AGAINST(? IN BOOLEAN MODE) AND profile.status = 'A') " +
                        "AND user_id > ? " +
                        "ORDER BY user_id " +
                        "LIMIT ?";
        Object[] params = new Object[] {userId, keyword, artistId, size + 1};

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ArtistInfo(
                        rs.getLong("user_id"),
                        rs.getString("profile_image"),
                        rs.getString("nickname"),
                        rs.getBoolean("likes")),
                params);
    }

    /** 작가 검색 결과 개수 조회 **/
    public int getArtistsSearchCount(String keyword) {
        String query = "SELECT COUNT(*) FROM profile " +
                        "WHERE (MATCH(nickname) AGAINST(? IN BOOLEAN MODE) AND profile.status = 'A')";
        return this.jdbcTemplate.queryForObject(query, int.class, keyword);
    }
}
