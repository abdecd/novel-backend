package com.abdecd.novelbackend.business.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.pojo.entity.SearchNovelEntity;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.common.constant.ElasticSearchConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticSearchService {
    @Autowired
    private ElasticsearchClient esClient;

    public void saveSearchNovelEntity(NovelInfoVO novelInfoVO) throws IOException {
        esClient.index(u -> u
                .index(ElasticSearchConstant.INDEX_NAME)
                .id(novelInfoVO.getId().toString())
                .document(novelInfoVO.toSearchNovelEntity())
        );
    }

    public void deleteSearchNovelEntity(Integer id) throws IOException {
        esClient.delete(u -> u
                .index(ElasticSearchConstant.INDEX_NAME)
                .id(id.toString())
        );
    }

    public PageVO<NovelInfoVO> searchNovel(String keyword, Integer page, Integer pageSize) throws IOException {
        var response = esClient.search(s -> s
            .index(ElasticSearchConstant.INDEX_NAME)
            .query(q -> q
                .bool(b -> b
                    .should(b1 -> b1.matchPhrase(b2 -> b2.field("title").query(keyword)))
                    .should(b1 -> b1.match(b2 -> b2.field("author").query(keyword)))
                    .should(b1 -> b1.match(b2 -> b2.field("tags_text").query(keyword)))
                    .should(b1 -> b1.match(b2 -> b2.field("description").query(keyword)))
                )
            )
            .from(Math.max(0, (page - 1) * pageSize))
            .size(page * pageSize),
            SearchNovelEntity.class
        );
        if (response.hits().total() == null || response.hits().total().value() == 0) return new PageVO<>(0, new ArrayList<>());
        List<SearchNovelEntity> list = response.hits().hits().stream().map(Hit::source).toList();
        var novelService = SpringContextUtil.getBean(NovelService.class);
        return new PageVO<>(
                Math.toIntExact(response.hits().total().value()),
                list.stream().parallel()
                        .map(item -> novelService.getNovelInfoVO(item.getId())).toList()
        );
    }

    public List<String> getSearchSuggestions(String keyword, Integer num) throws IOException {
        var response = esClient.search(s -> s
                        .index(ElasticSearchConstant.INDEX_NAME)
                        .suggest(sug -> sug.suggesters("suggestion", sug2 -> sug2
                                .prefix(keyword)
                                .completion(f -> f.field("suggestion")))),
                SearchNovelEntity.class
        );
        return response.suggest().get("suggestion").getFirst().completion().options()
                .stream().limit(num).map(CompletionSuggestOption::text).toList();
    }
}
