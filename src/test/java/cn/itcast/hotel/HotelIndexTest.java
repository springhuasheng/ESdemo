package cn.itcast.hotel;

import cn.itcast.hotel.constants.HotelIndexConstants;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class HotelIndexTest {

    @Autowired
    private HotelMapper hotelMapper;

    private RestHighLevelClient client;

    @Test
    void t1() throws IOException {
        // 创建索引 添加映射
        CreateIndexResponse response = client.indices().create(new CreateIndexRequest("hotel")
                        .source(HotelIndexConstants.MAPPING_TEMPLATE, XContentType.JSON)
                , RequestOptions.DEFAULT);

        if (response.isAcknowledged()) {
            // 批量添加 一次连接可以操作多个文档
            //client.bulk(, )
            List<Hotel> hotels = hotelMapper.selectList(null);
            for (Hotel hotel : hotels) {
                // 把这个hotel 插入到ES中
                client.index(new IndexRequest("hotel")
                                .source(new ObjectMapper().writeValueAsString(hotel), XContentType.JSON)
                        , RequestOptions.DEFAULT);
            }
        }

    }


    @Test
    void t2() throws IOException {
        // 酒店名称包含杭州如家的
        SearchRequest searchR = new SearchRequest("hotel");
        searchR.source().query(QueryBuilders.matchQuery("name", "杭州如家"));
        SearchResponse search = client.search(searchR, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("hit.getSourceAsMap() = " + hit.getSourceAsMap());
        }
    }


    @BeforeEach
    void t0() {
        // 连接到ES的对象
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.131.31:9200")));
    }

    @AfterEach
    void t00() throws IOException {
        // 关闭连接
        client.close();
    }

}
