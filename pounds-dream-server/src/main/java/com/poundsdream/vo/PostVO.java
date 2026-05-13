package com.poundsdream.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String title;
    private String content;
    private List<String> images;
    private String category;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private LocalDateTime createdAt;
}
