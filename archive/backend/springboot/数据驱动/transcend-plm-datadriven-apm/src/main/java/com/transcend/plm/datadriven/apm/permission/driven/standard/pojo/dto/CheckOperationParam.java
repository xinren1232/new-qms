package com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 * 检查实例操作入参
 * 需要条件：
 * 1.空间、空间应用
 * 2.实例bids
 * 3.具体操作code
 */
@Data
public class CheckOperationParam {

    private List<String> insBids;

    private String spaceBid;

    private String spaceAppBid;

    private String operationCode;

    private String operationName;
}
