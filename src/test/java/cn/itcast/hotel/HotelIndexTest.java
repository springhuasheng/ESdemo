package cn.itcast.hotel;

import cn.itcast.hotel.constants.HotelIndexConstants;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
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
    private RestHighLevelClient client;

    @Autowired
    private HotelMapper hotelMapper;

    /**
     * 前置操作
     */
    @BeforeEach
    public void t0() {
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.136.160:9200")));
    }

    @AfterEach
    public void t1() throws IOException {
        client.close();
    }

    /**
     * 创建索引 添加映射
     * 读取sql 数据 添加到ES
     */
    @Test
    public void t2() throws IOException {
        //创建索引
        CreateIndexResponse response = client.indices().create(new CreateIndexRequest("hotel")
                //添加映射
                .source(HotelIndexConstants.MAPPING_TEMPLATE, XContentType.JSON), RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            List<Hotel> hotels = hotelMapper.selectList(null);
            //循环读取
            for (Hotel hotel : hotels) {
                //设置索引
                client.index(new IndexRequest("hotel")
                                //设置 id
                                .id(hotel.getId().toString())
                                //设置 内容 将实体类转化为 JSON 进行存储
                                .source(new ObjectMapper().writeValueAsString(hotel), XContentType.JSON),
                        RequestOptions.DEFAULT);
            }
        }
    }


}
