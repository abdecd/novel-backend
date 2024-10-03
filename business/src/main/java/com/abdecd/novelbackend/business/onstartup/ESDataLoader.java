package com.abdecd.novelbackend.business.onstartup;

import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.service.search.ElasticSearchService;
import com.abdecd.novelbackend.business.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@ConditionalOnProperty(prefix = "spring.data.elasticsearch", name = "url")
@Component
public class ESDataLoader implements ApplicationRunner {
    @Autowired
    private NovelService novelService;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        var novels = novelService.getNovelIds().stream().map(novelId -> novelService.getNovelInfoVO(novelId)).toList();
        loadSearchNovelEntity(novels);
    }

    public void loadSearchNovelEntity(List<NovelInfoVO> novels) throws IOException {
        var tags = novelService.getAvailableTags().stream().map(NovelTags::getTagName).toList();
        elasticSearchService.initData(tags, novels);
    }
}
