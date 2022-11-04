package org.cmccx.src.report;

import org.cmccx.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.cmccx.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class ReportProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReportDao reportDao;

    @Autowired
    public ReportProvider(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    /** 총 신고횟수 조회 **/
    public int getTotalReportCount(long userId) throws BaseException {
        try {
            return reportDao.selectTotalReportCount(userId);
        } catch (Exception e) {
            logger.error("GetTotalReportCount Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
