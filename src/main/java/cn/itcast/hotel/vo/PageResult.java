package cn.itcast.hotel.vo;

import cn.itcast.hotel.pojo.HotelDoc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

// VO对象
@Data
@Builder
public class PageResult implements Serializable {
    private Long total;  // 总条数
    private List<HotelDoc> hotels;

    public PageResult() {
    }

    public PageResult(Long total, List<HotelDoc> hotels) {
        this.total = total;
        this.hotels = hotels;
    }
}
