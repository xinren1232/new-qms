package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppAo;
import com.transcend.plm.datadriven.apm.flow.pojo.qo.ApmSpaceRoleQO;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppQueryDto;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceDto;
import com.transcend.plm.datadriven.apm.space.pojo.qo.ApmSpaceQo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppAccessVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppQueryVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmRoleVO;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public interface ApmSpaceApplicationService {
    /**
     * 新增空间
     *
     * @param apmSpaceDto apmSpaceDto
     * @return return
     */
    boolean saveApmSpace(ApmSpaceDto apmSpaceDto);

    /**
     * 逻辑删除空间
     *
     * @param bid bid
     * @return return
     */
    boolean logicDeleteApmSpace(String bid);

    /**
     * 修改空间
     *
     * @param apmSpaceDto apmSpaceDto
     * @return return
     */
    boolean updateApmSpace(ApmSpaceDto apmSpaceDto);

    /**
     * 方法描述
     *
     * @param spaceBid spaceBid
     * @return 返回值
     */
    boolean setDefaultSpace(String spaceBid);

    /**
     * 方法描述
     *
     * @return 返回值
     */
    List<ApmSpaceVo> listApmSpace();

    /**
     * 方法描述
     *
     * @param pageRequest pageRequest
     * @return 返回值
     */
    PagedResult<ApmSpaceVo> listApmSpacePage(BaseRequest<ApmSpaceQo> pageRequest);

    /**
     * 方法描述
     *
     * @param apmSpaceAppQueryDto apmSpaceAppQueryDto
     * @return 返回值
     */
    List<ApmSpaceAppQueryVo> queryApmSpaceAppInstance(ApmSpaceAppQueryDto apmSpaceAppQueryDto);

    /**
     * 方法描述
     *
     * @return 返回值
     */
    ApmSpaceVo getDefaultApmSpace();

    /**
     * 方法描述
     *
     * @param bids bids
     * @return 返回值
     */
    List<ApmSpaceAppVo> listAppByBids(List<String> bids);

    /**
     * 空间配置 - 查询角色&权限列表
     *
     * @param apmSpaceRoleQo 查询条件
     * @return 角色&权限列表
     */
    List<ApmRoleVO> listRoleAndPermission(ApmSpaceRoleQO apmSpaceRoleQo);

    /**
     * 空间配置 - 查询角色&权限树
     *
     * @param apmSpaceRoleQo 查询条件
     * @return 角色&权限列表
     */
    List<ApmRoleVO> listRoleAndPermissionTree(ApmSpaceRoleQO apmSpaceRoleQo);


    /**
     * 根据领域bid获取空间下的所有权限
     *
     * @param sphereBid 领域bid
     * @return List<ApmAccessVO> 权限列表
     */
    List<ApmSpaceAppAccessVo> getPermissionBySphereBid(String sphereBid);

    /**
     * 添加空间应用
     *
     * @param apmSpaceAppAo 空间应用
     * @return Boolean
     */
    ApmSpaceApp addApp(ApmSpaceAppAo apmSpaceAppAo);

    /**
     * 操作空间应用（修改、删除、启用、禁用）
     *
     * @param apmSpaceAppAo apmSpaceAppAo
     * @return return
     */
    Boolean operationApp(ApmSpaceAppAo apmSpaceAppAo);

    /**
     * 通过空间bid与空间bid，获取对象类型信息
     *
     * @param spaceAppBid 空间应用bid
     * @return return
     */
    String getModelCodeBySpaceAppBid(String spaceAppBid);

    /**
     * 通过空间应用bid，获取空间bid
     *
     * @param spaceAppBid 空间应用bid
     * @return return
     */
    String getSpaceBidBySpaceAppBid(String spaceAppBid);

    /**
     * 通过空间Bid，获取空间应用列表
     *
     * @param spaceBid 空间Bid
     * @return 空间应用列表
     */
    List<ApmSpaceAppVo> listApp(String spaceBid);

    /**
     * 方法描述
     *
     * @param spaceAppBid spaceAppBid
     * @return com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp
     * @description 根据空间应用BID获取信息
     * @author jinpeng.bai
     * @date 2023/10/13 14:54
     * @since version 1.0
     */
    ApmSpaceApp getSpaceAppByBid(String spaceAppBid);

    /**
     * 方法描述
     *
     * @param sphereBid sphereBid
     * @return 返回值
     */
    List<ApmRoleVO> listRoleAndPermissionBySphereBid(String sphereBid);

    /**
     * 查询 或 创建 实例数据 团队
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param bid         bid
     * @return List<ApmRoleVO>
     * @version: 1.0
     * @date: 2023/10/24 10:39
     * @author: bin.yin
     */
    List<ApmRoleVO> queryOrCreateTeam(String spaceBid, String spaceAppBid, String bid);

    /**
     * 更改是否为模板空间
     *
     * @param bid          空间bid
     * @param templateFlag 模板标识（1：是模板，0：不是模板）
     * @return Boolean
     */
    Boolean changeTemplateSpace(String bid, Boolean templateFlag);

    /**
     * 获取空间模板列表
     *
     * @return return
     */
    List<ApmSpaceVo> listSpaceTemplate();

    /**
     * 方法描述
     *
     * @param qo qo
     * @return 返回值
     */
    List<ApmSpaceVo> listApmSpace(ApmSpaceQo qo);

    /**
     * 方法描述
     *
     * @param pageRequest pageRequest
     * @return 返回值
     */
    PagedResult<ApmSpaceVo> pageApmSpace(BaseRequest<ApmSpaceQo> pageRequest);

    /**
     * 方法描述
     *
     * @param apmSpaceAppAoList apmSpaceAppAoList
     * @return 返回值
     */
    Boolean sortApp(List<ApmSpaceAppAo> apmSpaceAppAoList);

    /**
     * 根据应用bid获取对象modelCode
     *
     * @param spaceAppBid spaceAppBid
     * @return 返回值
     */
    String getModelCodeByAppBid(String spaceAppBid);

    /**
     * 拷贝单个空间应用数据
     *
     * @param copySpaceAppBid copySpaceAppBid
     * @param nowSpaceBid     nowSpaceBid
     * @return return
     */
    boolean copySpaceAppInfo(String copySpaceAppBid, String nowSpaceBid);

    /**
     * 方法描述
     *
     * @param spaceAppBids spaceAppBids
     * @return 返回值
     */
    Map<String, String> getSpaceBySpaceAppBids(List<String> spaceAppBids);

    /**
     * 根据foreignBid获取空间实例
     *
     * @param foreignBid foreignBid
     * @return return
     */
    ApmSpaceVo getApmSpaceByForeignBid(String foreignBid);

    /**
     * 查询角色&系统角色列表
     * @param apmSpaceRoleQo 查询条件
     * @return List<ApmRoleVO>
     */
    List<ApmRoleVO> listRoleAndSystemRole(ApmSpaceRoleQO apmSpaceRoleQo);
}
