package com.lyj.community.mapper;

import com.lyj.community.dto.QuestionDTO;
import com.lyj.community.model.Question;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface QuestionMapper {

    @Insert("insert into question (title,description,gmt_create,gmt_modified,creator,tag) " +
            "values(#{title},#{description},#{gmtCreate},#{gmtModified},#{creator},#{tag})")
    void createQuestion(Question question);

    @Select("select * from question limit #{offset},#{size}")
    List<Question> findAll(Integer offset,Integer size);

    @Select("select * from question where creator=#{id} limit #{offset},#{size}")
    List<Question> findOwn(@Param("id") Integer id, @Param("offset") Integer offset,@Param("size") Integer size);

    @Select("select count(1) from question")
    Integer count();

    @Select("select count(1) from question where creator=#{id}")
    Integer countByUserId(Integer id);

    @Select("select * from question where id=#{id}")
    Question findById(Integer id);

    @Update("update question set title=#{title},description=#{description},gmt_modified=#{gmtModified}," +
            "tag=#{tag} where id=#{id}")
    void updateQuestion(Question question);
}
