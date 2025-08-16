package com.transcend.plm.datadriven.apm.space.model.view;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 对象模型
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@ApiModel("应用视图模式")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppViewModelDto {

    @ApiModelProperty("bid")
    private String bid;

    @ApiModelProperty("spaceAppBid")
    private String spaceAppBid;

    @ApiModelProperty("code")
    private String code;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("启用状态")
    private Integer enableFlag;

    @ApiModelProperty("视图内容")
    private Map<String, Object> viewContent;

    @ApiModelProperty("元模型")
    private Map<String, Object> mataModel;

    @ApiModelProperty("配置信息")
    private Map<String, Object> configContent;

    public static AppViewModelDto of() {
        return new AppViewModelDto();
    }
}
