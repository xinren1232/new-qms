package com.transcend.plm.configcenter.objectview.infrastructure.pojo.qo;

import lombok.Data;

import java.io.Serializable;
@Data
public class CfgViewConfigQo implements Serializable {
    /**
     * 视图名称
     */
    private String name;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Integer enableFlag;

    /**
     * 模型code
     */
    private String modelCode;
}
