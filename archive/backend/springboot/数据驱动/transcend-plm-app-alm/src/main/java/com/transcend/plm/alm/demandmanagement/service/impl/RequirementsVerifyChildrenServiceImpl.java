package com.transcend.plm.alm.demandmanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.service.RequirementsVerifyChildrenService;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.exception.AssertException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 需求校验子层实现
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/19 16:19
 */
@Log4j2
@Service
public class RequirementsVerifyChildrenServiceImpl implements RequirementsVerifyChildrenService {
    private List<Config> configs;

    @Resource
    private ObjectModelStandardI<MObject> objectModelCrudI;
    @Resource
    private DemandManagementProperties properties;

    @SuppressWarnings("all")
    @Value("${transcend.alm.verifyChildren:{}}")
    public void setConfigs(String configsJson) {
        try (JSONValidator validator = JSONValidator.from(configsJson)) {
            if (validator.validate()) {
                configs = JSON.parseArray(configsJson, Config.class);
                configs.forEach(config -> {
                    String[] statusArray = properties.getLiefCycleCode().get(config.getModelCode());
                    config.setOrder(ArrayUtils.indexOf(statusArray, config.getStatus()));
                });

                return;
            }
        } catch (Exception e) {
            log.error("setConfigs configsJson is not valid json", e);
        }
        log.error("setConfigs configsJson is not valid json");
    }


    @Override
    public boolean isSupport(String modelCode) {
        if (StringUtils.isBlank(modelCode)) {
            return false;
        }
        return configs.stream().anyMatch(config -> modelCode.equals(config.getModelCode()));
    }

    @Override
    public void verify(String modelCode, String bid, String targetStatus) {
        Assert.notBlank(bid, "bid is blank");

        Config config = getConfig(modelCode, targetStatus);
        if (config == null) {
            return;
        }

        //跳过回退
        if (config.isSkipFallback()) {
            MObject updateData = objectModelCrudI.getByBid(modelCode, bid);
            String lifeCycleCode = updateData.getLifeCycleCode();
            int oldStatusIndex = getStatusIndex(modelCode, lifeCycleCode);
            int newStatusIndex = getStatusIndex(modelCode, targetStatus);
            //如果新状态再旧状态之前则跳过，为节省性能状态一致也跳过
            if (newStatusIndex <= oldStatusIndex) {
                return;
            }
        }

        boolean failVerify;
        QueryWrapper wrapper = new QueryWrapper().in(RelationEnum.SOURCE_BID.getColumn(), bid);

        //有子层状态要求则需要所有数据都满足这个状态
        if (StringUtils.isNotBlank(config.getChildrenStatus()) && StringUtils.isNotBlank(config.getChildrenModelCode())) {
            failVerify = childrenStatusFailVerify(config, wrapper);
        } else {
            //没有要求子层状态则有一个子层就满足
            int count = objectModelCrudI.count(config.getRelationModelCode(), QueryWrapper.buildSqlQo(wrapper));
            failVerify = count <= 0;
        }

        //未通过校验抛出异常
        if (failVerify) {
            String errorMessage = Optional.ofNullable(config.getErrorMessage())
                    .orElse("状态无法变更，至少关联一个子需求，由子需求的状态自动变更");
            throw new AssertException(errorMessage);
        }

    }

    /**
     * 子层状态未通过校验
     *
     * @param config  配置信息
     * @param wrapper 查询条件
     * @return true/false 未通过校验返回true
     */
    private boolean childrenStatusFailVerify(Config config, QueryWrapper wrapper) {
        //查询所有子层Bid
        List<String> childrenBidList = objectModelCrudI
                .list(config.getRelationModelCode(), QueryWrapper.buildSqlQo(wrapper))
                .stream().map(TranscendRelationWrapper::new)
                .map(TranscendRelationWrapper::getTargetBid)
                .collect(Collectors.toList());

        //查询数据
        List<MObject> list = objectModelCrudI.listByBids(childrenBidList, config.getChildrenModelCode());
        if (list == null || list.isEmpty()) {
            return true;
        }

        //校验是否有不满足的状态
        String[] statusArray = properties.getLiefCycleCode().get(config.getChildrenModelCode());
        int requireStatus = ArrayUtils.indexOf(statusArray, config.getChildrenStatus());
        long count = list.stream().map(TranscendObjectWrapper::new).map(TranscendObjectWrapper::getLifeCycleCode)
                .filter(status -> ArrayUtils.indexOf(statusArray, status) < requireStatus)
                .count();
        return !(count <= 0 || config.isAnyMatch() && count < list.size());
    }

    /**
     * 根据目标状态获取配置信息
     *
     * @param modelCode    模型编码
     * @param targetStatus 目标状态
     * @return 配置信息
     */
    @Nullable
    private Config getConfig(String modelCode, String targetStatus) {
        int targetIndex = getStatusIndex(modelCode, targetStatus);
        return configs.stream().filter(config -> modelCode.equals(config.getModelCode()))
                .filter(config -> config.getOrder() <= targetIndex)
                .max(Comparator.comparingInt(Config::getOrder))
                .orElse(null);
    }

    /**
     * 获取状态的位置
     *
     * @param modelCode    模型编码
     * @param targetStatus 目标状态
     * @return 状态的位置
     */
    private int getStatusIndex(String modelCode, String targetStatus) {
        String[] statusArray = properties.getLiefCycleCode().get(modelCode);
        return ArrayUtils.indexOf(statusArray, targetStatus);
    }
}
