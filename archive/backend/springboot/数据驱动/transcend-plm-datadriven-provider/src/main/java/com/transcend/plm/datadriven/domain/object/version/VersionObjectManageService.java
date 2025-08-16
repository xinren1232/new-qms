package com.transcend.plm.datadriven.domain.object.version;

import com.alibaba.fastjson.JSON;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.framework.core.exception.TranscendBizException;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.model.MVersionObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.dto.LifeCyclePromoteDto;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.util.AlphabetVersionUpUtils;
import com.transcend.plm.datadriven.domain.object.base.VersionModelDomainService;
import com.transcend.plm.datadriven.infrastructure.draft.po.DraftPO;
import com.transcend.plm.datadriven.infrastructure.draft.repository.DraftRepository;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.dto.BaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 有版本对象业务组装
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class VersionObjectManageService {
    @Resource
    private DraftRepository draftRepository;
    @Resource
    private VersionModelDomainService versionModelDomainService;
    @Resource
    private CfgObjectFeignClient cfgObjectClient;

    /**
     * 根据dataBid查询单个源对象
     *
     * @param modelCode
     * @param dataBid
     * @return
     */
    public MVersionObject getByDataBid(String modelCode, String dataBid){
        MVersionObject mVersionObject = versionModelDomainService.getByDataBid(modelCode, dataBid);
        return mVersionObject;
    }

    /**
     * @param modelCode
     * @param dataBid
     * @return {@link List }<{@link MVersionObject }>
     */
    public List<MVersionObject> getHisByDataBid(String modelCode, String dataBid){
       return versionModelDomainService.listMVersionObjectHis(modelCode,dataBid);
    }

    /**
     * 获取列表
     *
     * @param modelCode
     * @param wrappers
     * @return
     */
    public List<MVersionObject> list(String modelCode, List<QueryWrapper> wrappers) {
        return versionModelDomainService.list(modelCode,wrappers);
    }

    /**
     * 分页查询
     * @param modelCode
     * @param pageQo
     * @return
     */
    public PagedResult<MVersionObject> page(String modelCode, BaseRequest<QueryCondition> pageQo, boolean filterRichText) {
        return versionModelDomainService.page(modelCode,pageQo, filterRichText);
    }

    /**
     * 检出
     *
     * @param modelCode
     * @param bid
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject checkOut(String modelCode, String bid) {
        CfgObjectVo cfgObjectVo = cfgObjectClient.getByModelCode(modelCode).getCheckExceptionData();
        if (cfgObjectVo == null) {
            throw new TranscendBizException(String.format("模型[%s]不存在", modelCode));
        }
        if (!ObjectTypeEnum.VERSION.getCode().equals((cfgObjectVo.getType()))) {
            throw new TranscendBizException(String.format("模型[%s]不是版本对象类型", modelCode));
        }
        // 1.获取dataBid的当前版本的数据
        MVersionObject mVersionObject = versionModelDomainService.getByBid(modelCode, bid);
        //校验是否已经被检出
        if (isObjectCheckOut(mVersionObject)) {
            throw new PlmBizException("数据已被检出");
        }
        if (isHistory(mVersionObject)) {
            throw new PlmBizException("历史数据不能检出，请刷新页面");
        }
        // 2.更新当前dataBid的数据的checkOutBy
        mVersionObject.setCheckoutBy(SsoHelper.getJobNumber());
        mVersionObject.setCheckoutTime(new Date());
        // 3.在程序中 version+1， 更新bid, 再存储于草稿表中
        Integer version = mVersionObject.getVersion();
        if(version != null){
            version = version + 1;
        }
        mVersionObject.setUpdatedTime(LocalDateTime.now());
        versionModelDomainService.updateByBid(modelCode, bid, mVersionObject);
        //插入草稿表
        mVersionObject.setVersion(version);
        mVersionObject.setBid(SnowflakeIdWorker.nextIdStr());
        buildAndSaveDraft(mVersionObject);
        return mVersionObject;
    }

    /**
     * @param mVersionObject
     * @return {@link DraftPO }
     */
    private DraftPO buildAndSaveDraft(MVersionObject mVersionObject) {
        String jsonStr = JSON.toJSONString(mVersionObject);
        DraftPO draftDataPO = new DraftPO();
        draftDataPO.setBid(mVersionObject.getBid());
        draftDataPO.setDataBid(mVersionObject.getDataBid());
        draftDataPO.setContent(jsonStr);
        draftDataPO.setBaseModel(mVersionObject.getModelCode());
        draftDataPO.setCreatedBy(SsoHelper.getJobNumber());
        draftDataPO.setUpdatedBy(SsoHelper.getJobNumber());
        draftDataPO.setCreatedTime(new Date());
        draftDataPO.setUpdatedTime(new Date());
        draftRepository.saveDraftData(draftDataPO);
        return draftDataPO;
    }

    /**
     * 对象实例是否已被检出
     *
     * @param mVersionObject 对象实例
     * @return
     */
    public boolean isObjectCheckOut(MVersionObject mVersionObject) {
        if (mVersionObject == null) {
            throw new PlmBizException("数据实例为空");
        }
        return StringUtils.isNotBlank(mVersionObject.getCheckoutBy());
    }

    /**
     * @param mVersionObject
     * @return boolean
     */
    public boolean isHistory(MVersionObject mVersionObject) {
        return "history".equals(mVersionObject.get("dataSource"));
    }

    /**
     * 对象实例是否已被检出
     *
     * @param modelCode 对象模型
     * @param dataBid   数据Id
     * @return
     */
    public boolean isObjectCheck(String modelCode, String dataBid) {
        MVersionObject mVersionObject = versionModelDomainService.getByDataBid(modelCode,dataBid);
        return this.isObjectCheckOut(mVersionObject);
    }

    /**
     * 撤销检出
     *
     * @param modelCode 模型编码
     * @param bid       数据bid
     * @return MBaseData
     */
    public MVersionObject cancelCheckOut(String modelCode, String bid) {
        // 2.把当前版本的 检出人和检出时间置空
        DraftPO draftPO = draftRepository.getByBid(bid);
        MVersionObject versionObject = versionModelDomainService.getByDataBid(modelCode, draftPO.getDataBid());
        // 1.删草稿
        draftRepository.deleteDraftByDataBid(versionObject.getDataBid());
        versionObject.setCheckoutBy(null);
        versionObject.setCheckoutTime(null);
        versionModelDomainService.updateByBid(modelCode, versionObject.getBid(), versionObject);
        return versionObject;
    }

    /**
     * 根据检出人判断是否是当前登录人查询实例数据
     *
     * @param modelCode
     * @param bid
     * @return
     */
    public Object getOrDraftByBid(String modelCode, String bid){
        //查实例表
        MVersionObject mVersionObject = versionModelDomainService.getByBid(modelCode, bid);
        //判断检出人checkOutBy是不是当前登录人 是返回草稿数据，否则返回实例数据
        String checkOutBy = SsoHelper.getJobNumber();
        if(mVersionObject.getCheckoutBy().equals(checkOutBy)){
            return draftRepository.getByDataBid(mVersionObject.getDataBid());
        }else{
            return mVersionObject;
        }
    }

    /**
     * 检入
     *
     * @param modelCode
     * @param mObject
     * @return MBaseData
     */
    @Transactional(rollbackFor = Exception.class)
    public MVersionObject checkIn(String modelCode, MVersionObject mObject) {
        // 1.用草稿的的数据 更新当前版本的数据
        String dataBid = mObject.getDataBid();
        //清空检出人和检出时间
        mObject.setCheckoutBy(null);
        mObject.setCheckoutTime(null);
        // 3.调用updateByDataBid
        versionModelDomainService.updateByDataBid(modelCode, dataBid, mObject);
        // 4.直接用草稿数据插入历史表(a.根据modelCode获取到表的信息 tableDefination,补充后缀)
        versionModelDomainService.addHis(modelCode, mObject);
        // 2.删掉草稿
        draftRepository.deleteDraftByDataBid(dataBid);
        return mObject;
    }


    /**
     * 合并检入（合并检出与检入的一套动作）   TODO
     *
     * @param baseModel
     * @param mObject
     * @return MBaseData
     */
    public MVersionObject mergeCheckIn(String baseModel, MVersionObject mObject) {
        // 1.获取dataBid的当前版本的数据
        MVersionObject mVersionObject = versionModelDomainService.getByDataBid(baseModel, mObject.getDataBid());
        //todo 需要严重map是否生效
        BeanUtil.copyProperties(mObject,mVersionObject);
        // 2.更新当前版本的数据，以及version+1， 更新bid
        mVersionObject.setVersion(mVersionObject.getVersion()+1);
        mVersionObject.setBid(SnowflakeIdWorker.nextIdStr());
        // 3.调用updateByDataBid
        versionModelDomainService.updateByDataBid(baseModel, mObject.getDataBid(), mVersionObject);
        // 4.直接插入历史表
        versionModelDomainService.addHis(baseModel, mVersionObject);
        return mVersionObject;
    }

    /**
     * 修订    TODO
     *
     * @param modelCode
     * @param dataBid
     * @return MBaseData
     */
    public MVersionObject revise(String modelCode, String dataBid) {
        // 1.获取dataBid的当前版本的数据
        MVersionObject mVersionObject = versionModelDomainService.getByDataBid(modelCode, dataBid);
        // 2.更新当前版本的数据，以及version=1,revision= {revision}+1（如A->B）， 更新bid, 生命周期重置
        mVersionObject.setVersion(1);

         mVersionObject.setRevision(AlphabetVersionUpUtils.getNext(mVersionObject.getRevision()));
        mVersionObject.setBid(SnowflakeIdWorker.nextIdStr());
        //重置生命周期状态，需要找到对象
        String initlifeCycleCode = cfgObjectClient.findObjectLifecycleByModelCode(modelCode)
                .getCheckExceptionData().getInitState();
        mVersionObject.setLifeCycleCode(initlifeCycleCode);
        // 3.调用updateByDataBid
        versionModelDomainService.updateByDataBid(modelCode, dataBid, mVersionObject);
        // 4.直接插入历史表
        versionModelDomainService.addHis(modelCode, mVersionObject);
        return mVersionObject;
    }

    /**
     * 修订    TODO
     *
     * @param modelCode
     * @param dataBid
     * @param lifeCode
     * @return MBaseData
     */
    public MVersionObject reviseToAssignLifeCode(String modelCode, String dataBid, String lifeCode) {
        // 1.获取dataBid的当前版本的数据
        MVersionObject mVersionObject = versionModelDomainService.getByDataBid(modelCode, dataBid);
        // 2.更新当前版本的数据，以及version=1,revision= {revision}+1（如A->B）， 更新bid, 生命周期重置
        mVersionObject.setVersion(1);

        mVersionObject.setRevision(AlphabetVersionUpUtils.getNext(mVersionObject.getRevision()));
        mVersionObject.setBid(SnowflakeIdWorker.nextIdStr());
        mVersionObject.setLifeCycleCode(lifeCode);
        // 3.调用updateByDataBid
        versionModelDomainService.updateByDataBid(modelCode, dataBid, mVersionObject);
        // 4.直接插入历史表
        versionModelDomainService.addHis(modelCode, mVersionObject);
        return mVersionObject;
    }

    /**
     * 修订    TODO
     *
     * @param modelCode
     * @param dataBid
     * @param lifeCycleCode
     * @param newBid
     * @return MBaseData
     */
    public MVersionObject promote(String modelCode, String dataBid,String lifeCycleCode,String newBid) {
        // 1.获取dataBid的当前版本的数据
        MVersionObject mVersionObject = versionModelDomainService.getByDataBid(modelCode, dataBid);
        // 2.更新当前版本的数据，以及version=1+1， 更新bid, 生命周期更新到提升的周期
        Integer version = mVersionObject.getVersion();
        if(version != null){
            version = version + 1;
        }
        if(StringUtils.isNotEmpty(newBid)){
            mVersionObject.setBid(newBid);
        }else{
            mVersionObject.setBid(SnowflakeIdWorker.nextIdStr());
        }
        mVersionObject.setVersion(version);
        mVersionObject.setLifeCycleCode(lifeCycleCode);
        mVersionObject.setUpdatedBy(SsoHelper.getJobNumber());
        mVersionObject.setUpdatedTime(LocalDateTime.now());
        // 3.调用updateByDataBid
        versionModelDomainService.updateByDataBid(modelCode, dataBid, mVersionObject);
        // 4.直接插入历史表
        versionModelDomainService.addHis(modelCode, mVersionObject);
        return mVersionObject;
    }

    /**
     * 生命周期提升    TODO
     *
     * @param dto
     * @return MBaseData
     */
    public MVersionObject promote(LifeCyclePromoteDto dto) {

        // 1.获取dataBid的当前版本的数据
        MVersionObject mVersionObject = versionModelDomainService.getByBid(dto.getModelCode(), dto.getBid());
        MVersionObject his = versionModelDomainService.getHisByBid(dto.getModelCode(), dto.getBid());

        // 2.用updateByDataBid更新生命周期
        mVersionObject.setLifeCycleCode(dto.getAfterLifeCycleCode());
        mVersionObject.setLcModelCode(dto.getAfterLifeCycleCode()+":"+dto.getModelCode());
        mVersionObject.setUpdatedBy(SsoHelper.getJobNumber());
        mVersionObject.setUpdatedTime(LocalDateTime.now());

        his.setLifeCycleCode(dto.getAfterLifeCycleCode());
        his.setLcModelCode(dto.getAfterLifeCycleCode()+":"+dto.getModelCode());
        his.setUpdatedBy(SsoHelper.getJobNumber());
        his.setUpdatedTime(LocalDateTime.now());
        versionModelDomainService.updateByDataBid(dto.getModelCode(), mVersionObject.getDataBid(), mVersionObject);
        //更新历史表
        versionModelDomainService.updateHisByBid(dto.getModelCode(), mVersionObject.getBid(), his);
        return mVersionObject;
    }


    /**
     * 获取一个或者从草稿取
     *
     * @param modelCode
     * @param dataBid
     * @return MBaseData
     */
    public MVersionObject getOneOrDraftByDataBid(String modelCode, String dataBid) {
        MVersionObject object = versionModelDomainService.getByDataBid(modelCode, dataBid);
        // 1.获取checkOutBy是否与当前登陆人一致，则从草稿中获取
        String checkOutBy = object.getCheckoutBy();
        if(SsoHelper.getJobNumber().equals(checkOutBy)){
            // 获取草稿 TODOS
        }
        return object;
    }

    /**
     * 暂存    TODO
     * 由于检出时，会生成一个新版本的快照数据，用草稿来存储，因此，在检出阶段，可以更改草稿的数据
     * @return MBaseData
     */
    public Boolean draftStorage(String modelCode, MVersionObject mObject) {
        // 覆盖草稿数据 modelCode 昨晚类型，mObject.getBid作为
        return true;
    }

}
