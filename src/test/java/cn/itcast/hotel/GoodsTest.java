package cn.itcast.hotel;

import cn.itcast.hotel.constants.GoodsIndexConstants;
import cn.itcast.hotel.constants.HotelIndexConstants;
import cn.itcast.hotel.mapper.GoodsMapper;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Goods;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.User;
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

/**
 * @Author: huasheng
 * @Date: 2022/1/12 0:29
 */
@SpringBootTest
public class GoodsTest {
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private GoodsMapper goodsMapper;


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
     * 1. 使用MybatisPlus查询goods表
     * 2. 在ES中创建索引并按照goods表添加映射 (title字段需要分词)
     * 3. 将goods表中的数据导入到ES中
     */
    @Test
    public void t2() throws IOException {
        //创建索引
        CreateIndexResponse response = client.indices().create(new CreateIndexRequest("goods")
                //添加映射
                .source(GoodsIndexConstants.MAPPING_TEMPLATE, XContentType.JSON), RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            List<Goods> goods = goodsMapper.selectList(null);
            //循环读取
            for (Goods good : goods) {
                //设置索引
                client.index(new IndexRequest("goods")
                                //设置 id
                                .id(good.getId().toString())
                                //设置 内容 将实体类转化为 JSON 进行存储
                                .source(new ObjectMapper().writeValueAsString(good), XContentType.JSON),
                        RequestOptions.DEFAULT);
            }
        }
    }

    /**
     * 查询品牌是三星的
     */
    @Test
    public void t3() throws IOException {
        //创建 映射索引对象
        SearchRequest goods = new SearchRequest("goods");
        //term查询 等值查询
        goods.source().query(QueryBuilders.termQuery("brandName","三星"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            //进行对象转化
            ObjectMapper objectMapper = new ObjectMapper();
            Goods goods1 = objectMapper.readValue(hit.getSourceAsString(), Goods.class);
            System.out.println("term等值 查询品牌为三星: "+goods1);
        }
    }

    /**
     * 查询title中包含华为手机的
     */
    @Test
    public void t4() throws IOException {
        //创建 映射索引对象
        SearchRequest goods = new SearchRequest("goods");
        //term查询 等值查询
        goods.source().query(QueryBuilders.matchQuery("title","华为手机"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            //进行对象转化
            ObjectMapper objectMapper = new ObjectMapper();
            Goods goods1 = objectMapper.readValue(hit.getSourceAsString(), Goods.class);
            System.out.println("match分词 查询title为华为手机: "+goods1);
        }
    }


}
