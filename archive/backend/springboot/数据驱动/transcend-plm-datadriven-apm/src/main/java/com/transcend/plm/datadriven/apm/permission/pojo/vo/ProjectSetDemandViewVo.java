package com.transcend.plm.datadriven.apm.permission.pojo.vo;

import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppTabHeader;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ProjectSetDemandViewVo {
    private String spaceAppBid;
    private CfgViewVo cfgViewVo;
    private ApmAppTabHeader apmAppTabHeader;
}
