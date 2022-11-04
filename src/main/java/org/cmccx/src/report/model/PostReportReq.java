package org.cmccx.src.report.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class PostReportReq {
    @NotNull(message = "신고 유형을 입력하세요.")
    private String reportType;
    private int reportTypeId;

    private Long targetArtId;

    @NotNull(message = "신고 대상 회원ID를 입력하세요.")
    private Long targetUserId;

    public PostReportReq(String reportType, Long targetArtId, Long targetUserId){
        this.reportType = reportType;
        this.targetArtId = targetArtId;
        this.targetUserId = targetUserId;
    }

    public PostReportReq(String reportType, Long targetUserId){
        this.reportType = reportType;
        this.targetUserId = targetUserId;
    }
}
