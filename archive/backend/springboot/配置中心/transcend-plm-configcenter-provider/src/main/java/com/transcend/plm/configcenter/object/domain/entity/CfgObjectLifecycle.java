package com.transcend.plm.configcenter.object.domain.entity;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleEditParam;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectLifeCycleSaveParam;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.object.infrastructure.extension.ExtensionService;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectLifeCyclePo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLifeCycleRepository;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.ObjectUtil;
import com.transsion.framework.common.StringUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * ObjectLifecycle
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 15:42
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CfgObjectLifecycle extends BasePoEntity {

    @Resource
    private CfgObjectLifeCycleRepository cfgObjectLifeCycleRepository = PlmContextHolder.getBean(CfgObjectLifeCycleRepository.class);

    @Resource
    private TransactionTemplate transactionTemplate = PlmContextHolder.getBean(TransactionTemplate.class);

    @ApiModelProperty(value = "生命周期模板id")
    private String lcTemplBid;

    @ApiModelProperty(value = "生命周期模板名称")
    private String lcTemplName;

    @ApiModelProperty(value = "生命周期状态版本")
    private String lcTemplVersion;

    @ApiModelProperty(value = "生命周期初始状态")
    private String initState;

    @ApiModelProperty(value = "生命周期初始状态中文")
    private String initStateName;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "阶段生命周期模板id")
    private String lcPhaseTemplBid;

    @ApiModelProperty(value = "阶段生命周期状态版本")
    private String lcPhaseTemplVersion;

    @ApiModelProperty(value = "阶段生命周期初始状态")
    private String phaseInitState;

    @ApiModelProperty(value = "阶段生命周期模版说明")
    private String phaseDescription;

    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @ApiModelProperty(value = "是否自定义")
    private Boolean custom;

    /**
     * 构建查询 Lifecycle
     */
    public static CfgObjectLifecycle buildWithFind(String modelCode) {
        CfgObjectLifecycle cfgObjectLifecycle = new CfgObjectLifecycle();
        return cfgObjectLifecycle.setModelCode(modelCode);
    }

    /**
     * 生命周期模型行为 - 查询并且填充值
     */
    public ObjectModelLifeCycleVO findAndPopulate() {
        if (StringUtil.isEmpty(modelCode)) {
            throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(), "modelCode不能为空");
        }

        LinkedHashSet<String> modelCodeList = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        List<CfgObjectLifeCyclePo> cfgObjectLifeCyclePos = cfgObjectLifeCycleRepository.list(modelCodeList);
        Map<String,CfgObjectLifeCyclePo> cfgObjectLifeCycleMap = new HashMap<>();
        for(CfgObjectLifeCyclePo po:cfgObjectLifeCyclePos){
            cfgObjectLifeCycleMap.put(po.getModelCode(),po);
        }
        //从小到大遍历，有值就赋值并跳出
        for (String code : modelCodeList) {
            CfgObjectLifeCyclePo po = cfgObjectLifeCycleMap.get(code);
            if (ObjectUtil.isNotEmpty(po)) {
                //po不为空，赋值
                this.setLcTemplBid(po.getLcTemplBid())
                        .setLcTemplVersion(po.getLcTemplVersion())
                        .setInitState(po.getInitState())
                        .setDescription(po.getDescription())
                        .setLcPhaseTemplBid(po.getLcPhaseTemplBid())
                        .setLcPhaseTemplVersion(po.getLcPhaseTemplVersion())
                        .setPhaseInitState(po.getPhaseInitState())
                        .setPhaseDescription(po.getPhaseDescription())
                        .setCustom(ObjectUtil.equals(code, modelCode))
                        .setModelCode(po.getModelCode());
                this.setCreatedTime(po.getCreatedTime());
                this.setUpdatedTime(po.getUpdatedTime());
                this.setCreatedBy(po.getCreatedBy());
                this.setUpdatedBy(po.getUpdatedBy());

                //根据生命周期 初始状态code查名称
                String statusName = PlmContextHolder.getBean(ExtensionService.class).findLifeCycleStatusNameByCode(initState);
                this.setInitStateName(statusName);

                //根据lcTemplBid && lcTemplVersion查询出对应的生命周期模板名称
                String templateName = PlmContextHolder.getBean(ExtensionService.class).findLifeCycleTemplateName(po);
                this.setLcTemplName(templateName);

                break;
            }
        }

        if (StringUtils.isEmpty(lcTemplBid)) {
            return null;
        }

        return BeanUtil.copy(this, ObjectModelLifeCycleVO.class);
    }

    /**
     * 构建自定义 Lifecycle
     */
    public static CfgObjectLifecycle buildWithSave(ObjectLifeCycleSaveParam param) {
        return BeanUtil.copy(param, CfgObjectLifecycle.class);
    }

    /**
     * 生命周期模型行为 - 保存
     */
    public Boolean save() {
        return transactionTemplate.execute(transactionStatus -> {
            //先删掉对应的配置
            cfgObjectLifeCycleRepository.delete(modelCode);
            //自定义 -> 新增
            CfgObjectLifeCyclePo po = BeanUtil.copy(this, CfgObjectLifeCyclePo.class);
            po.setCreatedTime(LocalDateTime.now());
            po.setUpdatedTime(LocalDateTime.now());
            String userCode= SsoHelper.getJobNumber();
            po.setCreatedBy(userCode);
            po.setUpdatedBy(userCode);
            po.setBid(SnowflakeIdWorker.nextIdStr());
            po.setLcTemplVersion("1");
            po.setLcPhaseTemplVersion("1");
            cfgObjectLifeCycleRepository.add(po);
            return true;
        });
    }

    /**
     * 构建编辑 Lifecycle
     */
    public static CfgObjectLifecycle buildWithEdit(ObjectLifeCycleEditParam param) {
        return BeanUtil.copy(param, CfgObjectLifecycle.class);
    }

    /**
     * 生命周期模型行为 - 编辑
     */
    public Boolean update() {
        CfgObjectLifeCyclePo po = BeanUtil.copy(this, CfgObjectLifeCyclePo.class);
        po.setUpdatedTime(LocalDateTime.now());
        String userCode= SsoHelper.getJobNumber();
        po.setUpdatedBy(userCode);
        return cfgObjectLifeCycleRepository.update(po);
    }

}
