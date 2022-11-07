package org.cmccx.src.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cmccx.utils.ScrollPagination;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetChatRoomMessagesRes {
    private boolean hasNextScroll;
    private long messageIdCursor;
    private String dateCursor;
    private List<ChatRoomMessage> messageList;

    public static GetChatRoomMessagesRes of(ScrollPagination<ChatRoomMessage> scrollInfo){
        if (scrollInfo.hasNextScroll()){
            return new GetChatRoomMessagesRes(
                    scrollInfo.hasNextScroll(),
                    scrollInfo.getNextCursor().getMessageId(),
                    scrollInfo.getNextCursor().getDate(),
                    scrollInfo.getCurrentScrollItems());
        }
        return new GetChatRoomMessagesRes(
                scrollInfo.hasNextScroll(),
                -1L,
                "",
                scrollInfo.getCurrentScrollItems());
    }
}
