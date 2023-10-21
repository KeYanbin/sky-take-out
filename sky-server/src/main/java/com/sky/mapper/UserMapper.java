package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Select;

/**
 * 用户信息(User)表数据库访问层
 *
 * @author keyanbin
 * @since 2023-10-12 19:09:31
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where openid = #{id}")
    User getById(Long userId);

}

