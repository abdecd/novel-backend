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

    public void initData(List<String> tags, List<NovelInfoVO> novels) throws IOException {
        for (var tag : tags) {
            if (!esClient.exists(g -> g
                    .index(ElasticSearchConstant.INDEX_NAME)
                    .id(tag)
            ).value()) {
                var searchNovelEntity = new SearchNovelEntity();
                searchNovelEntity.setSuggestion(List.of(tag));
                esClient.index(u -> u
                        .index(ElasticSearchConstant.INDEX_NAME)
                        .id(tag)
                        .document(searchNovelEntity)
                );
            }
        }
        for (var novel : novels) {
            if (!esClient.exists(g -> g
                    .index(ElasticSearchConstant.INDEX_NAME)
                    .id(novel.getId().toString())
            ).value()) {
                saveSearchNovelEntity(novel);
            }
        }
    }

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
        var strlen = keyword.replaceAll("\\s","").length();
        String minimumShouldMatch;
        if (strlen < 7) minimumShouldMatch = "100%";
        else minimumShouldMatch = "80%";
        var response = esClient.search(s -> s
            .index(ElasticSearchConstant.INDEX_NAME)
            .query(q -> q
                .bool(b -> b
                    .should(b1 -> b1.matchPhrase(b2 -> b2.field("title").query(keyword).slop(10).boost(2F)))
                    .should(b1 -> b1.matchPhrasePrefix(b2 -> b2.field("title").query(keyword).boost(1.5F)))
                    .should(b1 -> b1.matchPhrase(b2 -> b2.field("author").query(keyword).slop(1)))
                    .should(b1 -> b1.term(b2 -> b2.field("tags").value(keyword)))
                    .should(b1 -> b1.match(b2 -> b2.field("tags_text").query(keyword).minimumShouldMatch(minimumShouldMatch)))
                    .should(b1 -> b1.matchPhrase(b2 -> b2.field("description").query(keyword).slop(1).boost(0.5F)))
                )
            )
            .fields(f -> f.field("id"))
            .from(Math.max(0, (page - 1) * pageSize))
            .size(pageSize),
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
                        .completion(f -> f
                                .field("suggestion")
                                .size(num)
                                .skipDuplicates(true)
                        )
                ))
                .fields(f -> f.field("id")),
                SearchNovelEntity.class
        );
        return response.suggest().get("suggestion").getFirst().completion().options()
                .stream().map(CompletionSuggestOption::text).toList();
    }
}
