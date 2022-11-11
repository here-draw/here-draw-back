package org.cmccx.src.trade;

import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.trade.model.PostTradeConfirmReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class TradeDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 거래 상태 조회 **/
    public String selectTradeStatus(long roomId) {
        try {
            String query = "SELECT status " +
                    "FROM purchase_history " +
                    "WHERE room_id = ?";
            return this.jdbcTemplate.queryForObject(query, String.class, roomId);
        } catch (Exception e) {
            return null;
        }
    }

    /** 구매 내역 조회 **/
    public List<ArtInfo> selectPurchaseHistory(long userId) {
        String query = "SELECT a.art_id, a.user_id, art_image, title, price " +
                        "FROM purchase_history p " +
                        "INNER JOIN art a on p.art_id = a.art_id " +
                        "WHERE buyer_id = ? AND p.status = 'A'";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new ArtInfo(
                        rs.getLong("a.art_id"),
                        rs.getLong("a.user_id"),
                        rs.getString("art_image"),
                        rs.getString("title"),
                        rs.getInt("price")),
                userId);
    }

    /** 거래 확정 **/
    public int insertTradeConfirm(PostTradeConfirmReq postTradeConfirmReq, long userId) {
        String query = "INSERT INTO purchase_history(seller_id, buyer_id, room_id, art_id, status) " +
                        "VALUES (?, ?, ?, ?, IF(seller_id = ?, 'S', 'B')) " +
                        "ON DUPLICATE KEY UPDATE status = " +
                        "CASE " +
                        "    WHEN (seller_id = ? AND status = 'B') THEN 'A' " +
                        "    WHEN (seller_id = ? AND status = 'S') THEN 'S' " +
                        "    WHEN (buyer_id = ? AND status = 'S') THEN 'A' " +
                        "    WHEN (buyer_id = ? AND status = 'B') THEN 'B' " +
                        "END";

        Object[] params = new Object[] {postTradeConfirmReq.getSellerId(), postTradeConfirmReq.getBuyerId(), postTradeConfirmReq.getRoomId(), postTradeConfirmReq.getArtId(),
                                        userId,userId, userId, userId, userId};
        return this.jdbcTemplate.update(query, params);
    }

}
