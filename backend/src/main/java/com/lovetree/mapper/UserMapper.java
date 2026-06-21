package com.lovetree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lovetree.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User findByEmail(@Param("email") String email);
}
