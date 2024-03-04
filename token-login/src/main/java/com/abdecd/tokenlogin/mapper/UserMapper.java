package com.abdecd.tokenlogin.mapper;

import com.abdecd.tokenlogin.pojo.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
