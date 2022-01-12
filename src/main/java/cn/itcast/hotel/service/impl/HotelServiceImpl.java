package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.dto.RequestParams;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.HotelService;
import cn.itcast.hotel.vo.PageResult;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: huasheng
 * @Date: 2022/1/12 18:30
 */
@Service
public class HotelServiceImpl implements HotelService {

    @Autowired
    private RestHighLevelClient client;


    @Override
    public PageResult hotelLiset(RequestParams req) throws IOException {
        RestHighLevelClient client = getClient();
        SearchRequest hotel = new SearchRequest("hotel");
        //进行分词查询
        if (!StringUtils.isEmpty(req.getKey())) {
            hotel.source().query(QueryBuilders
                    .multiMatchQuery(req.getKey(), "name", "brand"));
        }
        //星级
        if (!StringUtils.isEmpty(req.getStarName())) {
            hotel.source().query(QueryBuilders
                    .termQuery("starName", req.getStarName()));
        }
        //最低价格
        if (null != req.getMinPrice()) {
            hotel.source().query(QueryBuilders
                    .rangeQuery("price").gte(req.getMinPrice()));
        }
        //最高价格
        if (null != req.getMaxPrice()) {
            hotel.source().query(QueryBuilders
                    .rangeQuery("price").lte(req.getMaxPrice()));
        }
        //品牌
        if (!StringUtils.isEmpty(req.getBrand())) {
            hotel.source().query(QueryBuilders
                    .termQuery("brand", req.getBrand()));
        }
        //地址
        if (!StringUtils.isEmpty(req.getCity())) {
            hotel.source().query(QueryBuilders
                    .termQuery("city", req.getCity()));
        }
        //排序
        if (!req.getSortBy().equals("default")) {
            hotel.source().sort(req.getSortBy(), SortOrder.DESC);
        }
        //经纬度 地理位置
        if (!StringUtils.isEmpty(req.getLocation())) {
            GeoDistance geoDistance = JSON.parseObject(req.getLocation(), GeoDistance.class);
            hotel.source().query(QueryBuilders.geoDistanceQuery("location").geoDistance(geoDistance));
        }

        hotel.source().from((req.getPage() - 1) * req.getSize())
                .size(req.getSize());

        //错误示例 传入 null或"" 会报错
//        hotel.source().query(QueryBuilders
//                .boolQuery()
//                .must(QueryBuilders
//                        .boolQuery()
//                        .must(QueryBuilders.termQuery("starName",
//                                StringUtils.isEmpty(req.getStarName()) ? "" : req.getStarName()))
//                        .must(QueryBuilders.rangeQuery("price")
//                                .gte(null == req.getMinPrice() ? 0 : req.getMinPrice())
//                                .lte(null == req.getMaxPrice() ? 1000000 : req.getMaxPrice())))
//                .must(QueryBuilders
//                        .boolQuery()
//                        .must(QueryBuilders.termQuery("brand",
//                                StringUtils.isEmpty(req.getBrand()) ? "" : req.getBrand()))
//                        .must(QueryBuilders.termQuery("city",
//                                StringUtils.isEmpty(req.getCity()) ? "" : req.getCity()))))
//                .sort("price", SortOrder.DESC)
//                .from((req.getPage() - 1) * req.getSize())
//                .size(req.getSize());

        SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
        SearchHits hits = search.getHits();
        TotalHits totalHits = hits.getTotalHits();
        Long aLong = totalHits.value;
        List<HotelDoc> collect = Arrays.stream(search.getHits().getHits()).map(c -> {
            return JSON.parseObject(c.getSourceAsString(), HotelDoc.class);
        }).collect(Collectors.toList());
        PageResult pageResult = new PageResult(aLong, collect);
        return pageResult;
    }

    /**
     * 创建 ES 连接
     */
    private RestHighLevelClient getClient() {
        return client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.136.160:9200")));
    }

}
