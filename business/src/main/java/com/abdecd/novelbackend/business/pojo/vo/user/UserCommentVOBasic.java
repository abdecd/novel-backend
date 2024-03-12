package com.abdecd.novelbackend.business.pojo.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCommentVOBasic {
    private Long id;
    private Integer userId;
    private UserDetail userDetail;
    private Long toId;
    private Integer toUserId;
    private UserDetail toUserDetail;
    private String content;
    private LocalDateTime timestamp;
    private Byte status;
    @Data
    public static class UserDetail {
        private Integer id;
        private String nickname;
        private String avatar;
    }
}
