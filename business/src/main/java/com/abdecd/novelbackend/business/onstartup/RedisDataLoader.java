package com.abdecd.novelbackend.business.onstartup;

import com.abdecd.novelbackend.business.mapper.ReaderHistoryMapper;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.tokenlogin.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisDataLoader implements ApplicationRunner {
    @Autowired
    private ReaderHistoryMapper readerHistoryMapper;
    @Autowired
    private NovelService novelService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, ReaderHistoryVO> redisTemplate;
    @Override
    public void run(ApplicationArguments args) {
        novelService.getNovelIds().forEach(novelId -> novelService.getNovelInfoVO(novelId));
        loadReaderHistory();
    }

    public void loadReaderHistory() {
        var users = userMapper.selectList(new LambdaQueryWrapper<>());
        for (var user : users) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(RedisConstant.READER_HISTORY + user.getId()))) continue;
            var list = readerHistoryMapper.listReaderHistoryVO(user.getId(), null, RedisConstant.READER_HISTORY_SIZE, StatusConstant.ENABLE);
            redisTemplate.opsForList().leftPushAll(RedisConstant.READER_HISTORY + user.getId(), list);
            redisTemplate.opsForList().trim(RedisConstant.READER_HISTORY + user.getId(), 0, RedisConstant.READER_HISTORY_SIZE);
        }
    }
}
