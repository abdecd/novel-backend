package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.mapper.NovelInfoMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.AddNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.UpdateNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.business.pojo.entity.NovelVolume;
import com.abdecd.novelbackend.business.pojo.vo.novel.contents.ContentsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class NovelService {
    @Autowired
    private NovelInfoMapper novelInfoMapper;
    @Autowired
    private NovelVolumeService novelVolumeService;
    @Autowired
    private NovelChapterService novelChapterService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ReaderService readerService;

    public NovelInfo getNovelInfo(int nid) {
        return novelInfoMapper.selectById(nid);
    }

    public List<NovelInfo> searchNovelInfoByTitle(String title, Long startId, Integer pageSize) {
        return novelInfoMapper.searchNovelInfoByTitle(title, startId, pageSize);
    }

    public List<NovelInfo> searchNovelInfoByAuthor(String author, Long startId, Integer pageSize) {
        return novelInfoMapper.searchNovelInfoByAuthor(author, startId, pageSize);
    }

    @UseFileService(value = "cover", param = UpdateNovelInfoDTO.class)
    public void updateNovelInfo(UpdateNovelInfoDTO updateNovelInfoDTO) {
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(updateNovelInfoDTO, novelInfo);
        novelInfoMapper.updateById(novelInfo);
    }

    @UseFileService(value = "cover", param = AddNovelInfoDTO.class)
    public Integer addNovelInfo(AddNovelInfoDTO addNovelInfoDTO) {
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(addNovelInfoDTO, novelInfo);
        novelInfoMapper.insert(novelInfo);
        return novelInfo.getId();
    }

    public void deleteNovelInfo(Integer id) {
        fileService.deleteImg(novelInfoMapper.selectById(id).getCover());
        novelInfoMapper.deleteById(id);
    }

    public ContentsVO getContents(Integer nid) {
        List<NovelVolume> novelVolume = novelVolumeService.listNovelVolume(nid);
        var contentsVO = new ContentsVO();
        for (var novelVolumeItem : novelVolume) {
            var vNum = novelVolumeItem.getVolumeNumber();
            var novelChapter = novelChapterService.listNovelChapter(nid, vNum);
            contentsVO.put(novelVolumeItem.getTitle(), novelChapter);
        }
        return contentsVO;
    }

    /**
     * 获取排行榜
     * @param timeType day, week, month
     * @param tagName 小说类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return :
     */
    public List<NovelInfo> getRankList(String timeType, String tagName, Integer page, Integer pageSize) {
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
        return list.subList((page - 1) * pageSize, page * pageSize);
    }
}
