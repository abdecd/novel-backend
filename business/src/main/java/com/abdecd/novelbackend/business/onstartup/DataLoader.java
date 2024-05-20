package com.abdecd.novelbackend.business.onstartup;

import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.abdecd.tokenlogin.mapper.UserMapper;
import com.abdecd.tokenlogin.pojo.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements ApplicationRunner {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, LocalDateTime> redisTemplateForTime;

    @Override
    public void run(ApplicationArguments args) {
        var users = userMapper.selectList(new LambdaQueryWrapper<>());
        loadTimestamp(users);
    }

    public void loadTimestamp(List<User> users) {
        var now = LocalDateTime.now();
        for (var user : users) {
            redisTemplateForTime.opsForValue().setIfAbsent(RedisConstant.READER_HISTORY_TIMESTAMP + user.getId(), now);
            redisTemplateForTime.opsForValue().setIfAbsent(RedisConstant.COMMENT_FOR_NOVEL_TIMESTAMP + user.getId(), now);
        }
    }
}
