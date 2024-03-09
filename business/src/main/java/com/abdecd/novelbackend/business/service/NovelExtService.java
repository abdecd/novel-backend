package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.mapper.NovelInfoMapper;
import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class NovelExtService {
    @Autowired
    private ReaderService readerService;
    @Autowired
    private NovelService novelService;
    @Autowired
    private NovelInfoMapper novelInfoMapper;

    public PageVO<NovelInfo> searchNovelInfoByTitle(String title, Long startId, Integer pageSize) {
        var total = novelInfoMapper.countSearchNovelInfoByTitle(title);
        var list = novelInfoMapper.searchNovelInfoByTitle(title, startId, pageSize);
        return new PageVO<>(total, list);
    }

    public PageVO<NovelInfo> searchNovelInfoByAuthor(String author, Long startId, Integer pageSize) {
        var total = novelInfoMapper.countSearchNovelInfoByAuthor(author);
        var list = novelInfoMapper.searchNovelInfoByAuthor(author, startId, pageSize);
        return new PageVO<>(total, list);
    }

    /**
     * 获取排行榜
     * @param timeType day, week, month
     * @param tagName 小说类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return :
     */
    public PageVO<NovelInfo> pageRankList(String timeType, String tagName, Integer page, Integer pageSize) {
        var now = LocalDate.now();
        LocalDateTime startTime = now.atTime(4, 0);
        LocalDateTime endTime = now.atTime(4, 0);
        startTime = switch (timeType) {
            case "day" -> now.minusDays(1).atTime(4, 0);
            case "week" -> now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(4, 0);
            case "month" -> now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).atTime(4, 0);
            default -> startTime;
        };
        endTime = switch (timeType) {
            case "day" -> now.atTime(4, 0);
            case "week" -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(4, 0);
            case "month" -> now.with(TemporalAdjusters.firstDayOfMonth()).atTime(4, 0);
            default -> endTime;
        };
        List<NovelInfo> list;
        if (tagName == null) {
            list = readerService.getRankList(startTime, endTime);
        } else {
            list = readerService.getRankListByTagName(tagName, startTime, endTime);
        }
        return new PageVO<NovelInfo>()
                .setTotal(100)
                .setRecords(list.subList((page - 1) * pageSize, page * pageSize));
    }


    public List<NovelInfo> getRecommendList() {
        var tagIds = readerService.getReaderFavoriteTagIds(UserContext.getUserId());
        List<NovelInfo> list = new ArrayList<>();
        List<Integer> weigthList = Arrays.asList(5, 3, 2);
        for (int i = 0; i < weigthList.size(); i++) {
            List<Integer> novelIds;
            try {
                novelIds = readerService.getNovelIdsByTagId(tagIds.get(i));
            } catch (IndexOutOfBoundsException e) {
                novelIds = novelService.getNovelIds();
            }
            Collections.shuffle(novelIds);
            var tmpList = novelIds.subList(0, weigthList.get(i))
                    .stream().parallel()
                    .map(novelId -> novelService.getNovelInfo(novelId))
                    .toList();
            list.addAll(tmpList);
        }
        return list;
    }
}
