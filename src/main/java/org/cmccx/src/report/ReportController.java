package org.cmccx.src.report;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.report.model.PostReportReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.cmccx.config.BaseResponseStatus.VALIDATION_ERROR;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 회원 신고 API
     * [POST] /reports/users
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/users")
    public BaseResponse<String> registerUserReport(@RequestBody @Valid PostReportReq postMapping) throws BaseException {
        reportService.registerUserReport(postMapping);

        return new BaseResponse<>("신고 완료 되었습니다.");
    }

    /**
     * 작품 신고 API
     * [POST] /reports/arts
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/arts")
    public BaseResponse<String> registerArtReport(@RequestBody @Valid PostReportReq postReportReq) throws BaseException {
        // ArtId 유효성 검사
        if (postReportReq.getTargetArtId() == null) {
            return new BaseResponse<>(VALIDATION_ERROR, "신고 대상 작품ID를 입력하세요.");
        }

        reportService.registerArtReport(postReportReq);

        return new BaseResponse<>("신고 완료 되었습니다.");
    }
}
