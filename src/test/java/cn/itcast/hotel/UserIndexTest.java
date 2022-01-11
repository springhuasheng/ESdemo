package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

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
    public void t2() throws IOException{
        boolean user = client.indices().exists(new GetIndexRequest("user"), RequestOptions.DEFAULT);
        if (user) {
            System.out.println("存在 user");
        }

    }

    @Test
    public void t3() throws IOException{
        AcknowledgedResponse user = client.indices().delete(new DeleteIndexRequest("user"), RequestOptions.DEFAULT);
        if (user.isAcknowledged()) {
            System.out.println("删除成功");
        }
    }

    @Test
    public void t4() throws IOException{
        client.indices().putMapping(new PutMappingRequest("user").source(""))
    }




}
