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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        parseResponseResult(response);
    }

    /**
     * 构建查询条件:全文检索
     */
    @Test
    void testSearchMathch() throws IOException {
        // 创建request对象
        SearchRequest request = new SearchRequest("items");
        // 配置request参数
        request.source().query(
                // 单字段查询
                //QueryBuilders.matchQuery("name","脱脂牛奶")
                // 多字段查询
                QueryBuilders.multiMatchQuery("脱脂牛奶", "name", "category")
        );
        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 解析结果
        parseResponseResult(response);
    }

    /**
     * 构建查询条件:精确查询
     */
    @Test
    void testTermSearch() throws IOException {
        // 创建request对象
        SearchRequest request = new SearchRequest("items");
        // 配置request参数
        request.source().query(
                // 精确查询
                //QueryBuilders.termQuery("category","牛奶")
                // 范围查询
                QueryBuilders.rangeQuery("price").gte("100").lte("150")
        );
        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 解析结果
        parseResponseResult(response);
    }

    /**
     * 构建查询条件:布尔查询
     */
    @Test
    void testBoolSearch() throws IOException {
        // 创建request对象
        SearchRequest request = new SearchRequest("items");

        // 创建布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 添加must条件
        boolQuery.must(QueryBuilders.termQuery("brand", "华为"));
        // 添加must条件
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte("2500"));

        // 配置request参数
        request.source().query(
                boolQuery
        );
        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 解析结果
        parseResponseResult(response);
    }

    /**
     * 构建查询条件
     */
    @Test
    void testSearch() throws IOException {
        // 创建request对象
        SearchRequest request = new SearchRequest("items");
        // 配置request参数
        request.source().query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("name", "脱脂牛奶"))
                        .filter(QueryBuilders.termQuery("brand", "德亚"))
                        .filter(QueryBuilders.rangeQuery("price").lt(30000))
        );
        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 解析结果
        parseResponseResult(response);
    }

    /**
     * 解析结果
     *
     * @param response
     */
    private static void parseResponseResult(SearchResponse response) {
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
