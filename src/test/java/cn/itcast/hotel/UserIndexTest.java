package cn.itcast.hotel;

import cn.itcast.hotel.pojo.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class UserIndexTest {


    // 我们在实际开发中会大量的接触到新的陌生的API
    // 我们一定要靠经验去猜  去使用
    // RestHighLevelClient 原生的API
    private RestHighLevelClient client;


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


    @Test
    void t1() throws IOException {
        // 创建索引
        // ES 是一个web服务器  给ES发请求
        // RequestOptions 是不是往请求头中添加信息
        CreateIndexResponse response = client.indices().create(new CreateIndexRequest("user"), RequestOptions.DEFAULT);
        if (!response.isAcknowledged()) {
            System.out.println("索引创建失败");
        }
    }


    @Test
    void t2() throws IOException {
        // 判断一个索引是否存在
        boolean exists = client.indices().exists(new GetIndexRequest("user"), RequestOptions.DEFAULT);
        System.out.println("exists = " + exists);
    }


    @Test
    void t3() throws IOException {
        // 删除一个索引
        AcknowledgedResponse response = client.indices().delete(new DeleteIndexRequest("user"), RequestOptions.DEFAULT);
        System.out.println("exists = " + response.isAcknowledged());
    }


    @Test
    void t4() throws IOException {
        // 给索引设置映射
        // 给哪个索引添加映射  映射是啥
        PutMappingRequest request = new PutMappingRequest("user");
        request.source("{\"properties\": {\n" +
                "      \"name\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"id\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"address\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_smart\"\n" +
                "      }\n" +
                "    }}", XContentType.JSON);
        AcknowledgedResponse response = client.indices().putMapping(request, RequestOptions.DEFAULT);
        System.out.println("exists = " + response.isAcknowledged());
    }

    @Test
    void t5() throws IOException {
        // 创建索引并且添加映射
        // ES 是一个web服务器  给ES发请求
        // RequestOptions 是不是往请求头中添加信息
        CreateIndexResponse response = client.indices()
                .create(new CreateIndexRequest("user").source("{\n" +
                        "  \"mappings\": {\n" +
                        "    \"properties\": {\n" +
                        "      \"name\": {\n" +
                        "        \"type\": \"keyword\"\n" +
                        "      },\n" +
                        "      \"id\": {\n" +
                        "        \"type\": \"long\"\n" +
                        "      },\n" +
                        "      \"address\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"analyzer\": \"ik_smart\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", XContentType.JSON), RequestOptions.DEFAULT);
        if (!response.isAcknowledged()) {
            System.out.println("索引创建失败");
        }
    }

    @Test
    void t6() throws IOException {
        // 添加一个文档到索引库
        // 告诉API 往哪个索引库中添加  添加的文档内容是啥
        IndexRequest indexRequest = new IndexRequest("user");
        // 这个id是_id  我们可以给它赋值 如果不赋值默认是随机串
        //indexRequest.id("1");
        // 第一种是搞一个Map添加
        /*Map<String, Object> mm = new HashMap<>();
        mm.put("id", 1L);
        mm.put("name", "zhs");
        mm.put("address", "杭州下沙");
        indexRequest.source(mm);*/


        // 接收前台的请求 . 自己去查询数据库
        // 我们拿到的数据通常都是Bean去封装的  Dto/POJO

        // 第二种是搞一个JSON字符串添加
        // 把User对象转成JSON字符串
        indexRequest.source(new ObjectMapper().writeValueAsString(User.builder()
                .id(2L)
                .name("lisi")
                .address("杭州西湖")
                .build()), XContentType.JSON);
        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("index = " + index.getId());
    }

    @Test
    void t7() throws IOException {
        // 删除一个文档到索引库
        // 我要删除哪个索引库中的哪个文档
        DeleteResponse delete = client.delete(new DeleteRequest("user").id("cJ4mSH4BCdS7xTwQEytQ"), RequestOptions.DEFAULT);
        System.out.println("delete = " + delete);
    }

    @Test
    void t8() throws IOException {
        // 修改一个文档到索引库
        // 我要修改哪个库中的哪个文档 修改的内容是什么
        Map<String, Object> mm = new HashMap<>();
        mm.put("name", "zhaoliu");
        UpdateResponse update = client.update(new UpdateRequest("user", "1").doc(mm), RequestOptions.DEFAULT);
        System.out.println("update = " + update);


        /*UpdateRequest updateR = new UpdateRequest();
        updateR.index("user");
        updateR.id("b54iSH4BCdS7xTwQxytr");
        updateR.doc(new ObjectMapper().writeValueAsString(User.builder().name("zhangsan").build()), XContentType.JSON);
        UpdateResponse update = client.update(updateR, RequestOptions.DEFAULT);
        System.out.println("update = " + update);*/
    }

    @Test
    void t9() throws IOException {
        // 查询全部
        /*SearchRequest searchR = new SearchRequest("user");
        SearchResponse response = client.search(searchR, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits().getHits()) {
            //System.out.println(hit.getSourceAsMap());
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(hit.getSourceAsString(), User.class);
            System.out.println("user = " + user);
        }*/

        // term查询
        // 查询name=zhaoliu
        /*SearchRequest searchR = new SearchRequest("user");
        searchR.source().query(QueryBuilders.termQuery("name", "zhaoliu"));
        SearchResponse response = client.search(searchR, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits().getHits()) {
            //System.out.println(hit.getSourceAsMap());
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(hit.getSourceAsString(), User.class);
            System.out.println("user = " + user);
        }*/

        // match查询
        SearchRequest searchR = new SearchRequest("user");
        searchR.source().query(QueryBuilders.matchQuery("address", "中国杭州"));
        SearchResponse response = client.search(searchR, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits().getHits()) {
            //System.out.println(hit.getSourceAsMap());
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(hit.getSourceAsString(), User.class);
            System.out.println("user = " + user);
        }
    }

}