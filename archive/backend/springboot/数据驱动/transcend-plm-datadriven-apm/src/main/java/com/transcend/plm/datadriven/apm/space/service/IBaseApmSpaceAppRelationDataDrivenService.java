package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.GroupListResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.MObjectTree;
import com.transcend.plm.datadriven.api.model.RelationMObject;
import com.transcend.plm.datadriven.api.model.dto.MoveTreeNodeParam;
import com.transcend.plm.datadriven.api.model.relation.delete.StructureRelDel;
import com.transcend.plm.datadriven.api.model.relation.qo.QueryPathQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationParentQo;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationTreeQo;
import com.transcend.plm.datadriven.api.model.relation.vo.QueryPathVo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.MObjectCopyAo;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.RelationDelAndRemParamAo;
import com.transcend.plm.datadriven.apm.space.model.ApmRelationMultiTreeAddParam;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.model.SpaceAppRelationAddParam;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmMultiTreeDto;
import com.transsion.framework.dto.BaseRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author unknown
 */
public interface IBaseApmSpaceAppRelationDataDrivenService {
    /**
     * batchDelete
     *
     * @param spaceBid         spaceBid
     * @param delAndRemParamAo delAndRemParamAo
     * @return {@link Boolean}
     */
    Boolean batchDelete(String spaceBid, RelationDelAndRemParamAo delAndRemParamAo);

    /**
     * batchRemove
     *
     * @param spaceBid         spaceBid
     * @param delAndRemParamAo delAndRemParamAo
     * @return {@link Boolean}
     */
    Boolean batchRemove(String spaceBid, RelationDelAndRemParamAo delAndRemParamAo);

    /**
     * removeAllRelation
     *
     * @param spaceAppRelationAddParam spaceAppRelationAddParam
     * @return {@link Boolean}
     */
    Boolean removeAllRelation(SpaceAppRelationAddParam spaceAppRelationAddParam);

    /**
     * moveTreeNode
     *
     * @param spaceBid           spaceBid
     * @param spaceAppBid        spaceAppBid
     * @param targetSpaceAppBid  targetSpaceAppBid
     * @param sourceBid          sourceBid
     * @param moveTreeNodeParams moveTreeNodeParams
     * @return {@link Boolean}
     */
    Boolean moveTreeNode(String spaceBid, String spaceAppBid, String targetSpaceAppBid, String sourceBid, List<MoveTreeNodeParam> moveTreeNodeParams);

    /**
     * add
     *
     * @param spaceAppRelationAddParam spaceAppRelationAddParam
     * @return {@link Boolean}
     */
    Boolean add(SpaceAppRelationAddParam spaceAppRelationAddParam);

    /**
     * 多对象树由于逻辑复杂，直接删除原来源跟目标的关系，在新增前端选中的关系
     * 新增关系时，需要找出选中数据的根节点数据 绑定关系
     *
     * @param apmRelationMultiTreeAddParam apmRelationMultiTreeAddParam
     * @param relationSpaceAppBid          relationSpaceAppBid
     * @param spaceBid                     spaceBid
     * @return Boolean
     * @version: 1.0
     * @date: 2023/10/24 20:32
     * @author: bin.yin
     */
    Boolean multiBatchSelect(String spaceBid, String relationSpaceAppBid, ApmRelationMultiTreeAddParam apmRelationMultiTreeAddParam);

    /**
     * addExpand
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param source      source
     * @param addExpandAo addExpandAo
     * @return {@link Object}
     */
    Object addExpand(String spaceBid, String spaceAppBid, String source, AddExpandAo addExpandAo);

    /**
     * listMultiTree
     *
     * @param apmMultiTreeDto apmMultiTreeDto
     * @param filterRichText  filterRichText
     * @return {@link List<MObjectTree>}
     */
    List<MObjectTree> listMultiTree(ApmMultiTreeDto apmMultiTreeDto, boolean filterRichText);

