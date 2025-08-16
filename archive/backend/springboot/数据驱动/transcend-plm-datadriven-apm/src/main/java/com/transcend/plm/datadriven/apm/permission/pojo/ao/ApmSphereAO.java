package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 空间领域AO
 * @createTime 2023-09-25 15:53:00
 */
@Data
public class ApmSphereAO {
    /**
     * 业务bid
     * 空间时，为bid
     * 对象时，为modelCode
     * 对象实例时，为bid
     */
    private String bizBid;
    /**
     * 类型 :space空间，object对象，instance对象下实例
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 父级业务bid
     */
    private String pBizBid;
    private String pType;
}
