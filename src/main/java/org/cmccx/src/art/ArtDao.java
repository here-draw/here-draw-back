package org.cmccx.src.art;

import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.art.model.GetArtByArtIdRes;
import org.cmccx.src.art.model.PostArtReq;
import org.cmccx.src.art.model.PutArtReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ArtDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 회원 ID 확인 **/
    public int checkUser(long userId){
        String query ="SELECT EXISTS(SELECT 1 FROM user WHERE user_id = ? AND status NOT IN ('D', 'P'))";
        return this.jdbcTemplate.queryForObject(query, int.class, userId);
    }

    /** 작품 ID 확인 **/
    public int checkArt(long artId){
        String query = "SELECT EXISTS(SELECT 1 FROM art WHERE art_id = ? AND status NOT IN ('B', 'D', 'N'))";
        return this.jdbcTemplate.queryForObject(query, int.class, artId);
    }

    /** 작품 상태 조회 **/
    public String checkArtStatus(long artId) {
        String query = "SELECT status FROM art WHERE art_id = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, artId);
    }

    /** 작가-작품 관계 확인 **/
    public int checkUserArt(long userId, long artId){
        String query = "SELECT EXISTS(SELECT 1 FROM art WHERE user_id = ? AND art_id = ? AND status NOT IN ('B', 'D', 'N'))";
        return this.jdbcTemplate.queryForObject(query, int.class, userId, artId);
    }

    /** 작가:작품명 중복 확인 **/
    public int checkArtTitle(long userId, String title, long artId){
        String query = "SELECT EXISTS(SELECT 1 FROM art WHERE (user_id = ? AND title = ?) AND art_id <> ? AND status <> 'D')";
        return this.jdbcTemplate.queryForObject(query, int.class, userId, title, artId);
    }

    /** 작품 판매수량 조회 **/
    public int selectSales(long artId) {
        String query = "SELECT sales_quantity FROM art WHERE art_id = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, artId);
    }

    /** 메인: 작품 조회(최신 등록순) **/
    public List<ArtInfo> selectArts(int categoryId, long artId, String date, int size){
        StringBuilder query = new StringBuilder("SELECT art_id, user_id, art_image, image_width, image_height, title, created_at FROM art ");
        Object[] params;

        if (categoryId == 0){   // 전체 조회
            query.append("WHERE (status = 'S') ");
            params = new Object[]{date, artId, date, size + 1};
        } else {    // 카테고리별 조회
            query.append("WHERE (category_type_id = ? AND status = 'S') ");
            params = new Object[]{categoryId ,date, artId, date, size + 1};
        }

        query.append("AND ((created_at = ? AND art_id > ?) ");
        query.append("OR created_at < ?) ");
        query.append("ORDER BY created_at DESC, art_id ");
        query.append("LIMIT ?");


        return this.jdbcTemplate.query(query.toString(),
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

    /** 작가별 작품 조회 **/
    public List<ArtInfo> selectArsByUserId(long userId, long artistId, boolean isMyPage, long artId, int size){
        StringBuilder query = new StringBuilder("SELECT art.art_id, art.user_id, art_image, title, price, IFNULL(user_like.art_id, 0)  AS count, art.status ");
        query.append("FROM art ");
        query.append("LEFT JOIN (SELECT art_id ");
        query.append("FROM gallery ");
        query.append("INNER JOIN bookmark b ON gallery.gallery_id = b.gallery_id ");
        query.append("WHERE user_id = ? ");
        query.append("GROUP BY art_id) user_like ON user_like.art_id = art.art_id ");
        query.append("WHERE art.user_id = ? AND art.art_id <> ? ");

        if (isMyPage){  // MYPage
            query.append("AND (status IN ('S', 'F', 'E')) ");
        } else {  // 작가 홈
            query.append("AND (status = 'S') ");
        }

        query.append("ORDER BY updated_at DESC ");
        query.append("LIMIT ?");

        Object[] params = new Object[]{userId, artistId, artId, size};

        return this.jdbcTemplate.query(query.toString(),
                (rs, rowNum) -> new ArtInfo(
                        rs.getLong("art.art_id"),
                        rs.getLong("art.user_id"),
                        rs.getString("art_image"),
                        rs.getString("title"),
                        rs.getInt("price"),
                        rs.getInt("count"),
                        rs.getString("status")),
                params);
    }


    /** 추천 작품 조회 **/
    public List<ArtInfo> selectRecommendedArts(long userId, long artId){
        String query = "SELECT art.art_id, art.user_id, art_image, IFNULL(user_like.art_id, 0) AS count " +
                        "FROM art " +
                        "LEFT JOIN (SELECT art_id " +
                        "FROM gallery " +
                        "INNER JOIN bookmark b ON gallery.gallery_id = b.gallery_id " +
                        "WHERE user_id = ? " +
                        "GROUP BY art_id) user_like ON user_like.art_id = art.art_id " +
                        "WHERE art.art_id <> ? AND status = 'S' " +
                        "ORDER BY rand() " +
                        "LIMIT 4";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ArtInfo(
                        rs.getLong("art.art_id"),
                        rs.getLong("art.user_id"),
                        rs.getString("art_image"),
                        rs.getInt("count")),
                userId, artId);
    }

    /** 작품 상세 정보 조회 **/
    public GetArtByArtIdRes selectArtByArtId(long artId){
        // 작품 및 작가 정보 조회
        String infoQuery = "SELECT art.user_id, art_image, image_width, image_height, title, price, exclusive_flag, additional_charge, simple_description, art.description, IFNULL(like_art, 0) AS like_art, status " +
                            "FROM art " +
                            "LEFT JOIN (SELECT COUNT(*) AS like_art, art_id " +
                            "FROM bookmark " +
                            "GROUP BY art_id) like_art_tb ON like_art_tb.art_id = art.art_id " +
                            "WHERE art.art_id = ? AND status NOT IN ('B', 'D', 'N')";

        GetArtByArtIdRes result =  this.jdbcTemplate.queryForObject(infoQuery,
                                    (rs, rowNum) -> new GetArtByArtIdRes(
                                            rs.getLong("art.user_id"),
                                            rs.getString("art_image"),
                                            rs.getInt("image_width"),
                                            rs.getInt("image_height"),
                                            rs.getString("title"),
                                            rs.getInt("price"),
                                            rs.getString("exclusive_flag"),
                                            rs.getInt("additional_charge"),
                                            rs.getString("simple_description"),
                                            rs.getString("art.description"),
                                            rs.getInt("like_art"),
                                            rs.getString("status")),
                                    artId);

        // 파일 유형 조회
        String fileQuery = "SELECT type FROM file " +
                            "INNER JOIN file_type ft ON file.file_type_id = ft.file_type_id " +
                            "WHERE art_id = ?";
        result.setFiletype(this.jdbcTemplate.query(fileQuery, (rs, rowNum) -> rs.getString("type"), artId));

        // 허용 범위 조회
        String copyrightQuery = "SELECT type FROM copyright " +
                                "INNER JOIN copyright_type ct ON copyright.copyright_type_id = ct.copyright_type_id " +
                                "WHERE art_id = ?";
        result.setCopyright(this.jdbcTemplate.query(copyrightQuery, (rs, rowNum) -> rs.getString("type"), artId));

        // 해시태그 조회
        String tagQuery = "SELECT name FROM art_tag " +
                            "INNER JOIN tag t ON art_tag.tag_id = t.tag_id " +
                            "WHERE art_id = ?";
        result.setTag(this.jdbcTemplate.query(tagQuery, (rs, rowNum) -> rs.getString("name"), artId));

        return result;
    }

    /** 최근 본 작품 조회 **/
    public List<ArtInfo> selectRecentArts(long userId) {
        String query = "SELECT recent_art.art_id, a.user_id, art_image " +
                        "FROM recent_art " +
                        "INNER JOIN art a ON recent_art.art_id = a.art_id " +
                        "WHERE recent_art.user_id = ? AND a.status IN ('S', 'F', 'E')";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ArtInfo(
                    rs.getLong("recent_art.art_id"),
                    rs.getLong("a.user_id"),
                    rs.getString("art_image")),
                userId);
    }

    /** 작품 등록 **/
    public long insertArt(long userId,PostArtReq postArtReq){
        String query = "INSERT INTO art " +
                        "(user_id, title, simple_description, price, count, art_image, image_width, image_height, category_type_id, description, exclusive_flag, additional_charge) "+
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Object[] params = new Object[]{userId,
                                        postArtReq.getTitle(),
                                        postArtReq.getSimpleDescription(),
                                        postArtReq.getPrice(),
                                        postArtReq.getAmount(),
                                        postArtReq.getArtImage(),
                                        postArtReq.getImageWidth(),
                                        postArtReq.getImageHeight(),
                                        postArtReq.getCategoryId(),
                                        postArtReq.getDescription(),
                                        postArtReq.getExclusive(),
                                        postArtReq.getAdditionalCharge()};
        this.jdbcTemplate.update(query, params);

        return this.jdbcTemplate.queryForObject("SELECT last_insert_id()", long.class);

    }

    /** 태그 등록 **/
    public List<Long> insertTag(List<String> tag) {
        StringBuilder query = new StringBuilder("INSERT IGNORE INTO tag(name) VALUE (?)");
        StringBuilder tagIdQuery = new StringBuilder("SELECT tag_id FROM tag WHERE name = ?");
        StringBuilder lastIdQuery = new StringBuilder("SELECT last_insert_id()");

        List<Long> tagId = new ArrayList<>();

        for (String hashTag : tag) {
            int isExists = this.jdbcTemplate.update(query.toString(), hashTag);

            if (isExists == 0) { // 이미 있는 태그
                tagId.add(this.jdbcTemplate.queryForObject(tagIdQuery.toString(), long.class, hashTag));
            } else { // 새로 추가한 태그
                tagId.add(this.jdbcTemplate.queryForObject(lastIdQuery.toString(), long.class));
            }
        }

        return tagId;
    }

    /** 파일 유형 등록 **/
    public int insertFiletype(long artId, List<Integer> filetypeId){
        String query = "INSERT INTO file(art_id, file_type_id) VALUES (?, ?)";
        return this.jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, artId);
                ps.setInt(2, filetypeId.get(i));
            }

            @Override
            public int getBatchSize() {
                return filetypeId.size();
            }
        }).length;
    }

    /** 허용 범위 등록 **/
    public int insertCopyright(long artId, List<Integer> copyrightId) {
        String query = "INSERT INTO copyright(art_id, copyright_type_id) VALUES (?, ?)";
        return this.jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, artId);
                ps.setInt(2, copyrightId.get(i));
            }

            @Override
            public int getBatchSize() {
                return copyrightId.size();
            }
        }).length;
    }

    /** 작품 태그 등록 **/
    public int insertArtTag(long artId, List<Long> tagId){
        String query = "INSERT INTO art_tag(art_id, tag_id) VALUES (?, ?)";
        return this.jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, artId);
                ps.setLong(2, tagId.get(i));
            }

            @Override
            public int getBatchSize() {
                return tagId.size();
            }
        }).length;
    }

    /** 최근 본 작품 등록 **/
    public void insertRecentArt(long userId, long artId) {
        String query = "INSERT INTO recent_art(user_id, art_id) VALUES (?, ?)";
        this.jdbcTemplate.update(query, userId, artId);
    }

    /** 작품 수정 **/
    public int updateArt(long artId, PutArtReq putArtReq){
        String query = "UPDATE art " +
                        "SET title=?, simple_description=?, price=?, count=?," +
                        "art_image=?, image_width=?, image_height=?," +
                        "category_type_id=?, description=?," +
                        "exclusive_flag=?, additional_charge=? " +
                        "WHERE art_id = ?";

        Object[] params = new Object[]{
                putArtReq.getTitle(),
                putArtReq.getSimpleDescription(),
                putArtReq.getPrice(),
                putArtReq.getAmount(),
                putArtReq.getNewArtImage(),
                putArtReq.getImageWidth(),
                putArtReq.getImageHeight(),
                putArtReq.getCategoryId(),
                putArtReq.getDescription(),
                putArtReq.getExclusive(),
                putArtReq.getAdditionalCharge(),
                artId};

        return this.jdbcTemplate.update(query, params);
    }

    /** 작품 삭제 **/
    public int deleteArt(long userId, long artId){
        String query = "UPDATE art SET status = 'D' WHERE user_id = ? AND art_id = ?";
        return this.jdbcTemplate.update(query, userId, artId);
    }

    /** 작품 파일 유형 삭제 **/
    public int deleteFiletype(long artId){
        String query = "DELETE FROM file WHERE art_id =?";

        return this.jdbcTemplate.update(query, artId);
    }

    /** 작품 허용 범위 삭제 **/
    public int deleteCopyright(long artId){
        String query = "DELETE FROM copyright WHERE art_id =?";

        return this.jdbcTemplate.update(query, artId);
    }

    /** 작품 허용 범위 삭제 **/
    public int deleteArtTag(long artId){
        String query = "DELETE FROM art_tag WHERE art_id =?";

        return this.jdbcTemplate.update(query, artId);
    }

    /** 최근 본 작품 전체 삭제 **/
    public int deleteAllRecentArts(long userId) {
        String query = "DELETE FROM recent_art WHERE user_id = ?";

        return this.jdbcTemplate.update(query, userId);
    }

}
