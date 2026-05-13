package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.common.PageResult;
import com.poundsdream.dto.CommentDTO;
import com.poundsdream.dto.PostDTO;
import com.poundsdream.entity.*;
import com.poundsdream.mapper.*;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.CommunityService;
import com.poundsdream.vo.CommentVO;
import com.poundsdream.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PostVO createPost(PostDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategory(dto.getCategory() != null ? dto.getCategory() : "经验分享");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        post.setStatus(1);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                post.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                post.setImages("[]");
            }
        } else {
            post.setImages("[]");
        }

        postMapper.insert(post);
        return convertToPostVO(post, userId);
    }

    @Override
    public PageResult<PostVO> getPosts(String category, int pageNum, int pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();

        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .eq(category != null, Post::getCategory, category)
                .orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(page, wrapper);

        List<PostVO> postVOs = result.getRecords().stream()
                .map(p -> convertToPostVO(p, userId))
                .collect(Collectors.toList());

        return new PageResult<>(postVOs, result.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public PostVO getPostDetail(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();

        Post post = postMapper.selectById(id);
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 增加浏览数
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);

        return convertToPostVO(post, userId);
    }

    @Override
    @Transactional
    public CommentVO addComment(CommentDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        Post post = postMapper.selectById(dto.getPostId());
        if (post == null || post.getStatus() != 1) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        return convertToCommentVO(comment, userId);
    }

    @Override
    public PageResult<CommentVO> getComments(Long postId, int pageNum, int pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();

        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt);

        Page<Comment> result = commentMapper.selectPage(page, wrapper);

        List<CommentVO> commentVOs = result.getRecords().stream()
                .map(c -> convertToCommentVO(c, userId))
                .collect(Collectors.toList());

        return new PageResult<>(commentVOs, result.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void toggleLike(Long targetId, int targetType) {
        Long userId = SecurityUtil.getCurrentUserId();

        Like existing = likeMapper.selectOne(
                new LambdaQueryWrapper<Like>()
                        .eq(Like::getUserId, userId)
                        .eq(Like::getTargetId, targetId)
                        .eq(Like::getTargetType, targetType));

        if (existing != null) {
            // 取消点赞
            likeMapper.deleteById(existing.getId());
            if (targetType == 1) {
                Post post = postMapper.selectById(targetId);
                if (post != null) {
                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                    postMapper.updateById(post);
                }
            } else {
                Comment comment = commentMapper.selectById(targetId);
                if (comment != null) {
                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                    commentMapper.updateById(comment);
                }
            }
        } else {
            // 点赞
            Like like = new Like();
            like.setUserId(userId);
            like.setTargetId(targetId);
            like.setTargetType(targetType);
            likeMapper.insert(like);

            if (targetType == 1) {
                Post post = postMapper.selectById(targetId);
                if (post != null) {
                    post.setLikeCount(post.getLikeCount() + 1);
                    postMapper.updateById(post);
                }
            } else {
                Comment comment = commentMapper.selectById(targetId);
                if (comment != null) {
                    comment.setLikeCount(comment.getLikeCount() + 1);
                    commentMapper.updateById(comment);
                }
            }
        }
    }

    private PostVO convertToPostVO(Post post, Long currentUserId) {
        User user = userMapper.selectById(post.getUserId());

        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setCategory(post.getCategory());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCreatedAt(post.getCreatedAt());

        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        // 解析图片 JSON
        try {
            List<String> images = objectMapper.readValue(post.getImages(), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            vo.setImages(images);
        } catch (JsonProcessingException e) {
            vo.setImages(List.of());
        }

        // 检查是否已点赞
        if (currentUserId != null) {
            Like like = likeMapper.selectOne(
                    new LambdaQueryWrapper<Like>()
                            .eq(Like::getUserId, currentUserId)
                            .eq(Like::getTargetId, post.getId())
                            .eq(Like::getTargetType, 1));
            vo.setLiked(like != null);
        }

        return vo;
    }

    private CommentVO convertToCommentVO(Comment comment, Long currentUserId) {
        User user = userMapper.selectById(comment.getUserId());

        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreatedAt(comment.getCreatedAt());

        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        if (comment.getReplyToUserId() != null) {
            User replyToUser = userMapper.selectById(comment.getReplyToUserId());
            if (replyToUser != null) {
                vo.setReplyToNickname(replyToUser.getNickname());
            }
        }

        // 检查是否已点赞
        if (currentUserId != null) {
            Like like = likeMapper.selectOne(
                    new LambdaQueryWrapper<Like>()
                            .eq(Like::getUserId, currentUserId)
                            .eq(Like::getTargetId, comment.getId())
                            .eq(Like::getTargetType, 2));
            vo.setLiked(like != null);
        }

        return vo;
    }
}
