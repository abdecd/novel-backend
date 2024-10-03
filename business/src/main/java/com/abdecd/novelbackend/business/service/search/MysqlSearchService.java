package com.abdecd.novelbackend.business.service.search;

import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.service.NovelExtService;
import com.abdecd.novelbackend.common.result.PageVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;

@ConditionalOnMissingBean(ElasticSearchService.class)
@Service
public class MysqlSearchService implements SearchService {

    @Override
    public void initData(List<String> tags, List<NovelInfoVO> novels) {
    }

    @Override
    public void saveSearchNovelEntity(NovelInfoVO novelInfoVO) {
    }

    @Override
    public void deleteSearchNovelEntity(Integer id) {
    }

    @Override
    public PageVO<NovelInfoVO> searchNovel(String keyword, Integer page, Integer pageSize) {
        var novelExtService = SpringContextUtil.getBean(NovelExtService.class);
        return novelExtService.searchNovelInfoByTitle(keyword, page, pageSize);
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, Integer num) {
        var novelExtService = SpringContextUtil.getBean(NovelExtService.class);
        return novelExtService.searchNovelInfoByTitle(keyword, 1, num).getRecords()
            .stream().map(NovelInfoVO::getTitle).toList();
    }
}