    /**
     * listMultiTreeGroupBy
     *
     * @param apmMultiTreeDto apmMultiTreeDto
     * @return {@link List<MObjectTree>}
     */
    List<MObjectTree> listMultiTreeGroupBy(ApmMultiTreeDto apmMultiTreeDto);

    /**
     * listMultiTreeGroupKb
     *
     * @param apmMultiTreeDto apmMultiTreeDto
     * @param filterRichText  filterRichText
     * @return {@link GroupListResult<MSpaceAppData>}
     */
    GroupListResult<MSpaceAppData> listMultiTreeGroupKb(ApmMultiTreeDto apmMultiTreeDto, boolean filterRichText);

    /**
     * groupList
     *
     * @param groupProperty   groupProperty
     * @param relationMObject relationMObject
     * @return {@link GroupListResult<MSpaceAppData>}
     */
    GroupListResult<MSpaceAppData> groupList(String groupProperty, RelationMObject relationMObject);


    /**
     * 对象关系复制
     *
     * @param mObjectCopyAo mObjectCopyAo
     * @return {@link Boolean}
     */
    boolean copyMObject(MObjectCopyAo mObjectCopyAo);

    /**
     * exportExcel
     *
     * @param spaceAppBid     spaceAppBid
     * @param type            type
     * @param relationMObject relationMObject
     * @param response        response
     */
    void exportExcel(String spaceAppBid, String type, RelationMObject relationMObject, HttpServletResponse response);

    /**
     * listRelation
     *
     * @param spaceBid        spaceBid
     * @param spaceAppBid     spaceAppBid
     * @param relationMObject relationMObject
     * @return {@link List<MObject>}
     */
    List<MObject> listRelation(String spaceBid, String spaceAppBid, RelationMObject relationMObject);

    /**
     * listRelationPage
     *
     * @param spaceBid        spaceBid
     * @param spaceAppBid     spaceAppBid
     * @param relationMObject relationMObject
     * @param filterRichText  filterRichText
     * @return {@link PagedResult<MObject>}
     */
    PagedResult<MObject> listRelationPage(String spaceBid, String spaceAppBid, BaseRequest<RelationMObject> relationMObject, boolean filterRichText);

    /**
     * tree
     *
     * @param spaceBid        spaceBid
     * @param spaceAppBid     spaceAppBid
     * @param relationMObject relationMObject
     * @param filterRichText  filterRichText
     * @return {@link List<MObjectTree>}
     */
    List<MObjectTree> tree(String spaceBid, String spaceAppBid, RelationMObject relationMObject, boolean filterRichText);

    /**
     * listRelationTree
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param qo          qo
     * @return {@link List<MObjectTree>}
     */
    List<MObjectTree> listRelationTree(String spaceBid, String spaceAppBid, RelationTreeQo qo);

    /**
     * listRelParent
     *
     * @param spaceBid    spaceBid
     * @param spaceAppBid spaceAppBid
     * @param qo          qo
     * @return {@link List<MObject>}
     */
    List<MObject> listRelParent(String spaceBid, String spaceAppBid, RelationParentQo qo);

    /**
     * structureBatchRemoveRel
     *
     * @param spaceBid        spaceBid
     * @param spaceAppBid     spaceAppBid
     * @param structureRelDel structureRelDel
     * @return {@link boolean}
     */
    boolean structureBatchRemoveRel(String spaceBid, String spaceAppBid, StructureRelDel structureRelDel);

    /**
     * 查询cad文档路径
     *
     * @param spaceBid    空间bid
     * @param spaceAppBid 应用bid
     * @param queryPathQo queryPathQo
     * @return java.util.List<com.transcend.plm.datadriven.api.model.relation.vo.QueryPathVo>
     * @version: 1.0
     * @date: 2024/8/26 17:30
     * @author: bin.yin
     **/
    List<QueryPathVo> queryCadPath(String spaceBid, String spaceAppBid, QueryPathQo queryPathQo);

}
