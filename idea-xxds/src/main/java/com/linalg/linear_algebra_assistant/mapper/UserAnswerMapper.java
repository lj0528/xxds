package com.linalg.linear_algebra_assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linalg.linear_algebra_assistant.entity.UserAnswer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {
}