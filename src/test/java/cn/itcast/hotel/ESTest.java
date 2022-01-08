package cn.itcast.hotel;

import cn.itcast.hotel.pojo.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ESTest {

    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.31.31:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }


    @Test
    void testAddDocument() throws IOException {
        CreateIndexRequest xxx = new CreateIndexRequest("user2");
        xxx.source("{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"name\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"address\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_smart\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n", XContentType.JSON);
        client.indices().create(xxx, RequestOptions.DEFAULT);
    }


    @Test
    void  sdfa() throws Exception {
        BulkRequest xx = new BulkRequest("user2");
        xx.add(new IndexRequest().source(new ObjectMapper().writeValueAsString(User.builder().address("杭州下沙").id(1l).name("熊大").build()), XContentType.JSON));
        xx.add(new IndexRequest().source(new ObjectMapper().writeValueAsString(User.builder().address("杭州余杭").id(2l).name("熊二").build()), XContentType.JSON));
        xx.add(new IndexRequest().source(new ObjectMapper().writeValueAsString(User.builder().address("杭州西湖").id(3l).name("光头强").build()), XContentType.JSON));
        xx.add(new IndexRequest().source(new ObjectMapper().writeValueAsString(User.builder().address("北京下沙").id(4l).name("李老板").build()), XContentType.JSON));
        client.bulk(xx, RequestOptions.DEFAULT);
    }
    @Test
    void testAddDocument2() throws IOException {



        // 添加文档  User.builder().address("杭州下沙黑马").uid(2l).name("zhs").build()
        /*IndexRequest indexxx = new IndexRequest("user");
        indexxx.id("1");
        Map<String, Object> mm = new HashMap<>();
        mm.put("uid", 1L);
        mm.put("name", "mm");
        mm.put("address", "杭州下沙");

        indexxx.source(mm);
        client.index(indexxx, RequestOptions.DEFAULT);*/

        /*client.index(new IndexRequest("user").source(new ObjectMapper().writeValueAsString(User.builder().address("杭州下沙黑马").uid(2l).name("zhs").build())
                , XContentType.JSON)
                , RequestOptions.DEFAULT);*/


        // 修改文档






        // 删除文档
        /*DeleteRequest dexx = new DeleteRequest("user");
        dexx.id("FwzeN34Bgx0CGjGXin8F");
        new DeleteRequest("user").id("121212");
        client.delete(dexx, RequestOptions.DEFAULT);*/

        // 查询文档
        // 查询所有
        /*SearchRequest xxx = new SearchRequest("user");
        SearchSourceBuilder sdas = new SearchSourceBuilder();
        QueryBuilder wer = new MatchAllQueryBuilder();
        sdas.query(wer);
        xxx.source(sdas);
        SearchResponse search = client.search(xxx, RequestOptions.DEFAULT);
        // search.getHits().getHits()[0].getSourceAsMap()
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("hit = " + hit.getSourceAsMap());
        }*/
        // 根据id查询
        /*SearchRequest sdf = new SearchRequest("user2");
        SearchSourceBuilder sss = new SearchSourceBuilder();
        QueryBuilder sdfafa = new TermQueryBuilder("id", "3");
        SearchResponse search = client.search(sdf, RequestOptions.DEFAULT);
        System.out.println("search = " + search.getHits().getHits()[0].getSourceAsMap());*/


        /*SearchRequest xxx = new SearchRequest("user2");
        xxx.source(new SearchSourceBuilder().query(new TermQueryBuilder("_id", "JwyrOH4Bgx0CGjGXZ3_o")));
        SearchResponse search = client.search(xxx, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("hit = " + hit.getSourceAsMap());
        }*/

        // term查询

        // match查询

        SearchRequest request = new SearchRequest("user2");
        request.source().query(
                QueryBuilders.matchQuery("address", "北京")
        );
        // 3.发送请求，得到响应
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println("hit.getSourceAsMap() = " + hit.getSourceAsMap());
        }

    }


}
