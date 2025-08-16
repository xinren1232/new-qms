package com.transcend.plm.datadriven.apm.space.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多对象树结构配置
 *
 * @author yinbin
 * @version:
 * @date 2023/10/11 09:10
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MultiTreeConfigVo {

    /**
     * 源对象模型编码
     */
    private String sourceModelCode;
    /**
     * 关系对象模型编码
     */
    private String relationModelCode;
    /**
     * 目标对象
     */
    private MultiTreeConfigVo targetModelCode;
    /**
     * 实例查询需要权限
     */
    private Boolean instanceQueryPermission = true;

    /**
     * 是筛选开发权限
     */
    private Boolean developmentPermission = false;

    /**
     * 实例查询过滤空间
     */
    private Boolean instanceQueryFilterSpace = true;


}
