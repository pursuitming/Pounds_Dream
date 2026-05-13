package com.poundsdream.service;

import com.poundsdream.common.PageResult;
import com.poundsdream.dto.CommentDTO;
import com.poundsdream.dto.PostDTO;
import com.poundsdream.vo.CommentVO;
import com.poundsdream.vo.PostVO;

public interface CommunityService {

    PostVO createPost(PostDTO dto);

    PageResult<PostVO> getPosts(String category, int pageNum, int pageSize);

    PostVO getPostDetail(Long id);

    CommentVO addComment(CommentDTO dto);

    PageResult<CommentVO> getComments(Long postId, int pageNum, int pageSize);

    void toggleLike(Long targetId, int targetType);
}
