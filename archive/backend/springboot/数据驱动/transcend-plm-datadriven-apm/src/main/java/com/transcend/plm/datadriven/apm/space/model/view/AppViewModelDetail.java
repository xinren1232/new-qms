package com.transcend.plm.datadriven.apm.space.model.view;

import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 应用视图模型详情
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@ApiModel("应用视图详情")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppViewModelDetail {


    @ApiModelProperty("视图信息")
    private CfgViewVo cfgViewVo;

    @ApiModelProperty("配置内容")
    private Map<String, Object> configContent;

    public static AppViewModelDetail of(){
        return new AppViewModelDetail();
    }

}
