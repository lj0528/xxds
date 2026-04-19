package com.linalg.linear_algebra_assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linalg.linear_algebra_assistant.entity.Theorem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TheoremMapper extends BaseMapper<Theorem> {
    @Select("SELECT * FROM theorem WHERE chapter_id = #{chapterId}")
    List<Theorem> selectByChapter(@Param("chapterId") Integer chapterId);

    @Select("SELECT * FROM theorem WHERE name LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')")
    List<Theorem> searchByKeyword(@Param("keyword") String keyword);
}
