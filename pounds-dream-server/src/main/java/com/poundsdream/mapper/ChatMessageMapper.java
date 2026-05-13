package com.poundsdream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poundsdream.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
