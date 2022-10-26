package org.cmccx.src.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;

import java.util.Date;
import java.time.LocalDate;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 회원가입 **/
    public long insertUser(String socialType, long socialId, String email, String profileImage) {
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
    public void updateUserStatus(long userId, char status){
        String query = "UPDATE user SET status = ? WHERE user_id = ?";

        this.jdbcTemplate.update(query, status, userId);
    }


    // 회원 가입 여부 확인 및 닉네임/상태 확인
    public UserInfo checkUser(String socialType, long socialId) {
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
}
