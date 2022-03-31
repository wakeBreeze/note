package com.zx.mapper.read;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zx.pojo.Ka92AndKa94;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description: com.zx.mapper.read
 * @version: 1.0
 */
@Mapper
public interface Ka92AndKa94ReadMapper{
    List<Ka92AndKa94> getKa92AndKa94s();

    String getAaz356();//获取序列
    String getAaz328();//获取序列
}
