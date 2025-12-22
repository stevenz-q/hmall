package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.domain.dto.ItemDoc;
import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * @author zhaoyq
 * @since 2025/9/26  19:01
 */
@SpringBootTest(properties = "spring.profiles.active=local")
@Slf4j
public class ElasticDocumentTest {

    @Autowired
    private IItemService itemService;
    private RestHighLevelClient client;


    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.120.101:9200")));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }

    @Test
    void testIndexDoc() throws IOException {
        Item item = itemService.getById(317578L);
        ItemDoc itemDoc = BeanUtils.copyProperties(item, ItemDoc.class);
        // 创建request对象
        IndexRequest indexRequest = new IndexRequest("items").id(itemDoc.getId()).source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        // 设置参数
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        indexRequest.source(jsonStr, XContentType.JSON);
        // 发起请求
        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDoc() throws IOException {
        GetRequest items = new GetRequest("items").id("317578");
        GetResponse response = client.get(items, RequestOptions.DEFAULT);
        ItemDoc bean = JSONUtil.toBean(response.getSourceAsString(), ItemDoc.class);
        System.out.println(bean);
    }

    @Test
    void testDeleteDoc() throws IOException {
        DeleteRequest items = new DeleteRequest("items").id("317578");
        DeleteResponse delete = client.delete(items, RequestOptions.DEFAULT);
        System.out.println(delete.getResult());
    }

    @Test
    void testUpdateDoc() throws IOException {
        UpdateRequest items = new UpdateRequest("items", "317578");
        items.doc("price", 58800, "commentCount", 1);
        UpdateResponse update = client.update(items, RequestOptions.DEFAULT);
        System.out.println(update.getIndex());
    }

    @Test
    void testBulkDoc() throws IOException {
        int pageNo = 1, items = 500;
        while (true) {
            // 查询
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<>(pageNo, items));
            List<ItemDoc> itemDocs = BeanUtil.copyToList(page.getRecords(), ItemDoc.class);
            if (CollUtils.isEmpty(itemDocs)) {
                break;
            }
            log.info("加载第{}页数据，共{}条", pageNo, itemDocs.size());
            BulkRequest request = new BulkRequest();
            itemDocs.forEach(itemDoc -> {
                request.add(new IndexRequest("items").id(itemDoc.getId()).source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
            });
            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }

    }
}
