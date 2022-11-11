package com.zx.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: com.zx.pojo
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    // 对应数据库中的主键（uuid，自增id，雪花算法，redis，zookeeper！）
    // 这里默认用的雪花算法
//    @TableId(type = IdType.ID_WORKER)
    @TableId(type = IdType.AUTO)// 自增ID
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
