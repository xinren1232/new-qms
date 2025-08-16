package com.transcend.plm.datadriven.apm.log.model.dto;

import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 操作日志新增参数
 * @author yinbin
 * @version:
 * @date 2023/10/07 15:55
 */
@Data
@Builder(toBuilder = true)
@ApiModel("操作日志新增参数")
public class OperationLogAddParam {

    @ApiModelProperty("空间bid")
    private String spaceBid;

    @ApiModelProperty("空间应用bid")
    private String spaceAppBid;

    @ApiModelProperty("对象模型编码")
    private String modelCode;

    @ApiModelProperty("实例bid")
    @NotBlank(message = "实例bid不能为空")
    private String instanceBid;

    @ApiModelProperty("操作属性名称")
    @NotBlank(message = "操作属性名称不能为空")
    private String fieldName;

    @ApiModelProperty("操作属性值")
    private Object fieldValue;

    @ApiModelProperty("视图属性元数据")
    private CfgViewMetaDto cfgViewMetaDto;

    @ApiModelProperty("视图属性元数据")
    private Map<String, Object> properties;

    @ApiModelProperty("是否是应用视图, 传true的时候, 视图属性元数据必传")
    private Boolean isAppView;

    @ApiModelProperty("创建人")
    private String createdBy;
}
