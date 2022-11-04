package org.cmccx.src.chat.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class PostChatRoomForArtReq {
    @NotNull(message = "메세지를 보낼 작가ID를 입력하세요.")
    private Long artistId;

    @NotNull(message = "구입 또는 문의하려는 작품ID를 입력하세요.")
    private Long artId;

    private Integer totalPrice;

    private String option;

    // 작품 문의
    public PostChatRoomForArtReq(Long artistId, Long artId) {
        this.artistId = artistId;
        this.artId = artId;
    }
}

