package com.transcend.plm.datadriven.api.model.qo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class ResourceQo {

    private String spaceBid;
    private String spaceAppBid;
    /**
     * 项目实例bid
     */
    private String instanceBid;
    /**
     * 多个项目实例bid
     */

    private List<String> instanceBids;


    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 开始时间
     */
    private Date endTime;

    private ModelMixQo modelMixQo;
}
