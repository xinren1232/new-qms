package com.transcend.plm.datadriven.apm.permission.service;

import com.transcend.plm.datadriven.apm.permission.pojo.ao.PermissionPlmOperationMapAO;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.PermissionPlmOperationMapVO;
import com.transcend.plm.datadriven.apm.permission.repository.entity.PermissionPlmOperationMap;

import java.util.List;
import java.util.Map;

/**
 * @author quan.cheng
 * @title IPermissionOperationService
 * @date 2024/5/7 15:17
 * @description 操作权限配置接口
 */
public interface IPermissionOperationService {

    /**
     * 新增扩展按钮
     *
     * @param permissionPlmOperationMapAO 扩展按钮实体
     * @param spaceAppBid 应用ID
     * @return {@link Boolean}
     * @date 2024/5/7 15:49
     * @author quan.cheng
     */
    Boolean saveOrUpdate(String spaceAppBid,PermissionPlmOperationMapAO permissionPlmOperationMapAO);

    /**
     * 批量删除扩展按钮
     *
     * @param permissionPlmOperationMapAOs 扩展按钮实体
     * @return {@link Boolean}
     * @date 2024/5/8 9:17
     * @author quan.cheng
     */
    Boolean delete(List<PermissionPlmOperationMapAO> permissionPlmOperationMapAOs);

    /**
     * 查询应用下的实体
     *
     * @param spaceAppBid 空间应用ID
     * @return {@link List< PermissionPlmOperationMapVO>}
     * @date 2024/5/8 9:45
     * @author quan.cheng
     */
    List<PermissionPlmOperationMapVO> queryOperationByAppId(String spaceAppBid);

    /**
     * 空间应用
     *
     * @date 2024/5/14 14:39
     * @author quan.cheng
     * @param spaceAppBid 空间应用id
     * @return {@link Map< String, String>}
     */
    List<Map<String,String>> queryAllOperationByAppId(String spaceAppBid);

    /**
     * 获取按钮映射
     * @param spaceAppBid 空间应用ID
     * @return MAP
     */
    Map<String, String> queryAllOperationByAppIdMapping(String spaceAppBid);

    /**
     * 根据空间应用ID获取按钮列表
     * @param spaceAppBid 空间应用ID
     * @return List<PermissionPlmOperationMap>
     */
    List<PermissionPlmOperationMap> listPermissionPlmOperationMaps(String spaceAppBid);


    /**
     * 复制空间应用下的按钮
     * @param oldSpaceAppBid 原空间应用ID
     * @param newSpaceAppBid 新空间应用ID
     * @return Boolean
     */
    Boolean copyBySpaceAppBid(String oldSpaceAppBid, String newSpaceAppBid);
}
