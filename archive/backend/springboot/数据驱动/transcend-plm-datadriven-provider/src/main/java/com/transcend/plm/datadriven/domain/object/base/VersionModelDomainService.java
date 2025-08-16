package com.transcend.plm.datadriven.domain.object.base;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.ReviseDto;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Component
public class VersionModelDomainService extends AbstractObjectDomainService<MVersionObject> {
    @Resource
    private CfgObjectFeignClient cfgObjectClient;
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Resource
    private CfgObjectFeignClient cfgObjectFeignClient;
    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationFeignClient;
    @Resource
    private RelationModelDomainService relationModelDomainService;

    /**
     * @param modelService
     */
    public VersionModelDomainService(ModelService<MVersionObject> modelService) {
        this.modelService = modelService;
    }

    /**
     * @param modelCode
     * @param mObject
     * @return {@link MVersionObject }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject add(String modelCode, MVersionObject mObject) {
        //初始化固定值
        mObject.setModelCode(modelCode);
        mObject.put(TranscendModelBaseFields.MODEL_CODE, modelCode);
        String bid = SnowflakeIdWorker.nextIdStr();
        if (StringUtil.isBlank(String.valueOf(mObject.get(TranscendModelBaseFields.BID)))) {
            mObject.put(TranscendModelBaseFields.BID, bid);
        }
        if (StringUtil.isBlank(String.valueOf(mObject.get(TranscendModelBaseFields.DATA_BID)))) {
            mObject.put(TranscendModelBaseFields.DATA_BID, bid);
        }
        ObjectModelLifeCycleVO objectModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(modelCode)
                .getCheckExceptionData();
        setMObjectDefaultValue(mObject, objectModelLifeCycleVO, modelCode);
        String versionStr = VersionObjectEnum.VERSION.getCode();
        if (mObject.get(versionStr) == null || StringUtil.isBlank(String.valueOf(mObject.get(versionStr)))) {
            mObject.put(versionStr, "1");
        }
        String revisionStr = VersionObjectEnum.REVISION.getCode();
        if (mObject.get(revisionStr) == null || StringUtil.isBlank(String.valueOf(mObject.get(revisionStr)))) {
            mObject.put(revisionStr, "A");
        }
        this.addHis(modelCode, mObject);
        return super.add(modelCode, mObject);
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link MVersionObject }
     */
    @Override
    public MVersionObject getByBid(String modelCode, String bid) {
        return JSON.parseObject(JSON.toJSONString(objectModelCrudI.getByBid(modelCode, bid)), MVersionObject.class) ;
    }

    /**
     * @param modelCode
     * @param dataBids
     * @return {@link List }<{@link MVersionObject }>
     */
    public List<MVersionObject> listMVersionObjects(String modelCode, List<String> dataBids){
        QueryWrapper qo = new QueryWrapper();
        qo.in(DataBaseConstant.COLUMN_DATA_BID, dataBids);
        List<QueryWrapper> mWrappers = QueryWrapper.buildSqlQo(qo);
        return this.list(modelCode, mWrappers);
    }

    /**
     * @param modelCode
     * @param dataBid
     * @return {@link List }<{@link MVersionObject }>
     */
    public List<MVersionObject> listMVersionObjectHis(String modelCode, String dataBid){
        QueryWrapper qo = new QueryWrapper();
        qo.eq(DataBaseConstant.COLUMN_DATA_BID, dataBid);
        List<QueryWrapper> mWrappers = QueryWrapper.buildSqlQo(qo);
        return this.listHis(modelCode, mWrappers);
    }

    /**
     * @param modelCode
     * @param versionObject
     * @return {@link MVersionObject }
     */
    @Override
    public MVersionObject addHis(String modelCode, MVersionObject versionObject) {
        //深克隆versionObject,然后清空id
        MVersionObject hisVersionObject = JSON.parseObject(JSON.toJSONString(versionObject), MVersionObject.class);
        hisVersionObject.remove(BaseDataEnum.ID.getCode());
        return modelService.addHis(modelCode, hisVersionObject);
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link MVersionObject }
     */
    public MVersionObject checkOut(String modelCode, String bid) {
        return objectModelCrudI.checkOut(modelCode, bid);
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link MVersionObject }
     */
    public MVersionObject cancelCheckOut(String modelCode, String bid) {
        return objectModelCrudI.cancelCheckOut(modelCode, bid);
    }

    /**
     * @param modelCode
     * @param mVersionObject
     * @return {@link MVersionObject }
     */
    public MVersionObject checkIn(String modelCode, MVersionObject mVersionObject) {
        return objectModelCrudI.checkIn(modelCode, mVersionObject);
    }

    /**
     * @param modelCode
     * @param bid
     * @return {@link Boolean }
     */
    @Override
    public Boolean logicalDeleteByBid(String modelCode, String bid) {
        return objectModelCrudI.logicalDeleteByBid(modelCode, bid);
    }

