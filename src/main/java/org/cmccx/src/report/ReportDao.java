package org.cmccx.src.report;

import org.cmccx.src.report.model.PostReportReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;

@Repository
public class ReportDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /** 회원 신고 접수 **/
    public int insertUserReport(long userId, PostReportReq postReportReq) {
        String query = "INSERT INTO report(report_type_id, target_art_id, target_user_id, user_id) "
                + "VALUES (?, ?, ?, ?)";

        String countQuery = "SELECT COUNT(*) FROM report WHERE target_user_id = ? AND target_art_id IS NULL";

        Object[] params = new Object[] {postReportReq.getReportTypeId(), postReportReq.getTargetArtId(),
                postReportReq.getTargetUserId(), userId};

        this.jdbcTemplate.update(query, params);

        // 회원 신고 횟수 반환
        return this.jdbcTemplate.queryForObject(countQuery, int.class, params[2]);
    }

    /** 작품 신고 접수 **/
    public int insertArtReport(long userId, PostReportReq postReportReq) {
        String query = "INSERT INTO report(report_type_id, target_art_id, target_user_id, user_id) "
                      + "VALUES (?, ?, ?, ?)";

        String countQuery = "SELECT COUNT(*) FROM report WHERE target_art_id = ?";

        Object[] params = new Object[] {postReportReq.getReportTypeId(), postReportReq.getTargetArtId(),
                                        postReportReq.getTargetUserId(), userId};

        this.jdbcTemplate.update(query, params);

        // 작품 신고 횟수 반환
        return this.jdbcTemplate.queryForObject(countQuery, int.class, params[1]);
    }

    /** 총 신고 횟수 조회 **/
    public int selectTotalReportCount(long userId) {
        String query = "SELECT COUNT(*) FROM report WHERE target_user_id = ? ";

        return this.jdbcTemplate.queryForObject(query, int.class, userId);
    }

    /** 유저 차단 **/
    public int updateUserBlock(long userId, Date dateTime) {
        String query = "UPDATE user SET blocked_date = ?, status = 'B' WHERE user_id = ?";

        return this.jdbcTemplate.update(query, dateTime, userId);
    }

    /** 작품 차단 **/
    public int updateArtBlock(long artId) {
        String query = "UPDATE art SET status = 'B' WHERE art_id = ?";

        return this.jdbcTemplate.update(query, artId);
    }
}
