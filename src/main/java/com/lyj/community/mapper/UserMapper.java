package com.lyj.community.mapper;


import com.lyj.community.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {

    @Insert("insert into user(account_id,name,token,gmt_create,gmt_modified,bio,avatar_url) " +
            "values(#{accountId},#{name},#{token},#{gmtCreate},#{gmtModified},#{bio},#{avatarUrl})")
    void insertUser(User user);

    @Select("select * from user where token=#{token}")
    User findByToken(String token);

    @Select("select * from user where id=#{creator}")
    User findById(Integer creator);
}
