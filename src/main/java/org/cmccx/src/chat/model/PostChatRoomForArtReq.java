package org.cmccx.src.chat.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class PostChatRoomForArtReq {
    @NotNull(message = "메세지를 보낼 작가ID를 입력하세요.")
    private Long artistId;

    private Long artId;

    private Integer totalPrice;

    private String option;
}