    /**
     * @param modelCode
     * @param bids
     * @return {@link Boolean }
     */
    @Override
    public Boolean batchLogicalDeleteByBid(String modelCode, List<String> bids) {
        //删除实例数据
        boolean deleteResult = objectModelCrudI.batchLogicalDeleteByBids(modelCode, bids);
        //逻辑删除该对象关联的关系数据
        if (deleteResult) {
            Map<String, Set<String>> relationDeleteParams = getRelationDeleteParams(modelCode, Sets.newHashSet(bids));
            relationModelDomainService.batchLogicalDeleteBySourceBid(relationDeleteParams);
        }
        return deleteResult;
    }

    /**
     * @param modelCode
     * @param pageQo
     * @param filterRichText
     * @return {@link PagedResult }<{@link MVersionObject }>
     */
    @Override
    public PagedResult<MVersionObject> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        return objectModelCrudI.page(modelCode, pageQo, filterRichText);
    }

    /**
     * @param modelCode
     * @param queryCondition
     * @return {@link List }<{@link MVersionObject }>
     */
    @Override
    public List<MVersionObject> listHis(String modelCode, QueryCondition queryCondition) {
        return super.listHis(modelCode, queryCondition);
    }

    /**
     * @param mVersionObject
     * @return {@link Boolean }
     */
    public Boolean saveDraft(MVersionObject mVersionObject) {
        return objectModelCrudI.saveDraft(mVersionObject);
    }

    /**
     * @param mObject
     * @param objectModelLifeCycleVO
     * @param modelCode
     */
    public void setMObjectDefaultValue(MObject mObject, ObjectModelLifeCycleVO objectModelLifeCycleVO, String modelCode) {
        mObject.put(TranscendModelBaseFields.ENABLE_FLAG, 0);
        mObject.put(TranscendModelBaseFields.DELETE_FLAG, 0);
        mObject.put(TranscendModelBaseFields.TENANT_ID, SsoHelper.getTenantId());
        mObject.put(TranscendModelBaseFields.CREATED_BY, SsoHelper.getJobNumber());
        mObject.put(TranscendModelBaseFields.UPDATED_BY, SsoHelper.getJobNumber());
        LocalDateTime now = LocalDateTime.now();
        mObject.put(TranscendModelBaseFields.CREATED_TIME, now);
        mObject.put(TranscendModelBaseFields.UPDATED_TIME, now);
        if (mObject.get(TranscendModelBaseFields.BID) == null) {
            mObject.put(TranscendModelBaseFields.BID, SnowflakeIdWorker.nextIdStr());
        }
        if (mObject.get(TranscendModelBaseFields.DATA_BID) == null) {
            mObject.put(TranscendModelBaseFields.DATA_BID, SnowflakeIdWorker.nextIdStr());
        }
        if (objectModelLifeCycleVO != null) {
            if (mObject.get(TranscendModelBaseFields.LC_TEMPL_BID) == null && StringUtil.isNotBlank(objectModelLifeCycleVO.getLcTemplBid())) {
                mObject.put(TranscendModelBaseFields.LC_TEMPL_BID, objectModelLifeCycleVO.getLcTemplBid());
            }
            if (mObject.get(TranscendModelBaseFields.LC_TEMPL_VERSION) == null && StringUtil.isNotBlank(objectModelLifeCycleVO.getLcTemplVersion())) {
                mObject.put(TranscendModelBaseFields.LC_TEMPL_VERSION, objectModelLifeCycleVO.getLcTemplVersion());
            }
            if (StringUtil.isBlank(mObject.getLifeCycleCode())) {
                //查询初始状态
                mObject.setLifeCycleCode(objectModelLifeCycleVO.getInitState());
            }
            mObject.put(TranscendModelBaseFields.LC_MODEL_CODE, objectModelLifeCycleVO.getInitState() + ":" + modelCode);
        }
    }

    /**
     * @param modelCode
     * @param bids
     * @return {@link Map }<{@link String }, {@link Set }<{@link String }>>
     */
    private Map<String, Set<String>> getRelationDeleteParams(String modelCode, Set<String> bids) {
        Map<String, Set<String>> deleteParams = Maps.newHashMap();
        // 查询是否是关系对象，如果不是关系对象，需要删除关系对象的关联关系，批量
        CfgObjectVo cfgObjectVo = cfgObjectFeignClient.getByModelCode(modelCode).getCheckExceptionData();
        if (!ObjectTypeEnum.RELATION.getCode().equals(cfgObjectVo.getType())) {
            // 查询当前对象的关系
            List<CfgObjectRelationVo> cfgObjectRelationVos = cfgObjectRelationFeignClient.listRelationTab(modelCode).getCheckExceptionData();
            if (CollectionUtils.isNotEmpty(cfgObjectRelationVos)) {
                for (CfgObjectRelationVo cfgObjectRelationVo : cfgObjectRelationVos) {
                    // 非内置关系 且是关系对象
                    if (!cfgObjectRelationVo.isInner()) {
                        deleteParams.put(cfgObjectRelationVo.getModelCode(), bids);
                    }
                }
            }
        }
        return deleteParams;
    }

    /**
     * 修订
     *
     * @param revise
     * @return
     */
    public MVersionObject revise(ReviseDto revise) {
        return objectModelCrudI.revise(revise);
    }

    /**
     * 提升生命周期
     *
     * @param promoteDto
     * @return
     */
    public MVersionObject promote(LifeCyclePromoteDto promoteDto) {
        return objectModelCrudI.promote(promoteDto);
    }
}
