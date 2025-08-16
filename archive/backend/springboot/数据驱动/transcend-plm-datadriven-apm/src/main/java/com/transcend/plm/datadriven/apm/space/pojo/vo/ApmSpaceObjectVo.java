package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import lombok.*;

/**
 * @author yinbin
 * @version:
 * @date 2023/11/01 09:28
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApmSpaceObjectVo extends CfgObjectVo {

    /** 空间应用bid **/
    private String spaceAppBid;
}
