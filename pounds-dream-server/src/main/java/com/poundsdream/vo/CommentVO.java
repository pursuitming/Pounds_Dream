package com.poundsdream.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;
    private Long postId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long parentId;
    private Long replyToUserId;
    private String replyToNickname;
    private String content;
    private Integer likeCount;
    private Boolean liked;
    private LocalDateTime createdAt;
}
