package com.abdecd.novelbackend.business.service.search;

import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.common.result.PageVO;

import java.io.IOException;
import java.util.List;

public interface SearchService {
    void initData(List<String> tags, List<NovelInfoVO> novels) throws IOException;

    void saveSearchNovelEntity(NovelInfoVO novelInfoVO) throws IOException;

    void deleteSearchNovelEntity(Integer id) throws IOException;

    PageVO<NovelInfoVO> searchNovel(String keyword, Integer page, Integer pageSize) throws IOException;

    List<String> getSearchSuggestions(String keyword, Integer num) throws IOException;
}
