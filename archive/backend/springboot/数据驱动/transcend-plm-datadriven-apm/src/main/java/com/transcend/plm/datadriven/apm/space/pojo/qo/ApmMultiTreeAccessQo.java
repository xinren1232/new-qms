package com.transcend.plm.datadriven.apm.space.pojo.qo;

import com.transcend.plm.datadriven.api.model.MBaseData;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmMultiTreeAccessQo {

    /**
     * 实例数据
     */
    private MBaseData instanceData;
    /**
     * 目标modelCode
     */
    private String targetModelCode;
    /**
     * 目标spaceAppBid
     */
    private String targetSpaceAppBid;
    /**
     * 入口： 应用入口：app, 关系tab入口：relation
     */
    private String entrance;

    /**
     * 当前空间应用bid
     */
    private String spaceAppBid;
    /**
     * 当前空间bid
     */
    private String spaceBid;

}
