package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * RestClient 查询
 *
 * @author zhaoyq
 * @since 2026/1/31  15:11
 */
//@SpringBootTest(properties = "spring.profiles.active=local")
@Slf4j
public class ElasticSearchTest {

    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.120.101:9200")));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }

    /**
     * 全文检索
     */
    @Test
    void testMatchAll() throws IOException {
        // 创建request对象
        SearchRequest request = new SearchRequest("items");
        // 配置request参数
        request.source()
                .query(QueryBuilders.matchAllQuery());
        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 解析结果
        SearchHits searchHits = response.getHits();
        // 查询的总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("total : " + total);
        // 查询的结果数组
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            // 得到source
            String json = hit.getSourceAsString();
            // 转换为ItemDoc
            Item item = JSONUtil.toBean(json, Item.class);
            System.out.println(item);

        }
    }
}
