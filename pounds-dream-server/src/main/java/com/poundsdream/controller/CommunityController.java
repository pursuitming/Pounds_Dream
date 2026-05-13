package com.poundsdream.controller;

import com.poundsdream.common.PageResult;
import com.poundsdream.common.Result;
import com.poundsdream.dto.CommentDTO;
import com.poundsdream.dto.PostDTO;
import com.poundsdream.service.CommunityService;
import com.poundsdream.vo.CommentVO;
import com.poundsdream.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "社区管理")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "发布帖子")
    @PostMapping("/posts")
    public Result<PostVO> createPost(@Valid @RequestBody PostDTO dto) {
        return Result.success(communityService.createPost(dto));
    }

    @Operation(summary = "获取帖子列表")
    @GetMapping("/posts")
    public Result<PageResult<PostVO>> getPosts(
            // 显式指定参数名为 "category"
            @RequestParam(value = "category", required = false) String category,
            // 显式指定参数名为 "pageNum"
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            // 显式指定参数名为 "pageSize"
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return Result.success(communityService.getPosts(category, pageNum, pageSize));
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/posts/{id}")
    public Result<PostVO> getPostDetail(@PathVariable("id") Long id) {
        return Result.success(communityService.getPostDetail(id));
    }
    @Operation(summary = "发表评论")
    @PostMapping("/comments")
    public Result<CommentVO> addComment(@Valid @RequestBody CommentDTO dto) {
        return Result.success(communityService.addComment(dto));
    }

    @Operation(summary = "获取评论列表")
    @GetMapping("/posts/{postId}/comments")
    public Result<PageResult<CommentVO>> getComments(
            @PathVariable Long postId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return Result.success(communityService.getComments(postId, pageNum, pageSize));
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like")
    public Result<Void> toggleLike(@RequestParam Long targetId, @RequestParam int targetType) {
        communityService.toggleLike(targetId, targetType);
        return Result.success();
    }
}
