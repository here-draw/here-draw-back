package org.cmccx.src.trade.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class PostTradeConfirmReq {
    @NotNull(message = "판매자 ID를 입력하세요.")
    private Long sellerId;

    @NotNull(message = "구매자 ID를 입력하세요.")
    private Long buyerId;

    @NotNull(message = "채팅방 ID를 입력하세요.")
    private Long roomId;

    @NotNull(message = "구매작품 ID를 입력하세요.")
    private Long artId;

    @NotNull(message = "독점 구매 여부를 입력하세요.")
    private Boolean exclusive;
}
