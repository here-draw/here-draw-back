package org.cmccx.src.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetChatRoomsRes {
    private long roomId;
    private long contactUserId;
    private String nickname;
    private String profileImage;
    private String artImage;
    private String lastMessage;
    private String lastDate;
}
