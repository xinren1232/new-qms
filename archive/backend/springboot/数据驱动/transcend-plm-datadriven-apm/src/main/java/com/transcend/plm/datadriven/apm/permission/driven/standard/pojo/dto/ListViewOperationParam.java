package com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto;

import lombok.Data;

/**
 * @author unknown
 * 查询列表过滤入参
 * 需要条件：
 * 1.空间、空间应用
 * 3.具体操作code
 */
@Data
public class ListViewOperationParam {

    private String spaceBid;

    private String spaceAppBid;

}
