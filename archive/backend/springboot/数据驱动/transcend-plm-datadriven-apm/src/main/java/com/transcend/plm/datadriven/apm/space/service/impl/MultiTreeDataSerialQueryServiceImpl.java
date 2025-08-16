package com.transcend.plm.datadriven.apm.space.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiTreeConfigVo;
import com.transcend.plm.datadriven.apm.space.service.MultiTreeDataQueryExecutor;
import com.transcend.plm.datadriven.apm.space.service.MultiTreeDataQueryService;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多对象树串行查询实现
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/7 14:56
 */
@Service
@AllArgsConstructor
public class MultiTreeDataSerialQueryServiceImpl implements MultiTreeDataQueryService {
    private MultiTreeDataQueryExecutor executor;

    @Override
    public Data query(Params params) {
        Data data = new Data(params.getMultiAppTreeConfig());
        //串行递归查询数据
        recursiveDataQuery(data, params.getMultiAppTreeConfig(), params.getEmpNo(), params.getSpaceBid(),
                params.getCheckPermission(), params.getQueryWrapperMap(), Boolean.TRUE.equals(params.getStrictMode()), null);
        return data;
    }


    @Override
    public boolean isSupport(Params entity) {
        return true;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }


    //region 私有方法

    /**
     * 递归查询方法
     *
     * @param data                   数据收集对象
     * @param config                 配置信息
     * @param empNo                  用户编码
     * @param spaceBid               空间Bid
     * @param defaultCheckPermission 默认权限
     * @param wrapperMap             查询参数
     * @param strictMode             是否严格模式
     * @param bidList                父级Bid集合
     */
    private void recursiveDataQuery(Data data, MultiTreeConfigVo config, String empNo, String spaceBid,
                                    Boolean defaultCheckPermission, Map<String, List<QueryWrapper>> wrapperMap,
                                    boolean strictMode, List<String> bidList) {
        //1、查询实例数据
        MultiTreeDataQueryExecutor.Params queryParams = MultiTreeDataQueryExecutor.Params
                .ofInstance(config, empNo, spaceBid, defaultCheckPermission, wrapperMap.get(config.getSourceModelCode()));
        List<MObject> instanceList = executor.execute(queryParams);
        data.putData(config.getSourceModelCode(), instanceList);

        //2、补偿上层要求的实例数据
        if (!strictMode) {
            compensationInstanceData(data, config, empNo, spaceBid, defaultCheckPermission, instanceList, bidList);
        }

        //3、查询关系数据
        instanceList = data.getData(config.getSourceModelCode());
        List<String> childrenBidList = null;
        if (CollUtil.isNotEmpty(instanceList) && StringUtils.isNotBlank(config.getRelationModelCode())) {
            //通过级联获取关系数据，防止拉取全部的关系数据
            List<String> sourceBidList = instanceList.stream().map(MBaseData::getBid).collect(Collectors.toList());
            QueryWrapper wrapper = new QueryWrapper().in(RelationObjectEnum.SOURCE_BID.getCode(), sourceBidList);
            queryParams = MultiTreeDataQueryExecutor.Params.ofRelation(config, QueryWrapper.buildSqlQo(wrapper));
            List<MObject> relationList = executor.execute(queryParams);
            data.putData(config.getRelationModelCode(), relationList);

            childrenBidList = relationList.stream().map(TranscendRelationWrapper::new)
                    .map(TranscendRelationWrapper::getTargetBid).collect(Collectors.toList());
        }

        //4、递归查询子层
        if (config.getTargetModelCode() != null) {
            recursiveDataQuery(data, config.getTargetModelCode(), empNo, spaceBid, defaultCheckPermission, wrapperMap, strictMode, childrenBidList);
        }

        //5、补偿下层要求的实例数据
        if (!strictMode && StringUtils.isNotBlank(config.getRelationModelCode())) {
            List<String> parentBidList = compensationRelationData(data, config);
            compensationInstanceData(data, config, empNo, spaceBid, defaultCheckPermission, null, parentBidList);
        }
    }

    /**
     * 补偿关系数据
     *
     * @param data   结果数据
     * @param config 对象配置
     * @return 需要补偿的上层数据bid
     */
    private List<String> compensationRelationData(Data data, MultiTreeConfigVo config) {

        //获取没有的关系数据Bid
        List<MObject> childrenList = Optional.ofNullable(data.getData(config.getTargetModelCode().getSourceModelCode()))
                .orElseGet(Collections::emptyList);
        Set<String> relationTargetBidList = Optional.ofNullable(data.getData(config.getRelationModelCode()))
                .orElseGet(Collections::emptyList).stream().map(TranscendRelationWrapper::new)
                .map(TranscendRelationWrapper::getTargetBid).collect(Collectors.toSet());
        List<String> targetBidList = childrenList.stream().map(MBaseData::getBid)
                .filter(bid -> !relationTargetBidList.contains(bid)).collect(Collectors.toList());

        //查询关系数据
        QueryWrapper wrapper = new QueryWrapper().in(RelationObjectEnum.TARGET_BID.getCode(), targetBidList);
        MultiTreeDataQueryExecutor.Params queryParams = MultiTreeDataQueryExecutor.Params
                .ofRelation(config, QueryWrapper.buildSqlQo(wrapper));
        List<MObject> relationList = executor.execute(queryParams);

        //写入数据集合
        if (CollUtil.isNotEmpty(relationList)) {
            data.putData(config.getRelationModelCode(), relationList);
            //返回缺少的关系数据
            return relationList.stream().map(TranscendRelationWrapper::new)
                    .map(TranscendRelationWrapper::getSourceBid).collect(Collectors.toList());
        }
        return null;

    }

    /**
     * 补偿实例数据
     *
     * @param data                   结果数据
     * @param config                 对象配置
     * @param empNo                  用户编码
     * @param spaceBid               空间Bid
     * @param defaultCheckPermission 默认权限
     * @param instanceList           实例数据 需要补充的数据回自动写入该集合
     * @param bidList                Bid集合
     */
    private void compensationInstanceData(Data data, MultiTreeConfigVo config,
                                          String empNo, String spaceBid, Boolean defaultCheckPermission,
                                          List<MObject> instanceList, List<String> bidList) {
        if (CollUtil.isEmpty(bidList)) {
            return;
        }

        //去除已有数据
        if (CollUtil.isNotEmpty(instanceList)) {
            instanceList.forEach(instance -> bidList.remove(instance.getBid()));
        }

        if (!bidList.isEmpty()) {
            //补偿上层需要的实例数据
            QueryWrapper wrapper = new QueryWrapper().in(BaseDataEnum.BID.getCode(), bidList);
            MultiTreeDataQueryExecutor.Params queryParams = MultiTreeDataQueryExecutor.Params
                    .ofInstance(config, empNo, spaceBid, defaultCheckPermission, QueryWrapper.buildSqlQo(wrapper));
            data.putData(config.getSourceModelCode(), executor.execute(queryParams));
        }
    }

    //endregion
}
