package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.api.model.vo.ApmSpaceAppVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 空间视图树视图对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/18 17:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApmSpaceViewTreeVo extends ApmSpaceVo {

    /**
     * 子层数据
     */
    private List<ApmSpaceAppViewTreeVo> children;

    /**
     * 应用视图树对象
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ApmSpaceAppViewTreeVo extends ApmSpaceAppVo {

        /**
         * 视图列表
         */
        private List<CfgViewVo> children;
    }
}
