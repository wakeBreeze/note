package com.zx.mapper.read;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zx.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: com.zx.mapper.read
 * @version: 1.0
 */
// 在对应的mapper上继承对应的类 baseMapper
@Mapper
public interface UserMapper extends BaseMapper<User> {
    void insert();
    // 所有CRUD操作已经编写完成
    // 不需要像之前那样配一大堆文件了！
}
