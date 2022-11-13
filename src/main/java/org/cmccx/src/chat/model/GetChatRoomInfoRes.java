package org.cmccx.src.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetChatRoomInfoRes {
    private String chatRoomType;
    private boolean blockUser;
    private long roomId;
    private long artId;
    private long artistId;
    private String artImage;
    private String title;
    private int totalPrice;
    private String option;
    private boolean tradeStatus;
}
