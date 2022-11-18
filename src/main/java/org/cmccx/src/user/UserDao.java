package org.cmccx.src.user;

import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.user.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;

import java.time.LocalDate;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 회원가입 **/
    public long insertUser(String socialType, String socialId, String email, String profileImage) {
        String query = "INSERT INTO user (social_type, social_id, email) VALUES(?, ?, ?)";
        Object[] params = new Object[]{socialType, socialId, email};
        this.jdbcTemplate.update(query, params);

        String userIdQuery = "SELECT last_insert_id()";
        int userId = this.jdbcTemplate.queryForObject(userIdQuery, int.class);
        String profileQuery = "INSERT INTO profile (user_id, profile_image) VALUES(?, ?)";
        Object[] profileParams = new Object[]{userId, profileImage};
        this.jdbcTemplate.update(profileQuery, profileParams);

        return userId;
    }

    /** 유저 상태 수정 **/
    public void updateUserStatus(long userId, String status){
        String query = "UPDATE user SET status = ? WHERE user_id = ?";

        this.jdbcTemplate.update(query, status, userId);
    }

    /** 유저 상태 조회 **/
    public String getUserStatus(long userId){
        String query = "SELECT status from user WHERE user_id = ?";

        return this.jdbcTemplate.queryForObject(query, String.class, userId);
    }

    // 회원 가입 여부 확인 및 닉네임/상태 확인
    public UserInfo checkUser(String socialType, String socialId) {
        String query = "SELECT u.user_id, p.nickname, u.status " +
                "FROM user u " +
                "LEFT JOIN profile p ON u.user_id = p.user_id " +
                "WHERE u.social_type = ? and u.social_id = ?";
        Object[] params = new Object[]{socialType, socialId};

        try {
            return this.jdbcTemplate.queryForObject(query,
                    (rs, rowNum) -> new UserInfo(
                            rs.getLong("u.user_id"),
                            rs.getString("p.nickname"),
                            rs.getString("u.status"))
                    , params);
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    // 탈퇴 회원의 이전 userId 삭제
    public void deletePrevUserId(long userId) {
        String query = "UPDATE user SET social_type = '-', social_id = '-' WHERE user_id = ?";
        this.jdbcTemplate.update(query, userId);
    }

    /** 휴면 회원 정보 가져오기 **/
    public void recoveryUserInfo(long userId){
        String query = "UPDATE profile SET status = 'A' WHERE user_id = ?";
        this.jdbcTemplate.update(query, userId);

        String recoveryQuery = "update user u set device_token = (select device_token from dormant_user where user_id = ?),\n" +
                "                  email = (select email from dormant_user where u.user_id = ?),\n" +
                "                  status = 'A'\n" +
                "where u.user_id = ?;";
        this.jdbcTemplate.update(recoveryQuery, userId, userId, userId);

        String deleteDormantQuery = "update dormant_user set status='I' where user_id = ?";
        this.jdbcTemplate.update(deleteDormantQuery, userId);
    }

    // 회원 가입 가능 날짜(last_login + 7) 받아오기
    public LocalDate getEnableSignUpDate(long userId) {
        String query = "SELECT date_add(last_login,INTERVAL 7 DAY) from user where user_id = " + userId;
        return this.jdbcTemplate.queryForObject(query, LocalDate.class);
    }

    // 차단일 가져오기
    public LocalDate getBlockedDate(long userId) {
        String query = "SELECT blocked_date from user where user_id = " + userId;
        return this.jdbcTemplate.queryForObject(query, LocalDate.class);
    }

    // 로그인 일자 업데이트
    public void updateLoginDate(long userId) {
        String query = "UPDATE user SET last_login = now() where user_id = " + userId;
        this.jdbcTemplate.update(query);
    }

    // userId 체크 (status가 A, I, B인 user)
    public int checkUserId(long userId) {
        String query = "SELECT EXISTS(SELECT user_id from profile where user_id = ? and status not in ('D', 'P'))";
        return this.jdbcTemplate.queryForObject(query, int.class, userId);
    }

    // 닉네임 중복 체크
    public int checkNickname(String nickname) {
        String query = "SELECT EXISTS(SELECT nickname from profile where nickname = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, nickname);
    }

    // 닉네임 설정(변경)
    public void modifyNickname(long userId, String nickname) {
        String query = "UPDATE profile SET nickname = ? where user_id = ?";
        this.jdbcTemplate.update(query, nickname, userId);
    }

    // 기본 갤러리 생성
    public void createDefaultGallery(long userId) {
        String query = "INSERT INTO gallery (user_id, name) VALUES(?, ?)";
        this.jdbcTemplate.update(query, userId, "기본 갤러리");
    }

    // 프로필 정보 조회
    public ProfileInfo getProfileInfo(long userId) {
        String query = "SELECT profile_image, nickname, description from profile where user_id = " + userId;
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new ProfileInfo(
                        rs.getString("profile_image"),
                        rs.getString("nickname"),
                        rs.getString("description"))
                );
    }

    // 마이페이지 - 유저 정보(팔로우 수, 팔로잉 수, 작품찜 수) 조회
    public LikeInfo getLikeInfo(long userId) {
        String query = "SELECT COUNT(CASE WHEN target_user_id= ? THEN 1 END) AS followerCnt,\n" +
                "       COUNT(CASE WHEN follower_id = ? THEN 1 END) AS followingCnt,\n" +
                "       (SELECT COUNT(*)\n" +
                "        FROM art a INNER JOIN bookmark b on b.art_id = a.art_id and a.user_id = ?) AS likeCnt\n" +
                "FROM follow\n" +
                "WHERE status='A'";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new LikeInfo(
                        rs.getInt("followerCnt"),
                        rs.getInt("followingCnt"),
                        rs.getInt("likeCnt")),
                userId, userId, userId);
    }

    // 작가 정보 조회
    public ArtistInfo getArtistInfo(long userId, long artistId) {
        String query = "SELECT p.profile_image as profileImg, p.nickname, p.description,\n" +
                "       COUNT(CASE WHEN f.target_user_id= ? and f.status='A' THEN 1 END) AS followerCnt,\n" +
                "       (SELECT COUNT(*)\n" +
                "        FROM art a INNER JOIN bookmark b on b.art_id = a.art_id and a.user_id = ?) AS likeCnt,\n" +
                "       (SELECT EXISTS(SELECT * from follow f_c where f_c.follower_id = ? and f_c.target_user_id = ?)) as isFollowing,\n" +
                "       (SELECT EXISTS(SELECT * from article_compilation where user_id = ? and status NOT IN ('I'))) as hasArticle\n" +
                "FROM profile p \n" +
                "LEFT JOIN follow f on f.target_user_id = ? \n" +
                "WHERE user_id = ?";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new ArtistInfo(
                        rs.getString("profileImg"),
                        rs.getString("nickname"),
                        rs.getString("description"),
                        rs.getInt("followerCnt"),
                        rs.getInt("likeCnt"),
                        rs.getBoolean("isFollowing"),
                        rs.getBoolean("hasArticle")),
                artistId, artistId, userId, artistId, artistId, artistId, artistId);
    }

    // 프로필 정보 수정
    public void modifyProfileInfo(long userId, String nickname, String description) {
        String query = "UPDATE profile SET nickname = ?, description = ? where user_id = ?";
        this.jdbcTemplate.update(query, nickname, description, userId);
    }

    // 프로필 정보 수정(프로필 사진 포함)
    public void modifyProfileInfo(long userId, String imageUrl, String nickname, String description) {
        String query = "UPDATE profile SET profile_image = ?, nickname = ?, description = ? where user_id = ?";
        this.jdbcTemplate.update(query, imageUrl, nickname, description, userId);
    }

    // 프로필 사진 조회
    public String getProfileImg(long userId) {
        String query = "SELECT profile_image from profile where user_id = " + userId;
        return this.jdbcTemplate.queryForObject(query, String.class);
    }

    // FollowList 체크
    public int checkFollowList(long userId, long targetId) {
        String query = "SELECT EXISTS(SELECT follower_id from follow where follower_id = ? and target_user_id = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, userId, targetId);
    }

    // FollowList 수정
    public int patchFollowList(long userId, long targetId, String status) {
        String checkStatusQuery = "select status from follow where follower_id = ? and target_user_id = ?";
        String statusCheck = this.jdbcTemplate.queryForObject(checkStatusQuery, (rs, rowNum) -> rs.getString("status"), userId, targetId);
        if(statusCheck.equals(status)) {
            return 0;
        } else {
            String patchFollowQuery = "update follow set status = ? where follower_id = ? and target_user_id = ?";
            this.jdbcTemplate.update(patchFollowQuery, status, userId, targetId);
            return 1;
        }
    }

    public void postFollowList(long userId, long targetId) {
        String createQuery = "insert into follow (follower_id, target_user_id) VALUES (?,?)";
        this.jdbcTemplate.update(createQuery, userId, targetId);
    }

    public List<ProfileInfo> getFollowerList(long userId) {
        String query = "SELECT p.profile_image as profileImage, p.nickname\n" +
                "from follow f\n" +
                "         INNER JOIN profile p on p.user_id = f.follower_id\n" +
                "where f.target_user_id = ? and f.status = 'A'";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ProfileInfo(
                        rs.getString("profileImage"),
                        rs.getString("nickname"),
                        null
                ), userId);
    }

    public List<ProfileInfo> getFollowingList(long userId) {
        String query = "SELECT p.profile_image as profileImage, p.nickname\n" +
                "from follow f\n" +
                "         INNER JOIN profile p on p.user_id = f.target_user_id\n" +
                "where f.follower_id = ? and f.status = 'A'";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ProfileInfo(
                        rs.getString("profileImage"),
                        rs.getString("nickname"),
                        null
                ), userId);
    }

    /** 작가별 작품 조회 **/
    public List<ArtInfo> selectArtsByUserId(long userId, long artistId, boolean isMyPage, long artId, int size){
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
}
