package org.cmccx.src.gallery;

import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.gallery.model.GetGalleriesRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class GalleryDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 갤러리-회원 관계 확인 **/
    public boolean checkGalleryByUserId(long userId, long galleryId) {
        String query = "SELECT EXISTS(SELECT 1 FROM gallery WHERE user_id = ? AND gallery_id =?)";
        return this.jdbcTemplate.queryForObject(query, Boolean.class, userId, galleryId);
    }

    /** 갤러리명 중복 확인 **/
    public boolean checkGalleryName(long userId, String name) {
        String query = "SELECT EXISTS(SELECT  1 FROM gallery WHERE user_id = ? AND name = ?)";
        return this.jdbcTemplate.queryForObject(query, Boolean.class, userId, name);
    }

    /** 작품 찜 상태 확인 **/
    public boolean checkBookmarkStatus(long galleryId, long artId) {
        String query = "SELECT EXISTS(SELECT 1 FROM bookmark WHERE gallery_id = ? AND art_id = ?)";
        return this.jdbcTemplate.queryForObject(query, Boolean.class, galleryId, artId);
    }

    /** 갤러리 목록 조회 **/
    public List<GetGalleriesRes> selectGalleries(long userId) {
        String query = "SELECT g.gallery_id, `name`, IFNULL(cnt, 0) AS cnt " +
                        "FROM gallery g " +
                        "LEFT JOIN (SELECT gallery_id, COUNT(*) AS cnt  FROM bookmark " +
                        "INNER JOIN art a ON bookmark.art_id = a.art_id " +
                        "WHERE a.status IN ('S', 'F', 'E') GROUP BY gallery_id) b ON b.gallery_id = g.gallery_id " +
                        "WHERE user_id = ?";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetGalleriesRes(
                        rs.getLong("g.gallery_id"),
                        rs.getString("name"),
                        rs.getInt("cnt")),
                userId);
    }

    /** 갤러리별 대표이미지 목록 조회 **/
    public List<String> selectGalleyImages(long galleryId) {
        String query = "SELECT art_image " +
                        "FROM bookmark " +
                        "INNER JOIN art a on bookmark.art_id = a.art_id " +
                        "WHERE gallery_id = ? AND a.status IN ('S', 'F', 'E') " +
                        "ORDER BY bookmark_id DESC " +
                        "LIMIT 4";
        return this.jdbcTemplate.queryForList(query, String.class, galleryId);
    }

    /** 갤러리 내 작품 목록 조회 **/
    public List<ArtInfo> selectArtsByGalleryId(long galleryId) {
        String query = "SELECT bookmark.art_id, a.user_id, art_image, title, price, 1 AS cnt, a.status " +
                "FROM bookmark " +
                "INNER JOIN gallery g on bookmark.gallery_id = g.gallery_id " +
                "INNER JOIN art a on bookmark.art_id = a.art_id " +
                "WHERE g.gallery_id = ? AND a.status IN ('S', 'F', 'E')";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ArtInfo(
                        rs.getLong("bookmark.art_id"),
                        rs.getLong("a.user_id"),
                        rs.getString("art_image"),
                        rs.getString("title"),
                        rs.getInt("price"),
                        rs.getInt("cnt"),
                        rs.getString("a.status")),
                galleryId);
    }

    /** 갤러리 생성 **/
    public long insertGallery(long userId, String name) {
        String query = "INSERT INTO gallery(user_id, name) VALUES (?, ?)";
        String idQuery = "SELECT last_insert_id()";

        this.jdbcTemplate.update(query, userId, name);
        return this.jdbcTemplate.queryForObject(idQuery, Long.class);
    }

    /** 갤러리명 수정 **/
    public int updateGalleryName(long galleryId, String name) {
        String query = "UPDATE gallery SET name = ? WHERE gallery_id = ?";

        return this.jdbcTemplate.update(query, name, galleryId);
    }

    /** 갤러리 삭제 **/
    public int deleteGalley(long galleryId) {
        String query = "DELETE FROM gallery WHERE gallery_id = ?";

        return this.jdbcTemplate.update(query, galleryId);
    }

    /** 찜 기능 **/
    public void insertBookmark(long galleryId, long artId) {
        String query = "INSERT INTO bookmark(gallery_id, art_id) VALUES (?, ?)";
        this.jdbcTemplate.update(query, galleryId, artId);
    }

    /** 찜 해제 **/
    public void deleteBookmark(long galleryId, long artId) {
        String query = "DELETE FROM bookmark WHERE gallery_id = ? AND art_id = ?;";
        this.jdbcTemplate.update(query, galleryId, artId);
    }
}
