package com.zx.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: com.zx.pojo
 * @version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ka92 {
    private String aaz328; // 疑点信息流水号
    private String aaz319; // 规则id
    private String ape800; // 规则类型
    private BigDecimal aze001; // 批次号
    private String aaz217; // 就诊编号
    private String aae317; // 疑点描述
    private String aaa027; // 统筹区编码(704新增)
    private String aab034; // 经办机构编号(704新增)
}
