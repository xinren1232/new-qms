package com.transcend.plm.datadriven.apm.space.pojo.qo;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.MultiAppConfig;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.utils.MultiTreeUtils;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 多对象树查询对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/30 11:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApmMultiTreeModelMixQo extends ModelMixQo {

    /**
     * 输出模型编码列表
     */
    @ApiModelProperty(value = "需要输出的模型编码")
    private List<String> outputModelCodeList;

    /**
     * 严格模式
     * 严格模式下，数据将不进行补偿，搜索出来才展示
     */
    @ApiModelProperty(value = "开启严格模式")
    private Boolean strictMode;

    /**
     * 多对象配置
     */
    @ApiModelProperty(value = "多对象配置")
    private MultiAppConfig multiAppConfig;

    /**
     * 是否需要补偿树bid
     * 如果当前数据查询结果非顶层数据，则需要补偿顶层的bid
     */
    @ApiModelProperty(value = "需要补偿树bid")
    private Boolean needCompensationTreeBid;


    public static ApmMultiTreeModelMixQo of(ModelMixQo modelMixQo) {
        ApmMultiTreeModelMixQo mixQo = new ApmMultiTreeModelMixQo();
        if (modelMixQo == null) {
            return mixQo;
        }
        BeanUtils.copyProperties(modelMixQo, mixQo);
        return mixQo;
    }

    @NotNull
    public static ApmMultiTreeModelMixQo of(ApmMultiTreeDto apmMultiTreeDto) {
        ApmMultiTreeModelMixQo modelMixQo = apmMultiTreeDto.getModelMixQo();
        List<String> outputModelCodeList = modelMixQo.getOutputModelCodeList();
        if (CollectionUtils.isEmpty(outputModelCodeList)) {
            IApmSpaceAppConfigManageService service = PlmContextHolder.getBean(IApmSpaceAppConfigManageService.class);
            //根据targetSpaceAppBid 查modelCode
            outputModelCodeList = service.getAppModelCodes(apmMultiTreeDto.getSpaceAppBid(), apmMultiTreeDto.getTargetSpaceAppBid());
        }
        //将分组放置到条件的分组参数中
        MultiTreeUtils.group2Condition(apmMultiTreeDto);
        modelMixQo.setMultiAppConfig(apmMultiTreeDto.getMultiAppTreeContent());
        modelMixQo.setOutputModelCodeList(outputModelCodeList);
        modelMixQo.setNeedCompensationTreeBid(true);
        return modelMixQo;
    }
}
