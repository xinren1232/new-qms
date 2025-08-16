package com.transcend.plm.datadriven.apm.space.pojo.qo;

import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppDto;
import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmSpaceQo {
    /**
     * 业务id
     */
    private String bid;

    /**
     * 领域id
     */
    private String sphereBid;

    /**
     * 名称
     */
    private String name;

    /**
     * 说明
     */
    private String description;

//    /**
//     * 图标url
//     */
//    private String iconUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否是模板，0不是，1是
     */
    private Boolean templateFlag;

    /**
     * 模板业务id
     */
    private String templateBid;

    private List<ApmSpaceAppDto> apmSpaceAppDtos;


}
