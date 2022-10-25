package org.cmccx.src.art;

import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.art.model.GetArtByArtIdRes;
import org.cmccx.src.art.model.PostArtReq;
import org.cmccx.src.art.model.PutArtReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
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
        StringBuilder query = new StringBuilder("SELECT EXISTS(SELECT 1 FROM user WHERE user_id = ?)");
        return this.jdbcTemplate.queryForObject(query.toString(), int.class, userId);
    }

    /** 작품 ID 확인 **/
    public int checkArt(long artId){
        StringBuilder query = new StringBuilder("SELECT EXISTS(SELECT 1 FROM art WHERE art_id = ?)");
        return this.jdbcTemplate.queryForObject(query.toString(), int.class, artId);
    }

    /** 작가-작품 관계 확인 **/
    public int checkUserArt(long userId, long artId){
        StringBuilder query = new StringBuilder("SELECT EXISTS(SELECT 1 FROM art WHERE user_id =? AND art_id= ?)");
        return this.jdbcTemplate.queryForObject(query.toString(), int.class, userId, artId);
    }

    /** 작가:작품명 중복 확인 **/
    public int checkArtTitle(long userId, String title, long artId){
        StringBuilder query = new StringBuilder("SELECT EXISTS(SELECT 1 FROM art WHERE (user_id = ? AND title = ?) AND art_id <> ?)");
        return this.jdbcTemplate.queryForObject(query.toString(), int.class, userId, title, artId);
    }

    /** 메인: 작품 조회(최신 등록순) **/
    public List<ArtInfo> selectArts(int categoryId, long artId, String date, int size){
        StringBuilder query = new StringBuilder("SELECT art_id, art_image, title, created_at FROM art ");
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
                        rs.getString("art_image"),
                        rs.getString("title"),
                        rs.getString("created_at")),
                params);
    }

    /** 작가별 작품 조회 **/
    public List<ArtInfo> selectArsByUserId(long userId, long artistId, boolean isMyPage, long artId, int size){
        StringBuilder query = new StringBuilder("SELECT art.art_id, art_image, title, price, IFNULL(user_like.art_id, 0)  AS count, art.status ");
        query.append("FROM art ");
        query.append("LEFT JOIN (SELECT art_id ");
        query.append("FROM gallery ");
        query.append("INNER JOIN bookmark b ON gallery.gallery_id = b.gallery_id ");
        query.append("WHERE user_id = ? ");
        query.append("GROUP BY art_id) user_like ON user_like.art_id = art.art_id ");
        query.append("WHERE art.user_id = ? AND art.art_id <> ? ");

        if (isMyPage){  // MYPage
            query.append("AND (status = 'S' OR status = 'F') ");
        } else {  // 작가 홈
            query.append("AND (status = 'S') ");
        }

        query.append("ORDER BY updated_at DESC ");
        query.append("LIMIT ?");

        Object[] params = new Object[]{userId, artistId, artId, size};

        return this.jdbcTemplate.query(query.toString(),
                (rs, rowNum) -> new ArtInfo(
                        rs.getLong("art_id"),
                        rs.getString("art_image"),
                        rs.getString("title"),
                        rs.getInt("price"),
                        rs.getInt("count"),
                        rs.getString("status")),
                params);
    }


    /** 추천 작품 조회 **/
    public List<ArtInfo> selectRecommendedArts(long userId, long artId){
        StringBuilder query = new StringBuilder("SELECT art.art_id, art_image, IFNULL(user_like.art_id, 0) AS count ");
        query.append("FROM art ");
        query.append("LEFT JOIN (SELECT art_id ");
        query.append("FROM gallery ");
        query.append("INNER JOIN bookmark b ON gallery.gallery_id = b.gallery_id ");
        query.append("WHERE user_id = ? ");
        query.append("GROUP BY art_id) user_like ON user_like.art_id = art.art_id ");
        query.append("WHERE art.art_id <> ? AND status = 'S' ");
        query.append("ORDER BY rand() ");
        query.append("LIMIT 4");

        return this.jdbcTemplate.query(query.toString(),
                (rs, rowNum) -> new ArtInfo(
                        rs.getLong("art.art_id"),
                        rs.getString("art_image"),
                        rs.getInt("count")),
                userId, artId);
    }

    /** 작품 상세 정보 조회 **/
    public GetArtByArtIdRes selectArtByArtId(long artId){
        // 작품 및 작가 정보 조회
        StringBuilder infoQuery = new StringBuilder("SELECT art.user_id, art_image, title, price, simple_description, art.description, IFNULL(like_art, 0) AS like_art ");
        infoQuery.append("FROM art ");
        infoQuery.append("LEFT JOIN (SELECT COUNT(*) AS like_art, art_id ");
        infoQuery.append("FROM bookmark ");
        infoQuery.append("GROUP BY art_id) like_art_tb ON like_art_tb.art_id = art.art_id ");
        infoQuery.append("WHERE art.art_id = ?");

        GetArtByArtIdRes result =  this.jdbcTemplate.queryForObject(infoQuery.toString(),
                                    (rs, rowNum) -> new GetArtByArtIdRes(
                                            rs.getLong("art.user_id"),
                                            rs.getString("art_image"),
                                            rs.getString("title"),
                                            rs.getInt("price"),
                                            rs.getString("simple_description"),
                                            rs.getString("art.description"),
                                            rs.getInt("like_art")),
                                    artId);

        // 파일 유형 조회
        StringBuilder fileQuery = new StringBuilder("SELECT type FROM file ");
        fileQuery.append("INNER JOIN file_type ft ON file.file_type_id = ft.file_type_id ");
        fileQuery.append("WHERE art_id = ?");
        result.setFiletype(this.jdbcTemplate.query(fileQuery.toString(), (rs, rowNum) -> rs.getString("type"), artId));

        // 허용 범위 조회
        StringBuilder copyrightQuery = new StringBuilder("SELECT type FROM copyright ");
        copyrightQuery.append("INNER JOIN copyright_type ct ON copyright.copyright_type_id = ct.copyright_type_id ");
        copyrightQuery.append("WHERE art_id = ?");
        result.setCopyright(this.jdbcTemplate.query(copyrightQuery.toString(), (rs, rowNum) -> rs.getString("type"), artId));

        // 해시태그 조회
        StringBuilder tagQuery = new StringBuilder("SELECT name FROM art_tag ");
        tagQuery.append("INNER JOIN tag t ON art_tag.tag_id = t.tag_id ");
        tagQuery.append("WHERE art_id = ?");
        result.setTag(this.jdbcTemplate.query(tagQuery.toString(), (rs, rowNum) -> rs.getString("name"), artId));

        return result;
    }

    /** 작품 등록 **/
    public long insertArt(long userId,PostArtReq postArtReq){
        StringBuilder query = new StringBuilder("INSERT INTO art ");
        query.append("(user_id, title, simple_description, price, count, art_image, category_type_id, description, exclusive_flag, additional_charge) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        Object[] params = new Object[]{userId,
                                        postArtReq.getTitle(),
                                        postArtReq.getSimpleDescription(),
                                        postArtReq.getPrice(),
                                        postArtReq.getAmount(),
                                        postArtReq.getArtImage(),
                                        postArtReq.getCategoryId(),
                                        postArtReq.getDescription(),
                                        postArtReq.getExclusive(),
                                        postArtReq.getAdditionalCharge()};
        this.jdbcTemplate.update(query.toString(), params);

        long lastTagId = this.jdbcTemplate.queryForObject("SELECT last_insert_id()", long.class);

        return lastTagId;

    }

    /** 태그 등록 **/
    public List<Long> insertTag(List<String> tag) {
        StringBuilder query = new StringBuilder("INSERT IGNORE INTO tag(name) VALUE (?)");
        StringBuilder tagIdQuery = new StringBuilder("SELECT tag_id FROM tag WHERE name = ?");
        StringBuilder lastIdQuery = new StringBuilder("SELECT last_insert_id()");

        List<Long> tagId = new ArrayList<>();

        for (int i = 0; i < tag.size(); i++){
            int isExists = this.jdbcTemplate.update(query.toString(), tag.get(i));

            if (isExists == 0){ // 이미 있는 태그
                tagId.add(this.jdbcTemplate.queryForObject(tagIdQuery.toString(), long.class, tag.get(i)));
            } else { // 새로 추가한 태그
                tagId.add(this.jdbcTemplate.queryForObject(lastIdQuery.toString(), long.class));
            }
        }

        return tagId;
    }

    /** 파일 유형 등록 **/
    public int insertFiletype(long artId, List<Integer> filetypeId){
        StringBuilder query = new StringBuilder("INSERT INTO file(art_id, file_type_id) VALUES (?, ?)");
        return this.jdbcTemplate.batchUpdate(query.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
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
        StringBuilder query = new StringBuilder("INSERT INTO copyright(art_id, copyright_type_id) VALUES (?, ?)");
        return this.jdbcTemplate.batchUpdate(query.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
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
        StringBuilder query = new StringBuilder("INSERT INTO art_tag(art_id, tag_id) VALUES (?, ?)");
        return this.jdbcTemplate.batchUpdate(query.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, artId);
                ps.setLong(2, tagId.get(i));
            }

            @Override
            public int getBatchSize() {
                return tagId.size();
            }
        }).length;
    }

    /** 작품 수정 **/
    public int updateArt(long artId, PutArtReq putArtReq){
        StringBuilder query = new StringBuilder("UPDATE art ");
        query.append("SET title=?, simple_description=?, price=?, count=?,");
        query.append("art_image=?, category_type_id=?, description=?,");
        query.append("exclusive_flag=?, additional_charge=? ");
        query.append("WHERE art_id = ?");

        Object[] params = new Object[]{
                putArtReq.getTitle(),
                putArtReq.getSimpleDescription(),
                putArtReq.getPrice(),
                putArtReq.getAmount(),
                putArtReq.getNewArtImage(),
                putArtReq.getCategoryId(),
                putArtReq.getDescription(),
                putArtReq.getExclusive(),
                putArtReq.getAdditionalCharge(),
                artId};

        return this.jdbcTemplate.update(query.toString(), params);
    }

    /** 작품 삭제 **/
    public int deleteArt(long userId, long artId){
        StringBuilder query = new StringBuilder("UPDATE art SET status = 'D' WHERE user_id = ? AND art_id = ?");
        return this.jdbcTemplate.update(query.toString(), userId, artId);
    }

    /** 작품 파일 유형 삭제 **/
    public int deleteFiletype(long artId){
        StringBuilder query = new StringBuilder("DELETE FROM file WHERE art_id =?");

        return this.jdbcTemplate.update(query.toString(), artId);
    }

    /** 작품 허용 범위 삭제 **/
    public int deleteCopyright(long artId){
        StringBuilder query = new StringBuilder("DELETE FROM copyright WHERE art_id =?");

        return this.jdbcTemplate.update(query.toString(), artId);
    }

    /** 작품 허용 범위 삭제 **/
    public int deleteArtTag(long artId){
        StringBuilder query = new StringBuilder("DELETE FROM art_tag WHERE art_id =?");

        return this.jdbcTemplate.update(query.toString(), artId);
    }

}
