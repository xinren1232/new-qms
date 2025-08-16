package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.common.util.StringUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.permission.enums.OperatorEnum;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.PermissionPlmOperationMapAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.PermissionPlmOperationMapVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmOperationMap;
import com.transcend.plm.datadriven.apm.permission.repository.service.PermissionPlmOperationMapService;
import com.transcend.plm.datadriven.apm.permission.service.IPermissionOperationService;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author quan.cheng
 * @title PermissionOperationService
 * @date 2024/5/7 15:21
 * @description 操作权限配置实现类
 */
@Service
@Slf4j
public class PermissionOperationService implements IPermissionOperationService {

    @Resource
    PermissionPlmOperationMapService permissionPlmOperationMapService;

    @Override
    public Boolean saveOrUpdate(String spaceAppBid, PermissionPlmOperationMapAO permissionPlmOperationMapAO) {
        List<PermissionPlmOperationMap> list = listPermissionPlmOperationMaps(spaceAppBid);
        //判断bid是否为空
        String bid = permissionPlmOperationMapAO.getBid();
        if (StringUtils.isNotBlank(bid)) {
            // 走编辑接口
            // 1.1查询出该应用下的扩展是否已经绑定 排除当前值
            List<PermissionPlmOperationMap> collect = list.stream().filter(t ->
                    !permissionPlmOperationMapAO.getBid().equals(t.getBid()) &&
                            t.getAccessOperationKey().equals(permissionPlmOperationMapAO.getAccessOperationKey())
            ).collect(Collectors.toList());
            // 1.2如果已经绑定则提示前端 该应用已经绑定了扩展按钮
            if (!CollectionUtils.isEmpty(collect)) {
                throw new BusinessException("该应用已经绑定了扩展按钮，请重新选择！");
            }
            // 1.3如果未绑定则直接编辑
            PermissionPlmOperationMap permissionPlmOperationMap = new PermissionPlmOperationMap();
            BeanUtils.copyProperties(permissionPlmOperationMapAO, permissionPlmOperationMap);
            permissionPlmOperationMap.setUpdatedBy(SsoHelper.getJobNumber());
            permissionPlmOperationMapService.saveOrUpdate(permissionPlmOperationMap);
        } else {
            // 走新增接口
            // 1.1查询出该应用下的扩展是否已经绑定
            List<PermissionPlmOperationMap> collect = list.stream().filter(t -> t.getAccessOperationKey().equals(permissionPlmOperationMapAO.getAccessOperationKey())).collect(Collectors.toList());
            // 1.2如果已经绑定则提示前端 该应用已经绑定了扩展按钮
            if (!CollectionUtils.isEmpty(collect)) {
                throw new BusinessException("该应用已经绑定了扩展按钮，请重新选择！");
            }
            // 1.3如果未绑定则直接新增
            PermissionPlmOperationMap permissionPlmOperationMap = new PermissionPlmOperationMap();
            BeanUtils.copyProperties(permissionPlmOperationMapAO, permissionPlmOperationMap);
            permissionPlmOperationMap.setBid(SnowflakeIdWorker.nextIdStr());
            permissionPlmOperationMap.setAppBid(spaceAppBid);
            permissionPlmOperationMap.setDeleteFlag(false);
            permissionPlmOperationMap.setCreatedBy(SsoHelper.getJobNumber());
            permissionPlmOperationMap.setUpdatedBy(SsoHelper.getJobNumber());
            permissionPlmOperationMap.setCreatedTime(new Date());
            permissionPlmOperationMapService.saveOrUpdate(permissionPlmOperationMap);
        }
        return null;
    }

    @Override
    public Boolean delete(List<PermissionPlmOperationMapAO> permissionPlmOperationMapAOs) {
        // 根据应用ID逻辑删除数据
        return permissionPlmOperationMapService.removeBatchByIds(permissionPlmOperationMapAOs.stream().map(PermissionPlmOperationMapAO::getId).collect(Collectors.toList()));
    }


    @Override
    public List<PermissionPlmOperationMapVO> queryOperationByAppId(String spaceAppBid) {
        // 设置缓存时间

        if (StringUtils.isEmpty(spaceAppBid)) {
            log.error("应用ID为空");
            return Collections.emptyList();
        }
        List<PermissionPlmOperationMap> list = listPermissionPlmOperationMaps(spaceAppBid);
        //根据应用BID查询应用下的按钮权限
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(t -> {
            PermissionPlmOperationMapVO permissionPlmOperationMapVO = new PermissionPlmOperationMapVO();
            BeanUtils.copyProperties(t, permissionPlmOperationMapVO);
            return permissionPlmOperationMapVO;
        }).collect(Collectors.toList());
    }


