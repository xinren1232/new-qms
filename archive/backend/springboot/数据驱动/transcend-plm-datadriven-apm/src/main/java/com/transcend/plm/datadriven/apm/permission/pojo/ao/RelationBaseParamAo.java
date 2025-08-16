package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Qiu Yuhao
 * @Date 2024/1/22 14:52
 * @Describe 关系基础参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RelationBaseParamAo {

    /**
     * 源应用bid
     */
    private String sourceSpaceAppBid;

    /**
     * 目标应用bid
     */
    private String targetSpaceAppBid;
}
