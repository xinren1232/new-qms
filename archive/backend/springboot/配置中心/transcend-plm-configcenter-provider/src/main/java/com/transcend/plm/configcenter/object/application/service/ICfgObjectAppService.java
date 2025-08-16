package com.transcend.plm.configcenter.object.application.service;

import com.transcend.plm.configcenter.api.model.object.dto.*;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTreeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;

import java.util.List;
import java.util.Map;

/**
 * IObjectModelAppService
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 10:27
 */
public interface ICfgObjectAppService {

    List<CfgObjectTreeVo> treeAndLockInfo();
    List<CfgObjectTreeVo> tree();

    CfgObjectVo add(ObjectAddParam objectAddParam);

    Boolean deleteWithChildrenAndInfo(String bid);

    Boolean updatePosition(List<ObjectPositionParam> objectPositionParamList);

    CfgObjectVo getObjectAndAll(String bid);

    Boolean checkout(String bid);

    Boolean staging(StagingParam stagingParam);

    CfgObjectVo readDraft(String bid);

    Boolean undoCheckout(String bid);

    Boolean checkin(CheckinParam checkinParam);

    CfgObjectVo addObjectAndAttr(ObjectAndAttrAddParam objectAndAttrAddParam);

    CfgObjectVo editObjectAndAttr(CheckinParam checkinParam);

    /**
     * 上面为基础接口
     * ==================优--雅--的--分--割--线==================
     * 下面为扩展接口
     */

    /**
     * 查询所有对象列表
     * @param: @param  * @param
     * @return: java.util.List<com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo>
     * @version: 1.0
     * @date: 2024/8/5/005
     * @author: yanbing.ao
     */
    List<CfgObjectVo> list();

    List<CfgObjectVo> listChildrenByModelCode(String modelCode);

    List<CfgObjectVo> listByModelCodes(List<String> modelCodeList);

    List<CfgObjectVo> listByNames(List<String> nameList);

    List<CfgObjectVo> listLikeName(String name);

    CfgObjectVo getObjectAndAttribute(String bid);

    CfgObjectVo get(String bid);
    CfgObjectVo getByModelCode(String modelCode);

    List<CfgObjectAttributeVo> listAttrsByModelCode(String modelCode);

    Map<String, List<CfgObjectAttributeVo>> listAttributesByModelCodes(List<String> modelCodeList);

    /**
     * 上面入参为modelCode的接口
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */

    List<String> findParentBidListByBid(String bid);

    List<CfgObjectVo> findChildrenListByBid(String bid);

    CfgObjectAttributeVo findAttrByBid(AttrFindParam attrFindParam);

    List<CfgObjectAttributeVo> findAttrsByBid(String bid);

    List<CfgObjectAttributeVo> findChildrenAttrsByObjectBid(String objBid);

    CfgObjectVo getOneJustAttrByBid(String bid);

    List<CfgObjectVo> listByBids(List<String> bidList);

    Map<String, List<CfgObjectAttributeVo>> findAttrsByObjectBidList(List<String> objectBidList);

    List<CfgObjectVo> listByBaseModel(String rootObject);


    CfgObjectVo saveOrUpdate(ObjectAddParam objectAddParam);

    /**
     * objectModel 历史表查询
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */
    CfgObjectVo getOneHistoryByBidAndVersion(String bid, Integer version);

    CfgObjectVo getObjectAndAttributeOrDraft(String bid);

    CfgObjectVo getObjectAndAttributeByModelCode(String modelCode);

    List<CfgObjectVo> listAllByModelCode(String modelCode);

//    CfgObjectVo getObjectAndAttributeByBaseModel(String baseModel);
}
