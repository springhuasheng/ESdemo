package cn.itcast.hotel.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @JsonIgnore
    private String longitude;
    @JsonIgnore
    private String latitude;
    private String pic;
    @TableField(exist = false)
    private String location;

    public String getLocation() {
        // 先写纬度  再写经度
        return this.latitude + ", " + this.longitude;
    }

}
