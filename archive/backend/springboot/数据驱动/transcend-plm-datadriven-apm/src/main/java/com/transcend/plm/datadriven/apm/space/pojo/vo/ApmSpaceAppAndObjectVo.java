package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/14 11:40
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApmSpaceAppAndObjectVo {
    /** 应用基础信息 **/
    private ApmSpaceApp apmSpaceApp;
    /** 应用对象基础信息 **/
    private CfgObjectVo cfgObjectVo;
}
