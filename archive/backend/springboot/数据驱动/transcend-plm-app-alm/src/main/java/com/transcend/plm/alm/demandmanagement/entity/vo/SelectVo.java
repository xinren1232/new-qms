package com.transcend.plm.alm.demandmanagement.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author bin.yin
 * @description: 下拉框vo
 * @version:
 * @date 2024/06/21 16:18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectVo {
    private String bid;
    private String name;
    private String spaceAppBid;
    private String modelCode;
    private String domainLeader;
    private Object domainSe;
    private String nodeId;
    private String nodeParentId;
    /** 是否禁选 **/
    private boolean disabled;
    private List<SelectVo> children;
}
