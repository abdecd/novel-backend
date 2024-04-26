package com.abdecd.novelbackend.business.onstartup;

import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.service.ElasticSearchService;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements ApplicationRunner {
    @Autowired
    private ReaderService readerService;
    @Autowired
    private NovelService novelService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, LocalDateTime> redisTemplateForTime;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        var novels = novelService.getNovelIds().stream().map(novelId -> novelService.getNovelInfoVO(novelId)).toList();
        var users = userMapper.selectList(new LambdaQueryWrapper<>());
        loadReaderHistory(users);
        loadTimestamp(users);
        loadSearchNovelEntity(novels);
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
        }
    }

    public void loadSearchNovelEntity(List<NovelInfoVO> novels) throws IOException {
        var tags = novelService.getAvailableTags().stream().map(NovelTags::getTagName).toList();
        elasticSearchService.initData(tags, novels);
    }
}
