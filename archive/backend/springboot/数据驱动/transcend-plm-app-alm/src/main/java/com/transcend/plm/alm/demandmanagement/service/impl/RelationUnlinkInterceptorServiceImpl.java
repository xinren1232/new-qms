package com.transcend.plm.alm.demandmanagement.service.impl;

import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.service.RelationUnlinkInterceptorService;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关系解除拦截服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/5 18:35
 */
@Log4j2
@RefreshScope
@Service
public class RelationUnlinkInterceptorServiceImpl implements RelationUnlinkInterceptorService {

    private final ObjectModelStandardI<MObject> objectModelCrudI;
    private final ApmSpaceAppService apmSpaceAppService;
    private final Map<String, String[]> lifeCycleCodeMap;
    private final Map<String, String> configs;

    public RelationUnlinkInterceptorServiceImpl(ObjectModelStandardI<MObject> objectModelCrudI,
                                                ApmSpaceAppService apmSpaceAppService,
                                                DemandManagementProperties properties) {
        this.objectModelCrudI = objectModelCrudI;
        this.apmSpaceAppService = apmSpaceAppService;
        this.lifeCycleCodeMap = properties.getLiefCycleCode();
        this.configs = properties.getRelationUnlinkInterceptor().stream()
                .collect(Collectors.toMap(Config::getModelCode, Config::getStatus, (v1, v2) -> v1));
    }

    @Override
    public boolean isSupport(String modelCode) {
        return configs.containsKey(modelCode);
    }

    @Override
    public boolean isIntercept(String modelCode, String sourceSpaceAppBid,
                               String sourceBid, List<String> relationBidList) {
        //1、是否剩余一条检测
        QueryWrapper wrapper = new QueryWrapper().eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid)
                .and().notIn(RelationEnum.BID.getColumn(), relationBidList);
        int count = objectModelCrudI.count(modelCode, QueryWrapper.buildSqlQo(wrapper));
        if (count > 0) {
            return false;
        }

        //2、源数据状态大于某个状态
        return statusIntercept(modelCode, sourceSpaceAppBid, sourceBid);
    }


    /**
     * 判断是否需要进行状态拦截
     *
     * @param targetModelCode   目标模型编码
     * @param sourceSpaceAppBid 源数据所属空间应用
     * @param sourceBid         源数据Bid
     * @return 是否需要拦截
     */
    private boolean statusIntercept(String targetModelCode, String sourceSpaceAppBid, String sourceBid) {
        ApmSpaceApp spaceApp = apmSpaceAppService.getByBid(sourceSpaceAppBid);
        if (spaceApp == null) {
            return false;
        }

        String[] interceptStatusCodeArray = getInterceptStatusCodeArray(spaceApp.getModelCode(), targetModelCode);
        if (ArrayUtils.isEmpty(interceptStatusCodeArray)) {
            return false;
        }

        assert interceptStatusCodeArray != null;
        QueryWrapper wrapper = new QueryWrapper().eq(BaseDataEnum.BID.getColumn(), sourceBid)
                .and().in(ObjectEnum.LIFE_CYCLE_CODE.getCode(), Arrays.asList(interceptStatusCodeArray));
        return objectModelCrudI.count(spaceApp.getModelCode(), QueryWrapper.buildSqlQo(wrapper)) > 0;
    }


    /**
     * 获取匹配的状态集合
     *
     * @param sourceModelCode 源模型编码
     * @param targetModelCode 目标模型编码
     * @return 需要匹配的状态集合
     */
    @Nullable
    private String[] getInterceptStatusCodeArray(String sourceModelCode, String targetModelCode) {
        String[] lifeCycleCodeArray = this.lifeCycleCodeMap.get(sourceModelCode);
        if (lifeCycleCodeArray == null) {
            log.warn("getInterceptStatusCodeArray lifeCycleCodeArray is null, sourceModelCode:{}", sourceModelCode);
            return null;
        }
        String statusCode = this.configs.get(targetModelCode);

        int indexOf = ArrayUtils.indexOf(lifeCycleCodeArray, statusCode);
        if (indexOf == -1) {
            log.warn("getInterceptStatusCodeArray  does not exist, targetModelCode:{},lifeCycleCode:{}",
                    targetModelCode, statusCode);
            return null;
        }
        return ArrayUtils.subarray(lifeCycleCodeArray, indexOf, lifeCycleCodeArray.length);
    }

}
