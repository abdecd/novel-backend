package com.abdecd.novelbackend.business.onstartup;

import com.abdecd.novelbackend.business.mapper.ReaderHistoryMapper;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.business.service.ReaderService;
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
public class RedisDataLoader implements ApplicationRunner {
    @Autowired
    private ReaderService readerService;
    @Autowired
    private NovelService novelService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, LocalDateTime> redisTemplateForTime;

    @Override
    public void run(ApplicationArguments args) {
        novelService.getNovelIds().forEach(novelId -> novelService.getNovelInfoVO(novelId));
        var users = userMapper.selectList(new LambdaQueryWrapper<>());
        loadReaderHistory(users);
        loadTimestamp(users);
    }

    public void loadReaderHistory(List<User> users) {
        for (var user : users) {
            readerService.getReaderHistoryCache(user.getId());
        }
    }

    public void loadTimestamp(List<User> users) {
        var now = LocalDateTime.now();
        for (var user : users) {
            redisTemplateForTime.opsForValue().setIfAbsent(RedisConstant.READER_HISTORY_TIMESTAMP + user.getId(), now);
            redisTemplateForTime.opsForValue().setIfAbsent(RedisConstant.COMMENT_FOR_NOVEL_TIMESTAMP + user.getId(), now);
            var allNovelIds = novelService.getNovelIds();
            for (var novelId : allNovelIds)
                redisTemplateForTime.opsForValue().setIfAbsent(RedisConstant.READER_HISTORY_A_NOVEL_TIMESTAMP + user.getId() + ':' + novelId, now);
        }
    }
}
