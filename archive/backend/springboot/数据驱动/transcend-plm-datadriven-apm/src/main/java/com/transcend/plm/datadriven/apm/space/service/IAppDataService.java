package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transsion.framework.dto.BaseRequest;

import java.util.List;
import java.util.Map;

/**
 * 处理应用数据的服务接口，Apm层统一在此处调用provider层的接口
 *
 * @author unknown
 */
public interface IAppDataService {
    /**
     * add
     *
     * @param modelCode modelCode
     * @param appData   appData
     * @param <T>       <T>
     * @return {@link T}
     */
    <T extends MObject> T add(String modelCode, T appData);

    /**
     * addBatch
     *
     * @param modelCode modelCode
     * @param mObjects  mObjects
     * @return {@link Boolean}
     */
    Boolean addBatch(String modelCode, List<? extends MBaseData> mObjects);


    /**
     * addBatch
     *
     * @param modelCode modelCode
     * @param mObjects  mObjects
     * @return {@link Boolean}
     */
    Boolean addBatch(String modelCode, List<? extends MBaseData> mObjects, Boolean ignorePermission);

    /**
     * 对其它应用提供接口，判断登录人员是否有新增权限
     * @param appData   查询参数
     * @return Boolean
     */
    Boolean validAddPermission(String spaceBid,String spaceAppBid, Map<String,String> appData);


    /**
     * list
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param queryCondition queryCondition
     * @return {@link List<MObject>}
     */
    List<MObject> list(String spaceBid, String spaceAppBid, QueryCondition queryCondition);

    /**
     * 单对象递归查询list
     * 查询匹配数据的父级和子级
     * @param spaceBid
     * @param spaceAppBid
     * @param queryCondition
     * @return
     */
    List<MObject> signObjectRecursionList(String spaceBid, String spaceAppBid, QueryCondition queryCondition);
    /**
     * list
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param userNO         userNO
     * @param queryCondition queryCondition
     * @return {@link List<MObject>}
     */
    List<MObject> list(String spaceBid, String spaceAppBid, String userNO, QueryCondition queryCondition);

    /**
     * page
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param pageQo         pageQo
     * @param filterRichText filterRichText
     * @param <T>            <T>
     * @return {@link PagedResult<T>}
     */
    <T extends MObject> PagedResult<T> page(String spaceBid, String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText);

    /**
     * page
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param userNO         userNO
     * @param pageQo         pageQo
     * @param filterRichText filterRichText
     * @param <T>            <T>
     * @return {@link PagedResult<T>}
     */
    <T extends MObject> PagedResult<T> page(String spaceBid, String spaceAppBid, String userNO, BaseRequest<QueryCondition> pageQo, boolean filterRichText);

    /**
     * page
     *
     * @param spaceBid       spaceBid
     * @param spaceAppBid    spaceAppBid
     * @param pageQo         pageQo
     * @param filterRichText filterRichText
     * @param <T>            <T>
     * @return {@link PagedResult<T>}
     */
    <T extends MObject> PagedResult<T> pageWithoutPermission(String spaceBid, String spaceAppBid, BaseRequest<QueryCondition> pageQo, boolean filterRichText);


    /**
     * updateByBid
     *
     * @param modelCode modelCode
     * @param bid       bid
     * @param mObject   mObject
     * @param <T>       <T>
     * @return {@link Boolean}
     */
    <T extends MObject> Boolean updateByBid(String modelCode, String bid, T mObject);

    /**
     * promote
     *
     * @param spaceBid   spaceBid
     * @param appBid     appBid
     * @param promoteDto promoteDto
     * @return {@link MSpaceAppData}
     */
    MSpaceAppData promote(String spaceBid, String appBid, LifeCyclePromoteDto promoteDto);

    /**
     * promoteVersionObject
     *
     * @param spaceBid   spaceBid
     * @param appBid     appBid
     * @param promoteDto promoteDto
     * @return {@link MSpaceAppData}
     */
    MSpaceAppData promoteVersionObject(String spaceBid, String appBid, LifeCyclePromoteDto promoteDto);

    /**
     * checkPermission
     *
     * @param appData      appData
     * @param operatorCode operatorCode
     * @param <T>          <T>
     */
    <T extends MBaseData> void checkPermission(T appData, String operatorCode);

    /**
     *
     * @param appData
     * @param operatorCode
     * @return
     */
    Boolean checkButtonPermission(MObject appData, String operatorCode);

}
