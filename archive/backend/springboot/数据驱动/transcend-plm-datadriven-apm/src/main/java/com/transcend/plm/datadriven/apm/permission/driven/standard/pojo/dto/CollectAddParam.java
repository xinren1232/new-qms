package com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto;

import lombok.Data;

/**
 * @author unknown
 * 收集新增数据集入参
 * 需要条件：
 * 1.空间、空间应用
 */
@Data
public class CollectAddParam {

    private String spaceBid;

    private String spaceAppBid;
}
