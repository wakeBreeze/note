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
public class Ka92AndKa94 {
    /**ka94*/
//    aaz356; // 疑点明细场景流水号
    private String akb020; // 医院编码
    private String aaz217; // 就诊编号
    private String aaz308; // 结算编号
    private String aaz213; // 明细编号
    private String ape835; // 政策依据
    private String ape801; // 扣款原因
    private Long ape805; // 扣款建议数量
    private BigDecimal ape802; // 扣款建议金额
    private String aaz319; // 规则id
    private String aaa027; // 统筹区编码(704新增)
    private String aab034; // 经办机构编号(704新增)
    /**ka92*/
//    aaz328; // 疑点信息流水号
    private String ape800; // 规则类型
    private BigDecimal aze001; // 批次号
    private String aae317; // 疑点描述
}
