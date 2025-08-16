package com.transcend.plm.datadriven.apm.space.pojo.qo;

import com.transcend.plm.datadriven.api.model.MBaseData;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmAccessQo {

    /**
     * 实例数据
     */
    private MBaseData instance;
    /**
     * 当前空间应用bid
     */
    private String spaceAppBid;
    /**
     * 当前空间bid
     */
    private String spaceBid;


}
