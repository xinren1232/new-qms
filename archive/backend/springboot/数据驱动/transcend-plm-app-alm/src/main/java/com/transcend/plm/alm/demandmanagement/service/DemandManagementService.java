package com.transcend.plm.alm.demandmanagement.service;

import com.transcend.plm.alm.demandmanagement.entity.ao.*;
import com.transcend.plm.alm.demandmanagement.entity.vo.SelectVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;

import java.util.List;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description 需求管理服务
 * @date 2024/06/21 11:10
 **/
public interface DemandManagementService {

    /**
     * 验证需求是否重复
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param instanceBid instanceBid
     * @return {@link Boolean}
     */
    Boolean checkRepeat(String spaceBid, String spaceAppBid, String instanceBid);

    /**
     * 查询RR下领域组件 数据
     *
     * @param spaceBid 空间bid
     * @param rrBid    RR需求关联领域编码
     * @return List<MSpaceAppData>
     */
    List<MObject> queryDomainTree(String spaceBid, String rrBid);

    /**
     * 查询
     *
     * @param spaceBid    空间bid
     * @param type        类型 1:小模块 2:领域/模块/应用
     * @param rrBid       RR需求关联领域编码
     * @param selectedBid 选中bid
     * @return List<SelectVo>
     */
    List<SelectVo> queryDomainSelection(String spaceBid, Integer type, String rrBid, String selectedBid);

    /**
     * 获取需求领域
     *
     * @param rrBid RR需求关联领域编码
     * @return {@link List<String>}
     */
    List<String> getDomainBidListByRrBid(String rrBid,String domainModelCode);

    List<MObject> getDomainBidListByRrBids(List<String> rrBid,String domainModelCode);

    boolean checkDomainHaveModule(String rrBid);

    /**
     * RR需求 批量移除 领域/应用/模块
     *
     * @param spaceBid       空间bid
     * @param spaceAppBid    应用bid
     * @param removeDomainAo removeDomainAo
     * @return java.lang.Boolean
     * @version: 1.0
     * @date: 2024/6/24 13:57
     * @author: bin.yin
     **/
    Boolean batchRemoveDomain(String spaceBid, String spaceAppBid, RemoveDomainAo removeDomainAo);


    /**
     * 更新所属需求的生命周期为当前生命周期
     *
     * @param modelCode       modelCode
     * @param spaceAppBid     应用bid
     * @param instanceBid     实例bid
     * @param demandLifecycle 需求生命周期
     * @return Boolean
     */
    Boolean updateBelongDemandLifecycle(String modelCode, String spaceAppBid, String instanceBid, String demandLifecycle);

    /**
     * 新增IR需求
     *
     * @param param       参数
     * @param spaceBid    空间bid
     * @param spaceAppBid 空间应用bid
     * @return Boolean
     */
    Boolean addIR(String spaceBid, String spaceAppBid, IRAddAndRelateAo param);

    /**
     * 关联IR需求
     *
     * @param spaceBid    空间bid
     * @param spaceAppBid 空间应用bid
     * @param param       参数
     * @return Boolean
     */
    Boolean relateIR(String spaceBid, String spaceAppBid, IRAddAndRelateAo param);


    /**
     * 移除IR需求
     *
     * @param spaceBid    空间bid
     * @param spaceAppBid 空间应用bid
     * @param param       参数
     * @return Boolean
     */
    Boolean removeIR(String spaceBid, String spaceAppBid, IRRemAo param);

    /**
     * RR需求更新主导领域
     *
     * @param spaceBid           空间bid
     * @param spaceAppBid        应用bid
     * @param updateLeadDomainAo updateLeadDomainAo
     * @return java.lang.Boolean
     * @version: 1.0
     * @date: 2024/6/24 16:54
     * @author: bin.yin
     **/
    Boolean updateLeadDomain(String spaceBid, String spaceAppBid, UpdateLeadDomainAo updateLeadDomainAo);

    /**
     * RR需求更新关联领域责任人
     *
     * @param spaceBid            spaceBid
     * @param spaceAppBid         spaceAppBid
     * @param updateResponsibleAo updateResponsibleAo
     * @return java.lang.Boolean
     * @version: 1.0
     * @date: 2024/6/24 17:17
     * @author: bin.yin
     **/
    Boolean updateResponsiblePerson(String spaceBid, String spaceAppBid, UpdateResponsibleAo updateResponsibleAo);


    /**
     * 绑定选择的领域/模块/应用
     *
     * @param spaceBid          spaceBid
     * @param spaceAppBid       spaceAppBid
     * @param rrBid             rrBid
     * @param type              type
     * @param selectAo          selectAo
     * @param rrDomainRoleCode  rrDomainRoleCode
     * @param rrProductRoleCode rrProductRoleCode
     * @return java.util.List<com.alibaba.tesla.alm.domain.MObject>
     */
    List<MObject> selectDomain(String spaceBid, String spaceAppBid, String rrBid, Integer type, SelectAo selectAo, String rrDomainRoleCode, String rrProductRoleCode);

    /**
     * 绑定选择的领域/模块/应用,会删除原来的领域组件
     *
     * @param spaceBid          spaceBid
     * @param spaceAppBid       spaceAppBid
     * @param rrBid             rrBid
     * @param selectAo          selectAo
     * @param rrDomainRoleCode  rrDomainRoleCode
     * @param rrProductRoleCode rrProductRoleCode
     * @return java.util.List<com.alibaba.tesla.alm.domain.MObject>
     */
    List<MObject> selectAllDomain(String spaceBid, String spaceAppBid, String rrBid, SelectAo selectAo, String rrDomainRoleCode, String rrProductRoleCode);

    Boolean copyDomain(String spaceBid, String spaceAppBid, String rrBid, MObjectCopyAo sourceParam);

    /**
     * 查询需求IR/SR树
     * @param projectName 项目名
     * @return List<MObjectTree>
     */
    List<MObjectTree> searchDemandIrsrTree(String projectName);
}
