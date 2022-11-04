package org.cmccx.src.report;

import org.cmccx.config.BaseException;
import org.cmccx.config.Constant;
import org.cmccx.src.art.ArtProvider;
import org.cmccx.src.report.model.PostReportReq;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class ReportService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReportProvider reportProvider;
    private final ReportDao reportDao;
    private final ArtProvider artProvider;
    private final JwtService jwtService;

    @Autowired
    public ReportService(ReportProvider reportProvider ,ReportDao reportDao, ArtProvider artProvider, JwtService jwtService) {
        this.reportProvider = reportProvider;
        this.reportDao = reportDao;
        this.artProvider = artProvider;
        this.jwtService = jwtService;
    }

    /** 회원 신고 접수 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void registerUserReport(PostReportReq postReportReq) throws BaseException {
        try {
            // 신고 접수한 회원ID 추출 및 검증
            long userId = jwtService.getUserId();

            postReportReq.setReportTypeId(Constant.getReportId(Constant.USER, postReportReq.getReportType()));  // 신고 유형 ID 등록
            int reportCount = reportDao.insertUserReport(userId, postReportReq);    // 신고 등록

            // 신고 3회 누적 시, 3일간 차단
            if (reportCount % 3 == 0) {
                /** 채팅 관련 데이터 삭제 후 3일 뒤 다시 활성화 **/
                Date blockedDate = Date.valueOf(LocalDate.now().plusDays(4));
                int result = reportDao.updateUserBlock(postReportReq.getTargetUserId(), blockedDate);
                if (result == 0) {
                    throw new Exception();
                }
            }
            // 영구 차단 확인
            checkPermanentBlock(postReportReq.getTargetUserId());

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("UserReport Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작품 신고 접수 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void registerArtReport(PostReportReq postReportReq) throws BaseException {
        int reportCount;
        try {
            // 신고 접수한 회원ID 추출 및 검증
            long userId = jwtService.getUserId();

            // 작품-작가ID 확인
            int isValid = artProvider.checkUserArt(postReportReq.getTargetUserId(), postReportReq.getTargetArtId());
            if (isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            postReportReq.setReportTypeId(Constant.getReportId(Constant.ART, postReportReq.getReportType()));   // 신고 유형 ID 등록
            reportCount = reportDao.insertArtReport(userId, postReportReq);    // 신고 등록

            // 신고 3회 누적 시, 작품 차단
            if (reportCount % 3 == 0) {
                int result = reportDao.updateArtBlock(postReportReq.getTargetArtId());
                if (result == 0) {
                    throw new Exception();
                }
            }

            // 영구 차단 확인
            checkPermanentBlock(postReportReq.getTargetUserId());

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("ARtReport Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 영구 차단 **/
    private void checkPermanentBlock(long userId) throws BaseException {
        try {
            int reportCount = reportProvider.getTotalReportCount(userId);

            // 신고 누적 9회 이상일 경우, 영구 차단
            if (reportCount == 9) {
                Date blockedDate = Date.valueOf(LocalDate.of(2999, 1, 1));
                int result = reportDao.updateUserBlock(userId, blockedDate);

                /** 데이터 전체 삭제 필요 **/

                if (result == 0) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            logger.error("CheckPermanentBlock Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
