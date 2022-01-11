package cn.itcast.hotel.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
@TableName("tb_hotel")
public class Hotel {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;
    private String starName;
    private String business;

    //转化JSON 将此字段排除
    @JsonIgnore
    private String longitude;
    @JsonIgnore
    private String latitude;

    private String pic;
    private String location;

    //获取Location时将 longitude + ", " + latitude 拼接返回
    public String getLocation() {
        return this.longitude + ", " + this.latitude;
    }

}
