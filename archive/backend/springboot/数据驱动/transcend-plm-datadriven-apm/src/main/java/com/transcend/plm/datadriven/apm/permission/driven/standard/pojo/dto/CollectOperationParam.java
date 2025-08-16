package com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 * 收集实例操作入参
 * 需要条件：
 * 1.空间、空间应用
 */
@Data
public class CollectOperationParam {

    private List<String> insBids;

    private String spaceBid;

    private String spaceAppBid;
}
