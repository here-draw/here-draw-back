package org.cmccx.src.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetExistentChatRoomData {
    private boolean isExistent;
    private long roomId;
    private String userStatus;
    private String contactUserStatus;
}
