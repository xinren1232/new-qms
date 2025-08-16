package com.transcend.plm.datadriven.domain.object.relation;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.feign.CfgObjectRelationFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectRelationVO;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.api.model.dto.ReviseDto;
import com.transcend.plm.datadriven.api.model.mata.RelationCrossUpApplicationQo;
import com.transcend.plm.datadriven.api.model.relation.qo.CrossRelationPathChainQO;
import com.transcend.plm.datadriven.api.model.relation.qo.RelationQo;
import com.transcend.plm.datadriven.api.model.relation.vo.RelationAndTargetVo;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.domain.object.base.OperationLogService;
import com.transcend.plm.datadriven.domain.object.base.RelationModelDomainService;
import com.transcend.plm.datadriven.domain.object.base.VersionModelDomainService;
import com.transcend.plm.datadriven.domain.object.base.pojo.dto.OperationLog;
import com.transcend.plm.datadriven.domain.object.version.VersionObjectManageService;
import com.transcend.plm.datadriven.infrastructure.basedata.repository.RelationDataRepository;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.dto.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 关系业务组装
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class RelationObjectManageService {
    @Resource
    private VersionObjectManageService versionObjectManageService;
    @Resource
    private RelationModelDomainService relationModelDomainService;

    @Resource
    private VersionModelDomainService versionModelDomainService;
    @Resource
    private CfgObjectFeignClient cfgObjectClient;
    @Resource
    private CfgObjectRelationFeignClient cfgObjectRelationClient;

    @Resource
    private RelationDataRepository relationDataRepository;
    @Resource
    private OperationLogService operationLogService;

    /**
     * 检出
     *
     * @param modelCode 模型编码
     * @param bid       数据bid
     * @return MVersionObject  返回version+1的草稿数据
     */
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject checkOut(String modelCode, String bid) {
        isVersionObject(modelCode);
        // 1.调用版本的检出
        MVersionObject versionObject = versionObjectManageService.checkOut(modelCode, bid);
        // 2.查询关系的配置，并复制关系
        List<ObjectRelationVO> objectRelationVOS = getObjectRelationVOS(modelCode);
        if (objectRelationVOS != null) {
            objectRelationVOS.forEach(objectRelationVO ->
                    relationModelDomainService.copyAndReset(objectRelationVO.getModelCode(), bid,
                            ImmutableMap.of(
                                    RelationEnum.SOURCE_BID.getCode(), versionObject.getBid(),
                                    RelationEnum.DRAFT.getCode(), Boolean.TRUE
                            ), null));
        }
        //记录操作日志
//        saveOpLog(modelCode,versionObject,"执行了检出操作");
        return versionObject;
    }

    /**
     * @param modelCode
     */
    private void isVersionObject(String modelCode) {
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(modelCode).getCheckExceptionData();
        if (cfgObjectVo == null) {
            throw new PlmBizException(String.format("模型[%s]不存在", modelCode));
        }
        if (!ObjectTypeEnum.VERSION.getCode().equals((cfgObjectVo.getType()))) {
            throw new PlmBizException(String.format("模型[%s]不是版本对象类型", modelCode));
        }
    }


    /**
     * 暂存
     * <p>
     * <p>
     * 由于检出时，会生成一个新版本的快照数据，用草稿来存储，因此，在检出阶段，可以更改草稿的数据
     *
     * @param modelCode
     * @param mObject
     * @return MBaseData
     */
    public Boolean draftStorage(String modelCode, MVersionObject mObject) {
        return versionObjectManageService.draftStorage(modelCode, mObject);
    }


    /**
     * 撤销检出
     *
     * @param modelCode 模型编码
     * @param bid       实例bid
     * @return Boolean  是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject cancelCheckOut(String modelCode, String bid) {
        isVersionObject(modelCode);
        // 1.调用版本的撤销检出
        MVersionObject mVersionObject = versionObjectManageService.cancelCheckOut(modelCode, bid);
        // 2.删除检出复制的 关系表数据
        List<ObjectRelationVO> objectRelationVOS = getObjectRelationVOS(modelCode);
        if (objectRelationVOS != null) {
            objectRelationVOS.forEach(objectRelationVO ->
                    relationModelDomainService.deleteBySourceBid(objectRelationVO.getModelCode(), bid));
        }
        //记录操作日志
//        saveOpLog(modelCode,mVersionObject,"执行了取消检出操作");
        return mVersionObject;
    }

    /**
     * 检入
     *
     * @param modelCode
     * @param mObject
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject checkIn(String modelCode, MVersionObject mObject) {
        isVersionObject(modelCode);
        //检测是否有dataBid
        if (mObject == null || StringUtils.isBlank(mObject.getDataBid())) {
            throw new IllegalArgumentException(("对象实例DataBid为空"));
        }
        // 源对象调用版本的检入
        MVersionObject mVersionObject = versionObjectManageService.checkIn(modelCode, mObject);
        // 查询关系的配置，并复制关系
        List<ObjectRelationVO> objectRelationVOS = getObjectRelationVOS(modelCode);
        // 生效关系的草稿
        if (!CollectionUtils.isEmpty(objectRelationVOS)) {
            objectRelationVOS.forEach(objectRelationVO ->{
                        relationModelDomainService.effectDraft(objectRelationVO.getModelCode(), mVersionObject.getDataBid());
                    });

        }
        return mVersionObject;
    }

    /**
     * 合并检入（合并检出与检入的一套动作）   TODO
     *
     * @param modelCode      模型编码
     * @param mVersionObject 版本实例数据
     * @return MVersionObject
     */
    public MVersionObject mergeCheckIn(String modelCode, MVersionObject mVersionObject) {
        // 1.调用版本的检出
        versionObjectManageService.mergeCheckIn(modelCode, mVersionObject);
        // 2.查询关系的配置，并复制关系
        // 3.按浮动固定转换规则，更新关系表的 targetBid,或者该上一个版本targetBid

        return null;
    }


    /**
     * 修订    TODO
     *
     * @return MBaseData
     */
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject revise(ReviseDto reviseDto) {
        //获取当前实例数据
        MVersionObject mVersionObject = versionModelDomainService.getByBid(reviseDto.getModelCode(), reviseDto.getBid());
        if("history".equals(mVersionObject.get("dataSource"))){
            throw new PlmBizException("历史数据不允许修订");
        }
        //获取关系数据
        List<ObjectRelationVO> objectRelationVos = getObjectRelationVOS(reviseDto.getModelCode());
        MVersionObject revise;
        // 1.调用版本修订功能
        if(StringUtils.isNoneBlank(reviseDto.getInitLifeCode())){
            revise = versionObjectManageService.reviseToAssignLifeCode(reviseDto.getModelCode(), mVersionObject.getDataBid(),reviseDto.getInitLifeCode());
        }else {
            revise = versionObjectManageService.revise(reviseDto.getModelCode(), mVersionObject.getDataBid());
        }
        if (CollectionUtils.isNotEmpty(objectRelationVos)) {
            for (ObjectRelationVO objectRelationVO : objectRelationVos) {
                //如果使用的是流程生命周期只复制数据
                if(reviseDto.getOnlyChaneVersionAndCopyRelation()){
                    //固定关系
                    ImmutableMap<String, Object> immutableMap = new ImmutableMap.Builder<String, Object>()
                            .put(RelationEnum.SOURCE_BID.getCode(), revise.getBid())
                            .build();
                    // 2.查询关系的配置，并复制关系
                    List<MRelationObject> mRelationObjects = relationModelDomainService.copyAndReset(objectRelationVO.getModelCode(), mVersionObject.getBid(), immutableMap, null);
                    relationModelDomainService.deleteBySourceBid(objectRelationVO.getModelCode(), mVersionObject.getBid());
                    relationModelDomainService.addHisBatch(objectRelationVO.getModelCode(), mRelationObjects);
                    continue;
                }
                // 后面多线程进行性能优化 todo
                // 2.按浮动固定转换规则，更新关系表的 targetBid,或者该上一个版本targetBid
                //判断关系规则
                ObjectRelationRuleQo objectRelationRuleQo = new ObjectRelationRuleQo();
                objectRelationRuleQo.setLcTemplateBid(mVersionObject.get(TranscendModelBaseFields.LC_TEMPL_BID)+"");
                objectRelationRuleQo.setVersion(mVersionObject.get(TranscendModelBaseFields.LC_TEMPL_VERSION)+"");
                objectRelationRuleQo.setFromLifeCycleCode(mVersionObject.getLifeCycleCode());
                objectRelationRuleQo.setToLifeCycleCode(revise.getLifeCycleCode());
                objectRelationRuleQo.setRelationModelCode(objectRelationVO.getModelCode());
                String relRule = cfgObjectRelationClient.getRelationRuleRes(objectRelationRuleQo)
                        .getCheckExceptionData();
                if (RelationConfigEnum.FLOAT2FIXED.getCode().equals(relRule) || RelationConfigEnum.FLOAT2FLOAT.getCode().equals(relRule)) {
                    //查询目标表的最新bid, 记录转换后的行为
                    relationModelDomainService.copyAndModify(objectRelationVO.getTargetModelCode(), objectRelationVO.getModelCode(),
                            mVersionObject.getBid(), revise.getBid(), relRule.split("2")[1]);
                } else {
                    //固定关系
                    ImmutableMap<String, Object> immutableMap = new ImmutableMap.Builder<String, Object>()
                            .put(RelationEnum.SOURCE_BID.getCode(), revise.getBid())
                            .build();
                    // 2.查询关系的配置，并复制关系
                    List<MRelationObject> mRelationObjects = relationModelDomainService.copyAndReset(objectRelationVO.getModelCode(), mVersionObject.getBid(), immutableMap, relRule.split("2")[1]);
                    relationModelDomainService.deleteBySourceBid(objectRelationVO.getModelCode(), mVersionObject.getBid());
                    relationModelDomainService.addHisBatch(objectRelationVO.getModelCode(), mRelationObjects);
                }
            }
        }
        //记录操作日志
        saveOpLog(reviseDto.getModelCode(),revise,"执行了修订操作,版本变成了"+revise.getRevision()+"-"+revise.getVersion());
        return revise;
    }

    private void saveOpLog(String modelCode,MVersionObject mVersionObject,String logDesc){
        OperationLog operationLog = new OperationLog();
        operationLog.setModelCode(modelCode);
        operationLog.setInstanceDataBid(mVersionObject.getDataBid());
        operationLog.setInstanceBid(mVersionObject.getBid());
        operationLog.setContent(logDesc);
        operationLog.setCreatedBy(SsoHelper.getJobNumber());
        operationLog.setCreatedByName(SsoHelper.getName());
        operationLog.setTenantCode(SsoHelper.getTenantCode());
        operationLog.setUpdatedBy(SsoHelper.getJobNumber());
        operationLogService.saveOperationLog(operationLog);
    }

    private List<ObjectRelationVO> getObjectRelationVOS(String modelCode) {
        BaseResponse<List<ObjectRelationVO>> response = cfgObjectClient.getObjectRelationVOsBySourceModelCode(modelCode);
        if (response == null || !Boolean.TRUE.equals(response.isSuccess())) {
            throw new PlmBizException("请求关系配置出错");
        }
        return response.getData();
    }

    /**
     * 生命周期提升    TODO
     *
     * @return MBaseData
     */
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject promote(LifeCyclePromoteDto dto) {
        //获取当前源对象实例数据
        MVersionObject mVersionObject = versionModelDomainService.getByBid(dto.getModelCode(), dto.getBid());
        if("history".equals(mVersionObject.get("dataSource"))){
            throw new PlmBizException("历史数据不允许提升");
        }
        if (Boolean.TRUE.equals(dto.getIsOnlyChangeLifeCode())) {
            //特殊处理逻辑是否只升版
            return versionObjectManageService.promote(dto);
        }

        //获取关系数据
        List<ObjectRelationVO> objectRelationVos = cfgObjectClient
                .getObjectRelationVOsBySourceModelCode(dto.getModelCode()).getCheckExceptionData();
        //是否只是提升状态
        boolean isOnlyPromote = true;
        String newSourceBid = SnowflakeIdWorker.nextIdStr();
        Map<String,String> relationRuleMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(objectRelationVos)) {
            for (ObjectRelationVO objectRelationVO : objectRelationVos) {
                // 后面多线程进行性能优化 todo
                // 2.按浮动固定转换规则，更新关系表的 targetBid,或者该上一个版本targetBid
                //判断关系规则
                ObjectRelationRuleQo objectRelationRuleQo = new ObjectRelationRuleQo();
                objectRelationRuleQo.setLcTemplateBid(mVersionObject.get(TranscendModelBaseFields.LC_TEMPL_BID) + "");
                objectRelationRuleQo.setVersion(mVersionObject.get(TranscendModelBaseFields.LC_TEMPL_VERSION) + "");
                objectRelationRuleQo.setFromLifeCycleCode(dto.getBeforeLifeCycleCode());
                objectRelationRuleQo.setToLifeCycleCode(dto.getAfterLifeCycleCode());
                objectRelationRuleQo.setRelationModelCode(objectRelationVO.getModelCode());
                objectRelationRuleQo.setTargetModelCode(dto.getModelCode());
                String relRule = cfgObjectRelationClient.getRelationRuleRes(objectRelationRuleQo)
                        .getCheckExceptionData();
                if (RelationConfigEnum.FIXED2FLOAT.getCode().equals(relRule)){
                    isOnlyPromote = false;
                }
                relationRuleMap.put(objectRelationVO.getBid(),relRule);
            }
        }
        if (CollectionUtils.isNotEmpty(objectRelationVos)) {
            for (ObjectRelationVO objectRelationVO : objectRelationVos) {

                if (RelationConfigEnum.FLOAT2FIXED.getCode().equals(relationRuleMap.get(objectRelationVO.getBid()))) {
                    //走其他逻辑
                    relationModelDomainService.copyAndModify(objectRelationVO.getTargetModelCode(), objectRelationVO.getModelCode(),
                            mVersionObject.getBid(),  isOnlyPromote?mVersionObject.getBid():newSourceBid, relationRuleMap.get(objectRelationVO.getBid()).split("2")[1]);
                } else if (RelationConfigEnum.FIXED2FLOAT.getCode().equals(relationRuleMap.get(objectRelationVO.getBid()))
                || !isOnlyPromote) {
                    //走其他逻辑
                    ImmutableMap<String, Object> immutableMap = new ImmutableMap.Builder<String, Object>()
                            .put(RelationEnum.SOURCE_BID.getCode(), newSourceBid)
                            .build();
                    // 2.查询关系的配置，并复制关系
                    List<MRelationObject> mRelationObjects = relationModelDomainService.copyAndReset(objectRelationVO.getModelCode(), mVersionObject.getBid(),
                            immutableMap, relationRuleMap.get(objectRelationVO.getBid()).split("2")[1]);
                    relationModelDomainService.deleteBySourceBid(objectRelationVO.getModelCode(), mVersionObject.getBid());
                    relationModelDomainService.addHisBatch(objectRelationVO.getModelCode(), mRelationObjects);
                }
            }
        }
        String stateChange = "状态由"+dto.getBeforeLifeCycleCode()+"变成"+dto.getAfterLifeCycleCode();
        if (isOnlyPromote) {
            // 1.调用版本的生命周期提升
            MVersionObject mVersion = versionObjectManageService.promote(dto);
            //记录操作日志
            saveOpLog(dto.getModelCode(),mVersion,"执行了生命周期提升操作,"+stateChange);
            return mVersion;
        } else {
            MVersionObject mVersion = versionObjectManageService.promote(dto.getModelCode(), mVersionObject.getDataBid(), dto.getAfterLifeCycleCode(), newSourceBid);
            //记录操作日志
            saveOpLog(dto.getModelCode(),mVersion,"执行了生命周期提升操作,"+stateChange+",版本变成了"+mVersion.getRevision()+"-"+mVersion.getVersion());
            return mVersion;
        }
    }

    /**
     * 分页查询关系对象和目标对象实例数据
     */
    public PagedResult<RelationAndTargetVo> pageRelationAndTarget(BaseRequest<RelationQo> pageQo) {
        //查询源对象的实例数据，用以判断是浮动还是固定
        RelationQo qo = pageQo.getParam();
        MVersionObject mVersionObject = versionModelDomainService.getByBid(qo.getSourceModelCode(), qo.getRelationData().getSourceBid());
        if (mVersionObject == null) {
            throw new PlmBizException("源对象实例数据不存在");
        }
        ObjectRelationRuleQo objectRelationRuleQo = new ObjectRelationRuleQo();
        objectRelationRuleQo.setLcTemplateBid(mVersionObject.get(TranscendModelBaseFields.LC_TEMPL_BID)+"");
        objectRelationRuleQo.setVersion(mVersionObject.get(TranscendModelBaseFields.LC_TEMPL_VERSION)+"");
        objectRelationRuleQo.setFromLifeCycleCode(mVersionObject.getLifeCycleCode());
        objectRelationRuleQo.setToLifeCycleCode(mVersionObject.getLifeCycleCode());
        objectRelationRuleQo.setRelationModelCode(qo.getRelationData().getModelCode());
        String relRule = cfgObjectRelationClient.getRelationRuleRes(objectRelationRuleQo)
                .getCheckExceptionData();
        List<RelationAndTargetVo> relationAndTargetVos;
        PageMethod.startPage(pageQo.getCurrent(), pageQo.getSize());
        if (RelationConfigEnum.FIXED2FLOAT.getCode().equals(relRule)) {
            //固定关系
            relationAndTargetVos = relationDataRepository.listRelationDataFixed(qo);
        } else {
            //浮动关系
            qo.getRelationData().remove(RelationEnum.SOURCE_BID.getCode());
            qo.getRelationData().setSourceDataBid(mVersionObject.getDataBid());
            relationAndTargetVos = relationDataRepository.listRelationDataFloat(qo);
        }
        PageInfo<RelationAndTargetVo> pageInfo = new PageInfo<>(relationAndTargetVos);
        return PageResultTools.create(pageInfo);
    }

    /**
     * 自下向上寻址的跨层级查询源对象的实例集合 强浮动以bid作为关联
     *
     * @param relationCrossUpApplicationQo
     * @param pageQo
     * @return
     */
    public PagedResult<MObject> pageCrossHierarchyUp(RelationCrossUpApplicationQo relationCrossUpApplicationQo,
                                                     BaseRequest<QueryCondition> pageQo){
        return relationDataRepository.pageCrossHierarchyUp(relationCrossUpApplicationQo, pageQo);
    }

    /**
     * @param qo
     * @param pageQo
     * @return {@link PagedResult }<{@link MObject }>
     */
    public PagedResult<MObject> pageCrossRelationInstance(CrossRelationPathChainQO qo, BaseRequest<QueryCondition> pageQo) {
        return relationDataRepository.pageCrossRelationInstance(qo, pageQo);
    }
}
