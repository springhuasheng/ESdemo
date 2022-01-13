package cn.itcast.hotel;

import cn.itcast.hotel.constants.GoodsIndexConstants;
import cn.itcast.hotel.constants.HotelIndexConstants;
import cn.itcast.hotel.mapper.GoodsMapper;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Goods;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.User;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
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
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
                //插入ES
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
        goods.source().query(QueryBuilders.termQuery("brandName", "三星"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            //进行对象转化
            ObjectMapper objectMapper = new ObjectMapper();
            Goods goods1 = objectMapper.readValue(hit.getSourceAsString(), Goods.class);
            System.out.println("term等值 查询品牌为三星: " + goods1);
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
        goods.source().query(QueryBuilders.matchQuery("title", "华为手机"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            //进行对象转化
            ObjectMapper objectMapper = new ObjectMapper();
            Goods goods1 = objectMapper.readValue(hit.getSourceAsString(), Goods.class);
            System.out.println("match分词 查询title为华为手机: " + goods1);
        }
    }


    /**
     * 根据id查询一条文档
     */
    @Test
    public void t5() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.idsQuery().addIds("536563"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("ids 查询: " + hit.getSourceAsMap());
        }
    }

    /**
     * 根据id删除一条文档
     */
    @Test
    public void t6() throws IOException {
        DeleteResponse goods = client.delete(new DeleteRequest("goods").id("1369283"), RequestOptions.DEFAULT);
        System.out.println("删除成功: " + goods);
    }

    /**
     * 分页查询所有文档
     */
    @Test
    public void t7() throws IOException {
        Integer page = 1;
        Integer pageSize = 1000;
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.matchAllQuery())
                .from((page - 1) * pageSize)
                .size(pageSize);
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("分页查询所有文档: " + hit.getSourceAsMap());
        }
    }

    /**
     * 统计索引中的总条数
     */
    @Test
    public void t8() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.matchAllQuery());
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        Long a = search.getHits().getTotalHits().value;
        System.out.println("goods 总条数为: " + a);
    }

    /**
     * 查询title中包含 荣耀 的文档
     */
    @Test
    public void t9() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.matchQuery("title", "荣耀"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("title+分词查询 荣耀: " + hit.getSourceAsMap());
        }
    }

    /**
     * 查询价格大于等于5000 的文档
     */
    @Test
    public void t10() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.rangeQuery("price").gte(5000));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("价格大于等于5000: " + hit.getSourceAsMap());
        }
    }

    /**
     * 查询分类是 平板电视  的文档
     */
    @Test
    public void t11() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.termQuery("categoryName", "平板电视"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("分类是 平板电视: " + hit.getSourceAsMap());
        }
    }

    /**
     * 查询品牌是  苹果    的文档
     */
    @Test
    public void t12() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.termQuery("brandName", "苹果"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("品牌是 苹果: " + hit.getSourceAsMap());
        }
    }

    /**
     * 查询名称中包含手机且价格低于5000的文档
     */
    @Test
    public void t13() throws IOException {
        SearchRequest goods = new SearchRequest("goods");

        //暂时 存在问题
//        goods.source()
//                .query(QueryBuilders.matchQuery("title", "手机"))
//                .query(QueryBuilders.rangeQuery("price").lt(5000))
//                .sort("price", SortOrder.DESC);

        goods.source().query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title", "手机"))
                .must(QueryBuilders.rangeQuery("price").lt(5000)))
                .sort("price", SortOrder.DESC);

        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("名称中包含手机且价格低于5000: " + hit.getSourceAsMap());
        }
    }

    /**
     * 查询名称中包含手机且品牌是苹果的文档
     */
    @Test
    public void t14() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title", "手机"))
                .must(QueryBuilders.termQuery("brandName", "苹果")));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("名称中包含手机且品牌是苹果: " + hit.getSourceAsMap());
        }
    }

    /**
     * 查询 名称中包含华为 或 者苹果 且 价格低于8000的文档
     */
    @Test
    public void t15() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders
                .boolQuery()
                .must(QueryBuilders.rangeQuery("price").lt(8000))
                .must(QueryBuilders
                        .boolQuery()
                        .should(QueryBuilders.matchQuery("title", "华为"))
                        .should(QueryBuilders.matchQuery("title", "苹果"))));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("名称中包含华为 或 者苹果 且 价格低于8000的文档" + hit.getSourceAsMap());
        }
    }

    /**
     * 查询 名称中包含苹果且价格低于8000 或者 品牌是苹果且价格大于5000的文档
     */
    @Test
    public void t16() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().query(QueryBuilders
                .boolQuery()
                .should(QueryBuilders
                        .boolQuery()
                        .must(QueryBuilders.matchQuery("title", "苹果"))
                        .must(QueryBuilders.rangeQuery("price").lt(8000)))
                .should(QueryBuilders
                        .boolQuery()
                        .must(QueryBuilders.matchQuery("title", "苹果"))
                        .must(QueryBuilders.rangeQuery("price").gt("5000"))));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("名称中包含苹果且价格低于8000 或者 品牌是苹果且价格大于5000: " + hit.getSourceAsMap());
        }
    }

    /**
     * 统计每个分类下的文档数量
     */
    @Test
    public void t17() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        //进行 聚合查询
        goods.source().aggregation(AggregationBuilders
                //随意 下面与之对应 即可   可理解为 key
                .terms("categoryName_agg")
                //分组的 字段
                .field("categoryName"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        //进行获取 要和上面对应  上面的 key
        // 前面的 Terms 需要自己 手动输入
        Terms categoryNameAgg = search.getAggregations().get("categoryName_agg");
        //获取集合
        List<? extends Terms.Bucket> buckets = categoryNameAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            //获取 分组的 类型名称
            String keyAsString = bucket.getKeyAsString();
            //获取统计的数量
            long docCount = bucket.getDocCount();
            System.out.println("分类: " + keyAsString + "-------" + "数量: " + docCount);
        }
    }

    /**
     * 暂时不会
     * 统计品牌是苹果的所有文档的平均价格
     */
    @Test
    public void t18() throws IOException {
        SearchRequest goods = new SearchRequest("goods");
        goods.source().aggregation(AggregationBuilders
                .avg("price_agg")
                .field("brandName"));
        SearchResponse search = client.search(goods, RequestOptions.DEFAULT);
        //进行获取 要和上面对应  上面的 key
        // 前面的 Terms 需要自己 手动输入
        Terms categoryNameAgg = search.getAggregations().get("categoryName_agg");
        Avg categoryName_agg = search.getAggregations().get("categoryName_agg");
        double value = categoryName_agg.getValue();
        System.out.println(value);
//        //获取集合
//        List<? extends Terms.Bucket> buckets = categoryNameAgg.getBuckets();
//        for (Terms.Bucket bucket : buckets) {
//            //获取 分组的 类型名称
//            String keyAsString = bucket.getKeyAsString();
//            //获取统计的数量
//            long docCount = bucket.getDocCount();
//            System.out.println("分类: " + keyAsString + "-------" + "数量: " + docCount);
//        }

    }

}