    @Override
    public List<Map<String,String>> queryAllOperationByAppId(String spaceAppBid) {
        List<Map<String,String>> maps = new ArrayList<>();
        OperatorEnum[] values = OperatorEnum.values();
        for (OperatorEnum value : values) {
            Map<String, String> operatorMap = new HashMap<>(16);
            operatorMap.put("key", value.getCode());
            operatorMap.put("value", value.getDesc());
            maps.add(operatorMap);
        }
        // 查询扩展按钮
        List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS = queryOperationByAppId(spaceAppBid);
        if (CollectionUtils.isEmpty(permissionPlmOperationMapVOS)) {
            log.debug("没有自定义按钮数据");
            return maps;
        }
        //不为空则组装按钮返回
        for (PermissionPlmOperationMapVO operationMapVO : permissionPlmOperationMapVOS) {
            Map<String, String> operatorMap = new HashMap<>(16);
            operatorMap.put("key", operationMapVO.getOperationCode());
            operatorMap.put("value", operationMapVO.getOperationName());
            maps.add(operatorMap);
        }
        return maps;
    }

    @Override
    public Map<String, String> queryAllOperationByAppIdMapping(String spaceAppBid) {
        Map<String,String> baseAttrMap = new HashMap<>(16);
        baseAttrMap.put(OperatorEnum.DELETE.getCode(),"operationDelete");
        baseAttrMap.put(OperatorEnum.EDIT.getCode(),"operationEdit");
        baseAttrMap.put(OperatorEnum.DETAIL.getCode(),"operationDetail");
        baseAttrMap.put(OperatorEnum.REVISE.getCode(),"operationRevise");
        baseAttrMap.put(OperatorEnum.PROMOTE.getCode(),"operationPromote");
        baseAttrMap.put(OperatorEnum.MOVE.getCode(),"operationMove");
        if (!StringUtil.isNotBlank(spaceAppBid)){
            log.debug("没有自定义按钮数据");
            return baseAttrMap;
        }
        List<PermissionPlmOperationMapVO> permissionPlmOperationMapVOS = queryOperationByAppId(spaceAppBid);
        if (CollectionUtils.isEmpty(permissionPlmOperationMapVOS)) {
            log.debug("没有自定义按钮数据");
            return baseAttrMap;
        }
        //不为空则组装按钮返回
        for (PermissionPlmOperationMapVO operationMapVO : permissionPlmOperationMapVOS) {
            baseAttrMap.put(operationMapVO.getOperationCode(),operationMapVO.getAccessOperationKey());
        }
        return baseAttrMap;
    }

    /**
     * 查询空间应用下的扩展按钮
     *
     * @param spaceAppBid 应用ID
     * @return {@link List<PermissionPlmOperationMap>}
     * @date 2024/5/8 10:18
     * @author quan.cheng
     */
    @Override
    public List<PermissionPlmOperationMap> listPermissionPlmOperationMaps(String spaceAppBid) {
        LambdaQueryWrapper<PermissionPlmOperationMap> permissionPlmOperationMapLambdaQueryWrapper = new LambdaQueryWrapper<>();
        permissionPlmOperationMapLambdaQueryWrapper.eq(PermissionPlmOperationMap::getAppBid, spaceAppBid)
                .eq(PermissionPlmOperationMap::getDeleteFlag, 0);
        List<PermissionPlmOperationMap> list = permissionPlmOperationMapService.list(permissionPlmOperationMapLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * 复制空间应用下的按钮
     *
     * @param oldSpaceAppBid 原空间应用ID
     * @param newSpaceAppBid 新空间应用ID
     * @return Boolean
     */
    @Override
    public Boolean copyBySpaceAppBid(String oldSpaceAppBid, String newSpaceAppBid) {
        if (StringUtils.isEmpty(oldSpaceAppBid) || StringUtils.isEmpty(newSpaceAppBid)) {
            log.warn("Invalid space app BIDs provided for copy operation");
            return Boolean.FALSE;
        }

        List<PermissionPlmOperationMap> permissionPlmOperationMaps = listPermissionPlmOperationMaps(oldSpaceAppBid);
        if (CollectionUtils.isEmpty(permissionPlmOperationMaps)) {
            log.info("No permission mappings found for space app BID: {}", oldSpaceAppBid);
            return Boolean.FALSE;
        }

        String currentUser = SsoHelper.getJobNumber();
        Date currentTime = new Date();

        List<PermissionPlmOperationMap> results = permissionPlmOperationMaps.stream()
                .map(original -> {
                    PermissionPlmOperationMap copy = BeanUtil.copyProperties(original, PermissionPlmOperationMap.class);
                    copy.setId(null);
                    copy.setBid(SnowflakeIdWorker.nextIdStr());
                    copy.setAppBid(newSpaceAppBid);
                    copy.setCreatedBy(currentUser);
                    copy.setUpdatedBy(currentUser);
                    copy.setCreatedTime(currentTime);
                    return copy;
                })
                .collect(Collectors.toList());

        try {
            permissionPlmOperationMapService.saveBatch(results);
            log.info("Successfully copied {} permission mappings from {} to {}",
                    results.size(), oldSpaceAppBid, newSpaceAppBid);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("Failed to save copied permission mappings: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }
}
