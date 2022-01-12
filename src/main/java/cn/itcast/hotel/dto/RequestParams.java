package cn.itcast.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Dto
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestParams implements Serializable {
    private String key;//搜索栏
    private Integer page;
    private Integer size;
    private String sortBy;//排序字段
    private String brand;//品牌
    private String city;//地址
    private String starName;//星级
    private Integer minPrice;//最低价格
    private Integer maxPrice;//最高价格
    private String location;//经纬度
}
