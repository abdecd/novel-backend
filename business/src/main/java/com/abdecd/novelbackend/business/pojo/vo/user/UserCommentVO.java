package com.abdecd.novelbackend.business.pojo.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCommentVO {
    private Long id;
    private Integer userId;
    private Long toId;
    private Integer toUserId;
    private String content;
    private LocalDateTime timestamp;
}
