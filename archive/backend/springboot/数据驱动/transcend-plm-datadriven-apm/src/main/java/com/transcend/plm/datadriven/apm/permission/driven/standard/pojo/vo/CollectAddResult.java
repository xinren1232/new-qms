package com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo;

import lombok.Data;

/**
 * @author unknown
 * 收集新增数据集结果
 * 1.对象类型
 * 2.类型名称
 * 3.空间、空间应用
 *
 */
@Data
public class CollectAddResult {

    private String name;

    private String modelCode;

    private String spaceBid;

    private String spaceAppBid;
}
