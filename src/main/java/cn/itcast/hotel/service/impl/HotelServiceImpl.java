package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.dto.RequestParams;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.HotelService;
import cn.itcast.hotel.vo.PageResult;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: huasheng
 * @Date: 2022/1/12 18:30
 */
@Service
public class HotelServiceImpl implements HotelService {

    @Autowired
    private RestHighLevelClient client;


    /**
     * 多条件 查询
     */
    @Override
    public PageResult hotelLiset(RequestParams params) throws IOException {
        //params.setLocation("31.21, 121.5");
        try {
            //获取链接
            RestHighLevelClient client = getClient();
            //设置 映射
            SearchRequest searchR = new SearchRequest("hotel");
            //获取 多条件 查询对象
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            // 关键字搜索
            String key = params.getKey();
            if (key == null || "".equals(key)) {
                boolQuery.must(QueryBuilders.matchAllQuery());
            } else {
                boolQuery.must(QueryBuilders.multiMatchQuery(key, "name", "brand"));
            }
            // 城市条件
            if (params.getCity() != null && !params.getCity().equals("")) {
                boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
            }
            // 品牌条件
            if (params.getBrand() != null && !params.getBrand().equals("")) {
                boolQuery.filter(QueryBuilders.termQuery("brand", params.getBrand()));
            }
            // 星级条件
            if (params.getStarName() != null && !params.getStarName().equals("")) {
                boolQuery.filter(QueryBuilders.termQuery("starName", params.getStarName()));
            }
            // 价格
            if (params.getMinPrice() != null && params.getMaxPrice() != null) {
                boolQuery.filter(QueryBuilders
                        .rangeQuery("price")
                        .gte(params.getMinPrice())
                        .lte(params.getMaxPrice())
                );
            }

            /*FunctionScoreQueryBuilder queryBuilder = QueryBuilders.functionScoreQuery(boolQuery,
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAD", true),
                                    ScoreFunctionBuilders.weightFactorFunction(10))
                    }).boostMode(CombineFunction.SUM);*/

            // 把广告的数据给存储到ArrayList中

            //进行分页
            searchR.source().query(boolQuery)
                    .from((params.getPage() - 1) * params.getSize())
                    .size(params.getSize());

            // 默认排序是按照位置从近到远
            // 如果用户指定了排序方式 我们按照用户指定的排序方式进行排序
            if ("default".equalsIgnoreCase(params.getSortBy())) {
                // 距离排序 由近及远
                if (!StringUtils.isEmpty(params.getLocation())) {
                    searchR.source()
                            .sort(new GeoDistanceSortBuilder("location", new GeoPoint(params.getLocation()))
                                    .unit(DistanceUnit.KILOMETERS));
                }
            } else if ("score".equalsIgnoreCase(params.getSortBy())) {
                // 评价排序
                searchR.source().sort("score", SortOrder.DESC);
            } else {
                // 价格排序
                searchR.source().sort("price", SortOrder.DESC);
            }

            // Jackson工具
            ObjectMapper mapper = new ObjectMapper();
            // 返回的数据列表
            ArrayList<HotelDoc> hotelDocs = new ArrayList<>();

            //如果是第一页
            if (params.getPage() == 1) {
                // 广告数据列表查询
                SearchRequest sr = new SearchRequest("hotel");
                //等值查询出 带有广告标记的 数据
                SearchSourceBuilder adDocs = sr.source().query(QueryBuilders.termQuery("isAD", true));
                SearchResponse response = client.search(sr, RequestOptions.DEFAULT);
                for (SearchHit hit : response.getHits().getHits()) {
                    //转化为对象
                    HotelDoc hotelDoc = mapper.readValue(hit.getSourceAsString(), HotelDoc.class);
                    //存储到 返回值的 集合中 即广告排在前列的展示
                    hotelDocs.add(hotelDoc);
                }
            }

            // 正常的数据列表查询
            SearchResponse search = client.search(searchR, RequestOptions.DEFAULT);
            // 返回的数据总条数
            long totalCount = search.getHits().getTotalHits().value;
            for (SearchHit hit : search.getHits().getHits()) {
                HotelDoc hotelDoc = mapper.readValue(hit.getSourceAsString(), HotelDoc.class);
                // 获取距离
//                if (ArrayUtil.isNotEmpty(hit.getSortValues())) {
                if (null != hit.getSortValues() && hit.getSortValues().length != 0) {
                    hotelDoc.setDistance(hit.getSortValues()[0]);
                }
                hotelDocs.add(hotelDoc);
            }
            //封装返回值
            return new PageResult(totalCount, hotelDocs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 页面搜索的词条 的聚合搜索
     */
    public Map<String, List<String>> holetFilters() {
        try {
            Map<String, List<String>> map = new HashMap<>();

            //进行 聚合设置
            SearchRequest sr = new SearchRequest("hotel");
            //地址
            sr.source().aggregation(AggregationBuilders.terms("city_group").field("city").size(20));
            //星级
            sr.source().aggregation(AggregationBuilders.terms("start_group").field("starName").size(20));
            //品牌
            sr.source().aggregation(AggregationBuilders.terms("brand_group").field("brand").size(20));
            //查询
            SearchResponse response = client.search(sr, RequestOptions.DEFAULT);

            //获取 地址的 聚合
            ParsedStringTerms city_group = response.getAggregations().get("city_group");
            ArrayList<String> cityList = new ArrayList<>();
            for (Terms.Bucket bucket : city_group.getBuckets()) {
                cityList.add(bucket.getKeyAsString());
            }
            map.put("city", cityList);

            //获取 星级的 聚合
            ParsedStringTerms start_group = response.getAggregations().get("start_group");
            ArrayList<String> startList = new ArrayList<>();
            for (Terms.Bucket bucket : start_group.getBuckets()) {
                startList.add(bucket.getKeyAsString());
            }
            map.put("starName", startList);

            //获取 品牌的 聚合
            ParsedStringTerms brand_group = response.getAggregations().get("brand_group");
            ArrayList<String> brandList = new ArrayList<>();
            for (Terms.Bucket bucket : brand_group.getBuckets()) {
                brandList.add(bucket.getKeyAsString());
            }

            //返回值的 封装
            map.put("brand", brandList);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 搜索框 联想功能
     */
    public List<String> holetSuggestion(String key) {

        try {
            SearchRequest sr = new SearchRequest("hotel");
            //随便 设置 一个字符 用于 下面值的获取
            sr.source().suggest(new SuggestBuilder().addSuggestion("hh",
                    //设置联想的 字段
                    SuggestBuilders.completionSuggestion("suggestion").prefix(key)
                            //条数
                            .size(10)
                            //跳过重复项
                            .skipDuplicates(true)));

            SearchResponse search = client.search(sr, RequestOptions.DEFAULT);
            //封装 返回值
            ArrayList<String> strings = new ArrayList<>();
            //循环获取 参数
            for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> hh : search.getSuggest().getSuggestion("hh").getEntries()) {
                for (Suggest.Suggestion.Entry.Option option : hh.getOptions()) {
                    //获取到 联想的 数据
                    String s = option.getText().string();
                    //封装返回值
                    strings.add(s);
                }
            }
            return strings;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 创建 ES 连接
     */
    private RestHighLevelClient getClient() {
        return client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.136.160:9200")));
    }

}
