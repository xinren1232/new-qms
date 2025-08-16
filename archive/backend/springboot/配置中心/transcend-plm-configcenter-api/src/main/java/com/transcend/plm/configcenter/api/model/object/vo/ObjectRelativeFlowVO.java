package com.transcend.plm.configcenter.api.model.object.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * ObjectRelativeFlowVO
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/07/20 16:04
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class ObjectRelativeFlowVO {

    private Long id;

    private String bid;


    private String flowCode;


    private String flowName;

    private String roleCode;

    private String objBid;

    private String description;

    private Integer enableFlag;


}
