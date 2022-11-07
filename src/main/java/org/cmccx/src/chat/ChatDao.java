package org.cmccx.src.chat;

import org.cmccx.config.Constant.RoomType;
import org.cmccx.src.chat.model.*;
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

    /** 이미 생성된 채팅방인지 확인 **/
    public GetExistentChatRoomData checkChatRoomByArt(RoomType type, long artId, long userId, long contactUserId) {
        Object[] params;
        StringBuilder query = new StringBuilder("SELECT COUNT(*) AS is_exist, c.room_id, user.status, contact_user.status ");
        query.append("FROM user_chatroom user ");
        query.append("INNER JOIN user_chatroom contact_user ON user.room_id = contact_user.room_id ");
        query.append("INNER JOIN chatroom c ON user.room_id = c.room_id ");
        query.append("WHERE user.user_id = ? AND contact_user.user_id = ? AND c.status = 'A' ");

        if (type == RoomType.DIRECT_MESSAGE) {  // 작가 DM일 경우
            query.append("AND c.art_id IS NULL");
            params = new Object[] { userId, contactUserId};

            return this.jdbcTemplate.queryForObject(query.toString(),
                    (rs, rowNum) -> new GetExistentChatRoomData(
                            rs.getBoolean("is_exist"),
                            rs.getLong("c.room_id"),
                            rs.getString("user.status"),
                            rs.getString("contact_user.status")),
                    params);

        } else {   // 작품 문의 또는 구입 채팅일 경우
            query.append("AND c.art_id =?");
            params = new Object[] { userId, contactUserId, artId};

            return this.jdbcTemplate.queryForObject(query.toString(),
                    (rs, rowNum) -> new GetExistentChatRoomData(
                            rs.getBoolean("is_exist"),
                            rs.getLong("c.room_id"),
                            rs.getString("user.status"),
                            rs.getString("contact_user.status")),
                    params);
        }
    }

    /** 채팅방 관계 확인 **/
    public int checkChatRoomId(long roomId, long userId) {
        String query = "SELECT EXISTS(SELECT 1 FROM user_chatroom WHERE room_id = ? AND user_id = ? AND status = 'A')";
        return this.jdbcTemplate.queryForObject(query, int.class, roomId, userId);
    }

    /** 채팅방 목록 조회 **/
    public List<GetChatRoomsRes> selectChatRooms(long userId) {
        String query = "SELECT us.room_id, chat_user_id, nickname, profile_image, IF(p.status <> 'I', art_image, null) AS art_image, message, DATE_FORMAT(m.created_at,'%Y-%m-%d') AS last_update " +
                        "FROM user_chatroom us " +
                        "LEFT JOIN (SELECT art_image, room_id FROM chatroom " +
                        "    INNER JOIN art a ON chatroom.art_id = a.art_id) artInfo ON  us.room_id = artInfo.room_id " +
                        "INNER JOIN (SELECT user_id AS chat_user_id, room_id FROM user_chatroom WHERE user_id <> ?) chatUser ON chatUser.room_id = us.room_id " +
                        "INNER JOIN (SELECT room_id, message, created_at FROM message WHERE created_at IN (SELECT MAX(created_at) FROM message GROUP BY room_id)) m ON us.room_id = m.room_id " +
                        "INNER JOIN profile p ON chat_user_id = p.user_id " +
                        "WHERE us.user_id = ? AND us.status = 'A' " +
                        "ORDER BY m.created_at DESC";
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

    /** 채팅방 정보 조회 **/
    public GetChatRoomInfoRes selectChatRoomInfo(long userId, long roomId) {
        String query = "SELECT ch.room_id, ch.art_id, a.user_id, title, total_price, `option`, " +
                "       CASE " +
                "            WHEN a.art_id IS NULL AND title IS NULL THEN 'DM' " +
                "            WHEN total_price IS NULL THEN 'inquiry' " +
                "            ELSE 'purchase' " +
                "       END AS type, " +
                "       CASE " +
                "            WHEN seller_id = ? THEN " +
                "                CASE " +
                "                    WHEN ph.status IN ('S', 'A') THEN TRUE " +
                "                    WHEN ph.status = 'B' THEN FALSE " +
                "                END " +
                "            WHEN buyer_id = ? THEN " +
                "                CASE " +
                "                    WHEN ph.status IN ('B', 'A') THEN TRUE " +
                "                    WHEN ph.status = 'S' THEN FALSE " +
                "                END " +
                "            ELSE FALSE " +
                "       END AS trade, " +
                "       blocked " +
                "FROM chatroom ch " +
                "INNER JOIN (SELECT room_id, IF(u.status IN ('D', 'B', 'P'), TRUE, FALSE) AS blocked FROM user_chatroom uc " +
                "                INNER JOIN user u ON uc.user_id = u.user_id " +
                "            WHERE u.user_id <> ?) uc ON uc.room_id = ch.room_id " +
                "LEFT JOIN art a ON ch.art_id = a.art_id " +
                "LEFT JOIN purchase_history ph ON a.art_id = ph.art_id " +
                "WHERE ch.room_id = ?";

        Object[] params = new Object[] {userId, userId, userId, roomId};

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetChatRoomInfoRes(
                        rs.getString("type"),
                        rs.getBoolean("blocked"),
                        rs.getLong("ch.room_id"),
                        rs.getLong("ch.art_id"),
                        rs.getLong("a.user_id"),
                        rs.getString("title"),
                        rs.getInt("total_price"),
                        rs.getString("option"),
                        rs.getBoolean("trade")),
                params);
    }

    /** 채팅방 메세지 기록 조회 **/
    public List<ChatRoomMessage> selectChatRoomMessages(long userId, long roomId, long messageId, String date)  {
        String query ="SELECT message_id, IF(user_id = ?, TRUE, FALSE) AS sender, message, created_at " +
                        "FROM message m " +
                        "INNER JOIN user_chatroom uc ON m.room_id = uc.room_id " +
                        "WHERE (m.room_id = ? AND uc.user_id = ? AND m.created_at > uc.updated_at) " +
                        "AND ((created_at = ? AND message_id > ?) " +
                        "OR created_at > ?) " +
                        "ORDER BY created_at, message_id " +
                        "LIMIT 21";

        Object[] params = new Object[] {userId, roomId, date, messageId, date};

        return this.jdbcTemplate.query(query, (rs, rowNum) -> new ChatRoomMessage(
                rs.getLong("message_id"),
                rs.getBoolean("sender"),
                rs.getString("message"),
                rs.getString("created_at")),
                params);
    }

    /** 채팅방 생성 **/
    public int insertChatRoom(PostChatRoomForArtReq params) {
        String query = "INSERT INTO chatroom(art_id, total_price, `option`) VALUES (?, ?, ?)";
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

    /** 메세지 저장 **/
    public void insertMessage(ChatMessageReq messageInfo) {
        String query = "INSERT INTO message(room_id, user_id, message) VALUES (?, ?, ?)";
        Object[] params = new Object[] {messageInfo.getRoomId(), messageInfo.getSenderId(), messageInfo.getMessage()};
        this.jdbcTemplate.update(query, params);
    }

    /** 채팅방 정보 업데이트 **/
    public int updateChatRoom(long roomId, PostChatRoomForArtReq chatRoomInfo) {
        String query = "UPDATE chatroom SET art_id=?, total_price=?, `option`=? WHERE room_id = ?";
        Object[] params = new Object[] {chatRoomInfo.getArtId(), chatRoomInfo.getTotalPrice(), chatRoomInfo.getOption(), roomId};
        String lastIdQuery = "SELECT last_insert_id()";
        this.jdbcTemplate.update(query, params);

        return this.jdbcTemplate.queryForObject(lastIdQuery, int.class);
    }

    /** 회원별 채팅방 재연결 **/
    public void updateUserChatRoom(long userId, long roomId) {
        String query = "UPDATE user_chatroom SET status = 'A' WHERE user_id = ? AND room_id = ?";
        this.jdbcTemplate.update(query, userId, roomId);
    }

    /** 회원 채팅방 나가기 **/
    public boolean deleteUserChatRoom(long userId, long roomId) {
        // 채팅방과 연결 해제
        String query = "UPDATE user_chatroom SET status = 'I' WHERE user_id = ? AND room_id = ?";
        this.jdbcTemplate.update(query, userId, roomId);

        // 채팅방에 회원이 남아있는지 확인
        String statusQuery = "SELECT IF(status = 'I', TRUE, FALSE) AS chatroom_status FROM user_chatroom " +
                             "WHERE user_id <> ? AND room_id = ?";
        return this.jdbcTemplate.queryForObject(statusQuery, Boolean.class, userId, roomId);
    }

    /** 채팅방 삭제 **/
    public int deleteChatRoom(long roomId) {
        String query = "UPDATE chatroom SET status = 'I' WHERE room_id = ?";
        return this.jdbcTemplate.update(query, roomId);
    }
}
