package com.transcend.plm.configcenter.object.domain.entity;

import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectAttrRoot;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectAttributeRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectHistoryGetOneParam;
import com.transcend.plm.configcenter.api.model.object.vo.*;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.exception.BusinessException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yinbin
 * @version:
 * @date 2023/01/07 11:37
 */
@Slf4j
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CfgObjectHistory {
    @Resource
    private CfgObjectRepository cfgObjectRepository = PlmContextHolder.getBean(CfgObjectRepository.class);

    @Resource
    private CfgObjectAttributeRepository cfgObjectAttributeRepository = PlmContextHolder.getBean(CfgObjectAttributeRepository.class);

    @ApiModelProperty(value = "模型code(优化使用)")
    private String modelCode;

    @ApiModelProperty(value = "版本")
    private Integer version;

    @ApiModelProperty(value = "modelCode+version的唯一id")
    private String modelVersionCode;

    @ApiModelProperty(value = "父的modelCode+version的唯一id")
    private String parentModelVersionCode;

    @ApiModelProperty(value = "父对象版本")
    private Integer parentVersion;

    @ApiModelProperty(value = "业务编码（对应SAP的编码）")
    private String code;

    @ApiModelProperty(value = "对象名称")
    private String name;

    @ApiModelProperty(value = "图标(存放路径)")
    private String avatar;

    @ApiModelProperty(value = "使用状态，禁用disable，启用enable，未启用off(默认off)")
    private Integer enableFlag;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "根模型")
    private String baseModel;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "是否有草稿")
    private Boolean draft;

    @ApiModelProperty(value = "是否关系对象")
    private Boolean relationFlag;

    @ApiModelProperty(value = "是否有版本")
    private Byte versionFlag;

    @ApiModelProperty(value = "锁信息：提示哪个对象被谁锁了")
    private Byte lockInfo;

    @ApiModelProperty(value = "检出人")
    private String checkoutBy;

    @ApiModelProperty(value = "属性")
    private List<CfgObjectAttributeVo> attrList;

    @ApiModelProperty(value = "生命周期")
    private ObjectModelLifeCycleVO lifeCycle;

    @ApiModelProperty(value = "权限")
    private List<CfgObjectPermissionVo> authList;

    @ApiModelProperty(value = "视图")
    private List<ObjectViewVO> viewList;

    /**
     * 构建getOne ObjectModelHistory
     */
    public static CfgObjectHistory buildWithGetOne(ObjectHistoryGetOneParam objectHistoryGetOneParam) {
        return BeanUtil.copy(objectHistoryGetOneParam, CfgObjectHistory.class);
    }

    /**
     * 构建vo返回
     */
    public CfgObjectVo build() {
        return BeanUtil.copy(this, CfgObjectVo.class);
    }

    /**
     * 对象模型行为 - getOne -填充基本信息
     */
    public CfgObjectHistory populateBaseInfo() {
        // 历史表
        CfgObjectPo po = cfgObjectRepository.findHistory(modelCode, version);
        if (po == null) {
            throw new BusinessException("对象未找到");
        }
        return BeanUtil.copy(po, CfgObjectHistory.class);
    }

    /**
     * 对象模型行为 - getOne -填充属性
     */
    public CfgObjectHistory populateAttrList() {
        //根据modelCode查询出对象属性并填充
        List<CfgObjectAttributeVo> list = CfgObjectAttrRoot.build().findHistoryByModelCode(modelCode, version);
        this.setAttrList(list);
        return this;
    }
}
