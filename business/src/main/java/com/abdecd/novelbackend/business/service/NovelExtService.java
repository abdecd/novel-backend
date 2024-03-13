package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.mapper.NovelInfoMapper;
import com.abdecd.novelbackend.business.mapper.ReaderHistoryMapper;
import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import com.abdecd.novelbackend.business.pojo.vo.novel.HotNovelVO;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class NovelExtService {
    @Autowired
    private ReaderService readerService;
    @Autowired
    private NovelService novelService;
    @Autowired
    private NovelInfoMapper novelInfoMapper;
    @Autowired
    private ReaderHistoryMapper readerHistoryMapper;

    public PageVO<NovelInfoVO> searchNovelInfoByTitle(String title, Integer page, Integer pageSize) {
        var idPage = novelInfoMapper.selectPage(new Page<>(page, pageSize), new LambdaQueryWrapper<NovelInfo>()
                .select(NovelInfo::getId)
                .like(NovelInfo::getTitle, title)
        );
        var list = idPage.getRecords().stream().parallel()
                .map(novelInfo -> novelService.getNovelInfoVO(novelInfo.getId()))
                .toList();
        return new PageVO<>(Math.toIntExact(idPage.getTotal()), list);
    }

    public PageVO<NovelInfoVO> searchNovelInfoByAuthor(String author, Integer page, Integer pageSize) {
        var idPage = novelInfoMapper.selectPage(new Page<>(page, pageSize), new LambdaQueryWrapper<NovelInfo>()
                .select(NovelInfo::getId)
                .like(NovelInfo::getAuthor, author)
        );
        var list = idPage.getRecords().stream().parallel()
                .map(novelInfo -> novelService.getNovelInfoVO(novelInfo.getId()))
                .toList();
        return new PageVO<>(Math.toIntExact(idPage.getTotal()), list);
    }

    /**
     * 获取排行榜
     * @param timeType day, week, month
     * @param tagName 小说类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return :
     */
    public PageVO<NovelInfoVO> pageRankList(String timeType, String tagName, Integer page, Integer pageSize) {
        var pair = getTimeFromTimeType(timeType);
        LocalDateTime startTime = pair.getFirst();
        LocalDateTime endTime = pair.getSecond();

        List<Integer> list;
        var self = SpringContextUtil.getBean(NovelExtService.class);
        if (tagName == null) {
            list = self.getRankList(startTime, endTime);
        } else {
            list = self.getRankListByTagName(tagName, startTime, endTime);
        }
        return new PageVO<NovelInfoVO>()
                .setTotal(list.size())
                .setRecords(list.subList(Math.max(0, (page - 1) * pageSize), Math.min(list.size(), page * pageSize)).stream().parallel()
                        .map(novelId -> novelService.getNovelInfoVO(novelId))
                        .toList());
    }

    /**
     * @param timeType day, week, month
     */
    public Pair<LocalDateTime, LocalDateTime> getTimeFromTimeType(String timeType) {
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
        return Pair.of(startTime, endTime);
    }

    /**
     * @param timeType day, week, month
     */
    public List<NovelTags> getHotTags(String timeType) {
        var pair = getTimeFromTimeType(timeType);
        var tagIds = readerService.getHotTagIds(pair.getFirst(), pair.getSecond());
        var tagList = novelService.getAvailableTags();
        return tagIds.stream()
                .map(id -> tagList.stream()
                        .filter(obj -> Objects.equals(obj.getId(), id)).findFirst().orElseGet(() -> null))
                .toList();
    }

    @Cacheable(value = "novelRankList#32", key = "#startTime.toString() + ':' + #endTime.toString()")
    public List<Integer> getRankList(LocalDateTime startTime, LocalDateTime endTime) {
        var list = readerHistoryMapper.getRankList(startTime, endTime);
        if (list.size() < 100) {
            var addList = readerHistoryMapper.getRandomRankList();
            if (addList.size() > 100 - list.size()) addList = addList.subList(0, 100 - list.size());
            list.addAll(addList);
        }
        return list;
    }

    @Cacheable(value = "novelRankListByTagName#32", key = "#tagName + ':' + #startTime.toString() + ':' + #endTime.toString()")
    public List<Integer> getRankListByTagName(String tagName, LocalDateTime startTime, LocalDateTime endTime) {
        var list = readerHistoryMapper.getRankListByTagName(tagName, startTime, endTime);
        if (list.size() < 100) {
            var addList = readerHistoryMapper.getRandomRankListByTagName(tagName);
            if (addList.size() > 100 - list.size()) addList = addList.subList(0, 100 - list.size());
            list.addAll(addList);
        }
        return list;
    }

    public List<NovelInfoVO> getRecommendList(Integer num) {
        List<Integer> tagIds = new ArrayList<>();
        if (UserContext.getUserId() != null) tagIds = readerService.getReaderFavoriteTagIds(UserContext.getUserId());
        List<NovelInfoVO> list = new ArrayList<>();
        List<Integer> weigthList = new ArrayList<>();
        weigthList.add((int) Math.ceil((double) num / 2));
        weigthList.add(num / 3);
        weigthList.add(num - weigthList.get(0) - weigthList.get(1));
        for (int i = 0; i < weigthList.size(); i++) {
            List<Integer> novelIds;
            try {
                novelIds = readerService.getNovelIdsByTagId(tagIds.get(i));
            } catch (IndexOutOfBoundsException e) {
                novelIds = novelService.getNovelIds();
            }
            Collections.shuffle(novelIds);
            if (novelIds.size() > weigthList.get(i)) novelIds = novelIds.subList(0, weigthList.get(i));
            var tmpList = novelIds
                    .stream().parallel()
                    .map(novelId -> novelService.getNovelInfoVO(novelId))
                    .toList();
            list.addAll(tmpList);
        }
        return list;
    }

    public List<HotNovelVO> getHotList(String week, Integer num) {
        var tags = getHotTags("week");
        var novelExtService = SpringContextUtil.getBean(NovelExtService.class);
        var pair = novelExtService.getTimeFromTimeType(week);
        return tags.stream().map(tag -> {
            var list = novelExtService.getRankListByTagName(tag.getTagName(), pair.getFirst(), pair.getSecond());
            return new HotNovelVO(tag, list
                    .subList(0, Math.min(num, list.size()))
                    .stream().parallel().map(id -> novelService.getNovelInfoVO(id))
                    .toList());
        }).toList();
    }
}
