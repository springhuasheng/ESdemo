package cn.itcast.hotel;

import cn.itcast.hotel.constants.HotelIndexConstants;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
class Day02HotelIndexTest {

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

    /**
     * 后置操作
     */
    @AfterEach
    public void t00() throws IOException {
        client.close();
    }

    /**
     * 查询全部
     * matchAllQuery()
     */
    @Test
    public void t1() throws IOException {
        //设置 索引
        SearchRequest hotel = new SearchRequest("hotel");
        //matchAllQuery() 查询 全部
        hotel.source().query(QueryBuilders.matchAllQuery());
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            //获取数据
            System.out.println("hit = " + hit.getSourceAsMap());
        }
    }

    /**
     * 分词查询
     * matchQuery() 分词查询
     */
    @Test
    public void t2() throws IOException {
        //设置 索引
        SearchRequest hotel = new SearchRequest("hotel");
        //matchQuery() 分词查询
        hotel.source().query(QueryBuilders.matchQuery("name", "连锁酒店"));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            //获取数据
            System.out.println("hit = " + hit.getSourceAsMap());
        }
    }

    /**
     * hotel多条件 查询
     * multiMatchQuery()
     * "如家" 为 想查询的条件
     * "name","brand" 为 查询的内容
     * "name" 设置映射为 text 所以为 分词 查询此字段
     * "brand" 设置映射为 keyword 所以为 等值 查询此字段
     */
    @Test
    public void t3() throws IOException {
        //先创建 索引
        SearchRequest hotel = new SearchRequest("hotel");
        hotel.source().query(QueryBuilders.multiMatchQuery("如家", "name", "brand"));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("hotel多条件 查询" + hit.getSourceAsMap());
        }

    }

    /**
     * 根据 id 查询
     * idsQuery().addIds() 可传多个参数
     */
    @Test
    public void t4() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        hotel.source().query(QueryBuilders.idsQuery().addIds("36934", "1455383931"));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("idsQuery().addIds()" + hit.getSourceAsMap());
        }
    }

    /**
     * 等值查询
     * termQuery()
     */
    @Test
    public void t5() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        hotel.source().query(QueryBuilders.termQuery("brand", "喜来登"));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("idsQuery().addIds()" + hit.getSourceAsMap());
        }
    }

    /**
     * 数值 范围查询 ( 可能 时间Long 也适用 有待尝试 )
     * rangeQuery() 只能适用于 数值类型的字段
     */
    @Test
    public void t6() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        //价格 大于等于200 小于等于500
        hotel.source().query(QueryBuilders.rangeQuery("price").gte(200).lte(500));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }

    /**
     * 经纬度范围查询
     * geoDistanceQuery() 对应 纬经度字段 location
     * .point() 设置 纬经度
     * .distance() 设置 范围大小 10 千米
     */
    @Test
    public void t7() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        //对应字段 location
        hotel.source().query(QueryBuilders.geoDistanceQuery("location")
                //设置 纬经度
                .point(31.21, 121.5)
                //设置 范围大小 10 千米
                .distance(10, DistanceUnit.KILOMETERS));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("经纬度范围查询" + hit.getSourceAsMap());
        }
    }

    /**
     * boolQuery()多条件查询
     * (名称包含如家 并且 价格低于500) 或者 (地址在上海)
     */
    @Test
    public void t8() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        hotel.source().query(QueryBuilders
                .boolQuery()
                .should(QueryBuilders.termQuery("city", "上海"))
                .should(QueryBuilders
                        .boolQuery()
                        .must(QueryBuilders.matchQuery("name", "如家"))
                        .must(QueryBuilders.rangeQuery("price").lt(500))));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("boolQuery()多条件查询" + hit.getSourceAsMap());
        }
    }

    /**
     * 分页查询 + 普通字段排序
     */
    @Test
    public void t9() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        hotel.source().query(QueryBuilders.matchAllQuery())
                .sort("price", SortOrder.DESC)
                .from(10)  // ( page - 1 ) * pageSize
                .size(10); //pageSize
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }

    /**
     * 根据当前的地理位置 进行查询( 默认: 由近到远)
     * geoDistanceSort()
     */
    @Test
    public void t10() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        //查询全部
        hotel.source().query(QueryBuilders.matchAllQuery())
                //进行地理位置配置
                .sort(SortBuilders.geoDistanceSort("location", 31.21, 121.5));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("当前的地理位置 进行查询" + hit.getSourceAsMap());
        }
    }

    /**
     * 进行分词查询 + 为某个字段进行 加分 操作
     * functionScoreQuery() 功能评分查询
     */
    @Test
    public void t11() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        //进行分词查询
        hotel.source().query(QueryBuilders.functionScoreQuery(QueryBuilders.matchQuery("name", "酒店"),
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        //设置 某个字段 对评分进行操作
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("brand", "如家"),
                                //设置为 10分
                                ScoreFunctionBuilders.weightFactorFunction(10))
                        //不加 boostMode() 默认为相乘操作
                        //进行评分 CombineFunction.SUM 累加 初始分数( ES自动生成的分数 匹配度 ) + 上面设置的 10 分
                }).boostMode(CombineFunction.SUM));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println("分词查询 + 为某个字段进行 加分" + hit.getSourceAsMap());
        }
    }

    /**
     * 字段的高亮替换
     * name=<em>如家</em>酒店(北京上地安宁庄东路店)
     */
    @Test
    public void t12() throws IOException {
        SearchRequest hotel = new SearchRequest("hotel");
        //设置 分词 查询
        hotel.source().query(QueryBuilders.matchQuery("name", "如家"))
                //进行 高亮的字段 设置  后面标签随意
                .highlighter(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            //初始数据
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //拿到高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            //为数组结构
            Text[] fragments = name.getFragments();
            //获取到带标签的name
            String s = fragments[0].toString();
            sourceAsMap.put("name", s);
            System.out.println("字段的高亮替换" + sourceAsMap);
        }
    }

}