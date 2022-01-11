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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class UserIndexTest {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 前置操作
     * 创建连接ES对象
     */
    @BeforeEach
    public void t0() {
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.136.160:9200")));
    }

    /**
     * 后置操作
     * 关流
     */
    @AfterEach
    public void t00() throws IOException {
        client.close();
    }

    @Test
    public void t1() throws IOException {
        CreateIndexRequest user = new CreateIndexRequest("user");
        CreateIndexResponse response = client.indices().create(user, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            System.out.println("创建索引成功");
        }
    }

    @Test
    public void t2() throws IOException {
        boolean user = client.indices().exists(new GetIndexRequest("user"), RequestOptions.DEFAULT);
        if (user) {
            System.out.println("存在 user");
        }

    }

    @Test
    public void t3() throws IOException {
        AcknowledgedResponse user = client.indices().delete(new DeleteIndexRequest("user"), RequestOptions.DEFAULT);
        if (user.isAcknowledged()) {
            System.out.println("删除成功");
        }
    }

    @Test
    public void t4() throws IOException {
        AcknowledgedResponse user = client.indices().putMapping(new PutMappingRequest("user")
                .source("{\"properties\": {\n" +
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
                        "    }}", XContentType.JSON), RequestOptions.DEFAULT);
        if (user.isAcknowledged()) {
            System.out.println("添加映射成功");
        }
    }


    @Test
    public void t5() throws IOException {
        IndexRequest user = new IndexRequest("user");
        user.source(new ObjectMapper().writeValueAsString(User.builder()
                .id(3L)
                .name("啊giao")
                .address("中国河南").build()), XContentType.JSON);
        IndexResponse index = client.index(user, RequestOptions.DEFAULT);
        System.out.println(index.getId());
    }

    @Test
    public void t6() throws IOException {
        DeleteResponse delete = client.delete(new DeleteRequest("user").id("ojwOSX4B_1QmOvE1XqDa"), RequestOptions.DEFAULT);
        System.out.println("删除成功" + delete);
    }

    @Test
    public void t7() throws IOException {
        Map<String, Object> mm = new HashMap<>();
        mm.put("name", "老毕灯");
        UpdateResponse user = client.update(new UpdateRequest("user", "ozwVSX4B_1QmOvE1-KDl").doc(mm), RequestOptions.DEFAULT);
        System.out.println(user);
    }

    @Test
    public void t8() throws IOException {
        //查询全部
//        SearchRequest user = new SearchRequest("user");
//        user.source().query(QueryBuilders.matchQuery("address","中国湖南"));
//        SearchResponse search = client.search(user, RequestOptions.DEFAULT);
//        for (SearchHit hit : search.getHits().getHits()) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            User user1 = objectMapper.readValue(hit.getSourceAsString(), User.class);
//            System.out.println("查询到"+user1);
//        }

        //term查询 等值查
//        SearchRequest user = new SearchRequest("user");
//        user.source().query(QueryBuilders.termQuery("address","中国湖南"));
//        SearchResponse search = client.search(user, RequestOptions.DEFAULT);
//        for (SearchHit hit : search.getHits().getHits()) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            User user1 = objectMapper.readValue(hit.getSourceAsString(), User.class);
//            System.out.println("term查询"+user1);
//        }

        //match查询 分词查
        SearchRequest user = new SearchRequest("user");
        user.source().query(QueryBuilders.matchQuery("address", "湖南"));
        SearchResponse search = client.search(user, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            ObjectMapper objectMapper = new ObjectMapper();
            User user1 = objectMapper.readValue(hit.getSourceAsString(), User.class);
            System.out.println("match查询" + user1);
        }

    }


}
