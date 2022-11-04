package org.cmccx.src.chat;

import org.cmccx.src.chat.model.GetChatRoomsRes;
import org.cmccx.src.chat.model.PostChatRoomForArtReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ChatDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 작품 채팅방 존재 확인 **/
    public void checkChatRoomByArt() {
        String query ="";
    }

    /** 작가 채팅방 존재 확인 **/
    public void checkChatRoomByArtist() {
        String query ="";
    }

    /** 채팅방 존재 확인 **/
    public int checkChatRoomId(long roomId, long userId) {
        String query = "SELECT EXISTS(SELECT 1 FROM user_chatroom WHERE room_id = ? AND user_id = ? AND status = 'A')";
        return this.jdbcTemplate.queryForObject(query, int.class, roomId, userId);
    }

    /** 채팅방 목록 조회 **/
    public List<GetChatRoomsRes> selectChatRooms(long userId) {
        String query = "SELECT us.room_id, chat_user_id, nickname, profile_image, IF(p.status <> 'I', art_image, null) AS art_image, message, DATE_FORMAT(m.created_at,'%Y년 %m월 %d일') AS last_update" +
                        "FROM user_chatroom us " +
                        "LEFT JOIN (SELECT art_image, room_id FROM chatroom " +
                        "    INNER JOIN art a ON chatroom.art_id = a.art_id) artInfo ON  us.room_id = artInfo.room_id " +
                        "INNER JOIN (SELECT user_id AS chat_user_id, room_id FROM user_chatroom WHERE user_id <> ?) chatUser ON chatUser.room_id = us.room_id " +
                        "INNER JOIN message m ON us.room_id = m.room_id " +
                        "INNER JOIN profile p ON chat_user_id = p.user_id " +
                        "WHERE us.user_id = ? AND us.status = 'A'";
        return this.jdbcTemplate.query(query, (rs, rowNum) -> new GetChatRoomsRes(
                rs.getLong("us.room_id"),
                rs.getLong("chat_user_id"),
                rs.getString("nickname"),
                rs.getString("profile_image"),
                rs.getString("art_image"),
                rs.getString("message"),
                rs.getString("last_update")),
                userId, userId);
    }

    /** 채팅방 생성 **/
    public int insertChatRoom(PostChatRoomForArtReq params) {
        String query = "INSERT INTO chatroom(art_id, total_price, option) VALUES (?, ?, ?)";
        String lastIdQuery = "SELECT last_insert_id()";
        this.jdbcTemplate.update(query, params.getArtId(), params.getTotalPrice(), params.getOption());

        return this.jdbcTemplate.queryForObject(lastIdQuery, int.class);
    }

    /** 회원별 채팅방 생성 **/
    public void insertUserChatRoom(long[] userIds, long roomId) {
        String query = "INSERT INTO user_chatroom(user_id, room_id) VALUES (?, ?)";
        this.jdbcTemplate.update(query, userIds[0], roomId);
        this.jdbcTemplate.update(query, userIds[1], roomId);
    }


}
