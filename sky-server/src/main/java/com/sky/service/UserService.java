package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * 用户信息(User)表服务接口
 *
 * @author keyanbin
 * @since 2023-10-12 19:09:32
 */
public interface UserService extends IService<User> {

    /**
     * 用户登陆
     *
     * @param userLoginDTO
     * @return User
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}

