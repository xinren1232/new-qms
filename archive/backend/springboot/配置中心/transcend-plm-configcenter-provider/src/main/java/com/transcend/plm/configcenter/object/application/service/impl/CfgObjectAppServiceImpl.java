package com.transcend.plm.configcenter.object.application.service.impl;

import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.common.constant.DataBaseConstant;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectAttrRoot;
import com.transcend.plm.configcenter.object.domain.aggergate.CfgObjectRoot;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.object.domain.entity.CfgObjectHistory;
import com.transcend.plm.configcenter.object.infrastructure.extension.ExtensionService;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectAttributePo;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectAttributeRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLifeCycleRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectLockRepository;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.api.model.object.dto.*;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectAttributeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTreeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.configcenter.object.pojo.CfgObjectConverter;
import com.transcend.plm.configcenter.table.domain.service.CfgTableDomainService;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.table.pojo.CfgTableConverter;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableAttributeDto;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ObjectModelAppServiceImpl
 * 对域和基础层进行调用编排，供controller调用
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2021/10/12 14:47
 * @since 1.0
 */
@Slf4j
@Service
@Qualifier("cfgObjectAppServiceImpl")
public class CfgObjectAppServiceImpl implements ICfgObjectAppService, IExcelStrategy {

    @Resource
    private CfgObjectRepository cfgObjectRepository;

    @Resource
    private CfgObjectAttributeRepository cfgObjectAttributeRepository;

    @Resource
    private CfgObjectLifeCycleRepository cfgObjectLifeCycleRepository;

    @Resource
    private CfgObjectLockRepository cfgObjectLockRepository;

    @Resource
    private CfgTableDomainService cfgTableDomainService;

    @Resource
    private ExtensionService extensionService;

    @Resource
    private TransactionTemplate transactionTemplate;


    @Override
    public List<CfgObjectTreeVo> treeAndLockInfo() {
        return CfgObjectRoot.build()
                .findAll()
                .conversionToTreeVo()
                .populateLockInfo()
                .buildTree();
    }

    @Override
    public List<CfgObjectTreeVo> tree() {
        return CfgObjectRoot.build()
                .findAll()
                .conversionToTreeVo()
                .buildTree();
    }

    @Override
    public List<CfgObjectVo> listAllByModelCode(String modelCode){
        List<CfgObjectPo> cfgObjectPos = cfgObjectRepository.findLikeModelCode(modelCode);
        List<CfgObjectVo> list = CfgObjectConverter.INSTANCE.pos2vos(cfgObjectPos);
        return list;
    }

    @Override
    public CfgObjectVo add(ObjectAddParam objectAddParam) {
        return CfgObject.buildWithAddOrUpdate(objectAddParam)
                .populateInfoByParentCode(objectAddParam.getParentCode())
                .populateBaseModel()
                .checkObjectNameCannotBeSame()
                .add();
    }

    @Override
    public Boolean deleteWithChildrenAndInfo(String bid) {
        //查询出所有对象的modelCode
        String modelCode = CfgObject.buildWithBid(bid).toModelCode();
        List<CfgObjectPo> all = cfgObjectRepository.findAllChildrenByModelCode(modelCode);
        List<String> modelCodeList = all.stream().map(CfgObjectPo::getModelCode).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(modelCodeList)) {
            return true;
        }

        return transactionTemplate.execute(transactionStatus -> {
            //删草稿 - 无论有没有草稿都执行删除草稿操作（包含子对象）
            Boolean deleteDraft = extensionService.deleteDraftByList(modelCodeList);
            //删对象 - （包含子对象）
            Boolean deleteObject = cfgObjectRepository.deleteByList(modelCodeList);
            //删属性 - （包含子对象）
            Boolean deleteAttr = cfgObjectAttributeRepository.deleteByList(modelCodeList);
            //删生命周期 - （包含子对象）
            Boolean cancelLifecycle = cfgObjectLifeCycleRepository.deleteByList(modelCodeList);
            //删权限 - （包含子对象）
            Boolean deleteAuth = extensionService.deleteAuthByList(modelCodeList);
            //删视图 - （包含子对象）
            Boolean deleteView = extensionService.deleteViewByList(modelCodeList);
            //删锁 - （包含子对象）
            Boolean deleteLock = cfgObjectLockRepository.deleteByList(modelCodeList);

            log.info("执行对象删除操作，删除草稿结果：{}，删除对象结果：{}，删除属性结果：{}，删除生命周期结果：{}，" +
                            "删除权限结果：{}，删除视图结果：{}，删除锁结果：{}",
                    deleteDraft, deleteObject, deleteAttr, cancelLifecycle, deleteAuth, deleteView, deleteLock);
            return true;
        });
    }

    @Override
    public Boolean updatePosition(List<ObjectPositionParam> objectPositionParamList) {
        return CfgObjectRoot.build()
                .checkWithBulkEditPosition(objectPositionParamList)
                .bulkUpdatePosition(objectPositionParamList);
    }

    @Override
    public CfgObjectVo getObjectAndAll(String bid) {
        return CfgObject.buildWithBid(bid)
                .populateBaseInfo()
                .populateLockInfo()
                .populateAttributes()
                .populateLifeCycle()
                .populateAuth()
                .populateView()
                .build();
    }

    @Override
    public Boolean checkout(String bid) {
        return CfgObject.buildWithBid(bid).populateBaseInfo().checkout();
    }

    @Override
    public Boolean staging(StagingParam stagingParam) {
        return CfgObject.buildWithStaging(stagingParam).populateLockInfo().staging();
    }

    @Override
    public CfgObjectVo readDraft(String bid) {
        return CfgObject.buildWithReadDraft(bid).readDraft();
    }

    @Override
    public Boolean undoCheckout(String bid) {
        return CfgObject.buildWithBid(bid).populateBaseInfo().undoCheckout();
    }

    @Override
    public Boolean checkin(CheckinParam checkinParam) {
        boolean flag = CfgObject.buildWithCheckin(checkinParam)
                .checkObjectNameCannotBeSameWithCheckin()
                .checkin(checkinParam);
        // 补充构建表
        // 1.基类都需要作为基础字段，非基类，如果属性设置为基础字段，也需要补充
//        autoTable(checkinParam.getModelCode());
        return flag;
    }

    /**
     * 生成表
     * @param modelCode
     */
    private void autoTable(String modelCode) {

        List<CfgTableAttributeDto> tableAttributes = new ArrayList<>();
        // 获取对象以及属性信息
        CfgObjectVo objectVo = CfgObject.buildWithModelCode(modelCode)
                .populateBaseInfoByModelCode()
                .populateAttributes().build();
        CfgTableDto tableDto = null;
        if (StringUtil.isNotBlank(objectVo.getTableBid())){
            CfgTableVo tableVo = cfgTableDomainService.getByBid(objectVo.getTableBid());
            tableDto = CfgTableConverter.INSTANCE.vo2dto(tableVo);
        }
        // 如果没有生成表，则需要生成
        if (tableDto == null){
            tableDto = new CfgTableDto();
            tableDto.setBid(objectVo.getTableBid())
                    .setComment(objectVo.getName())
                    .setLogicTableName(objectVo.getModelCode());
        }
        List<CfgObjectAttributeVo> objectAttributes = objectVo.getAttrList();
        // 遍历对象属性信息，填充于表属性中
        objectAttributes.stream().filter(cfgObjectAttributeVo ->
                // 是基类或者基础属性
                ObjectCodeUtils.isBaseModel(cfgObjectAttributeVo.getModelCode())
                        || DataBaseConstant.BYTE_TRUE.equals(cfgObjectAttributeVo.getBaseAttr())
        ).forEach(objectAttributeVo ->
                tableAttributes.add(
                        CfgTableAttributeDto.of().setColumnName(objectAttributeVo.getDbKey())
                                .setProperty(objectAttributeVo.getCode())
                                .setComment(
                                        objectAttributeVo.getName() +
                                                ":" + objectAttributeVo.getDescription()
                                ).setType(objectAttributeVo.getDataType())
                )
        );
        // 先移除属性
        cfgTableDomainService.logicalDeleteAttributeByTableBid(tableDto.getBid());
        // 再重新存储
        cfgTableDomainService.saveOrUpdateTableAndAttribute(tableDto);
    }

    @Override
    public CfgObjectVo addObjectAndAttr(ObjectAndAttrAddParam objectAndAttrAddParam) {
        return CfgObject.buildWithAddOrUpdate(objectAndAttrAddParam.getObjectAddParam())
                .setAttrList(objectAndAttrAddParam.getAttrList())
                .populateInfoByParentCode(objectAndAttrAddParam.getObjectAddParam().getParentCode())
                .populateBaseModel()
                .checkObjectNameCannotBeSame()
                .addObjectAndAttr();
    }

    @Override
    public CfgObjectVo editObjectAndAttr(CheckinParam checkinParam) {
        if (StringUtil.isBlank(checkinParam.getModelCode())) {
            String modelCode = CfgObject.buildWithBid(checkinParam.getBid()).toModelCode();
            checkinParam.setModelCode(modelCode);
        }
        return CfgObject.buildWithCheckin(checkinParam)
                .checkObjectNameCannotBeSameWithCheckin()
                .editObjectAndAttr(checkinParam)
                .build();
    }

    /**
     * 上面为基础接口
     * ==================优--雅--的--分--割--线==================
     * 下面为扩展接口
     */

    @Override
    public List<CfgObjectVo> list() {
        return CfgObjectRoot.build()
                .findAll()
                .conversionToVO();
    }

    @Override
    public List<CfgObjectVo> listChildrenByModelCode(String modelCode) {
        return CfgObjectRoot.build()
                .findChildrenListByModelCode(modelCode)
                .conversionToVO();
    }

    @Override
    public List<CfgObjectVo> listByModelCodes(List<String> modelCodeList) {
        return CfgObjectRoot.build().listByModelCodes(modelCodeList);
    }

    @Override
    public List<CfgObjectVo> listByNames(List<String> nameList) {
        return CfgObjectRoot.build()
                .findListByNameList(nameList);
    }

    @Override
    public List<CfgObjectVo> listLikeName(String name) {
        return CfgObjectRoot.build()
                .findLikeName(name);
    }

    @Override
    public CfgObjectVo getObjectAndAttribute(String bid) {
        return CfgObject.buildWithBid(bid)
                .populateBaseInfo()
                .populateLockInfo()
                .populateAttributes()
//                .populateSupperAttributes()
                .build();
    }

    @Override
    public CfgObjectVo getObjectAndAttributeByModelCode(String modelCode) {
        return CfgObject.buildWithModelCode(modelCode)
                .populateBaseInfoByModelCode()
                .populateLockInfo()
                .populateAttributes()
                .build();
    }

    @Override
    public CfgObjectVo get(String bid) {
        return CfgObject.buildWithBid(bid)
                .populateBaseInfo()
                .populateLockInfo()
                .build();
    }

    @Override
    public CfgObjectVo getByModelCode(String modelCode) {
        return CfgObject.buildWithModelCode(modelCode)
                .populateBaseInfoByModelCode()
                .populateLockInfo()
                .build();
    }

    @Override
    public List<CfgObjectAttributeVo> listAttrsByModelCode(String modelCode) {
        return CfgObjectAttrRoot.build().findByModelCode(modelCode);
    }

    @Override
    public Map<String, List<CfgObjectAttributeVo>> listAttributesByModelCodes(List<String> modelCodeList) {
        return CfgObjectAttrRoot.build().findAttrsByModelCodeList(modelCodeList);
    }

    /**
     * 上面入参为modelCode的接口
     * ==================优--雅--的--分--割--线==================
     * 下面入参为bid的接口
     */

    @Override
    public List<String> findParentBidListByBid(String bid) {
        String modelCode = CfgObject.buildWithBid(bid).toModelCode();
        return CfgObjectRoot.build().findParentBidListByModelCode(modelCode);
    }

    @Override
    public List<CfgObjectVo> findChildrenListByBid(String bid) {
        String modelCode = CfgObject.buildWithBid(bid).toModelCode();
        return CfgObjectRoot.build()
                .findChildrenListByModelCode(modelCode)
                .conversionToVO();
    }

    @Override
    public CfgObjectAttributeVo findAttrByBid(AttrFindParam attrFindParam) {
        String modelCode = CfgObject.buildWithBid(attrFindParam.getObjectBid()).toModelCode();
        CfgObjectAttributePo po = cfgObjectAttributeRepository.findAttrByModelCodeAndBid(modelCode, attrFindParam.getBid());
        return BeanUtil.copy(po, CfgObjectAttributeVo.class);
    }

    @Override
    public List<CfgObjectAttributeVo> findAttrsByBid(String bid) {
        String modelCode = CfgObject.buildWithBid(bid).toModelCode();
        return CfgObjectAttrRoot.build().findByModelCode(modelCode);
    }

    @Override
    public List<CfgObjectAttributeVo> findChildrenAttrsByObjectBid(String objBid) {
        String modelCode = CfgObject.buildWithBid(objBid).toModelCode();
        return CfgObjectAttrRoot.build().findChildrenByModelCode(modelCode);
    }

    @Override
    public CfgObjectVo getOneJustAttrByBid(String bid) {
        return getObjectAndAttribute(bid);
    }

    @Override
    public List<CfgObjectVo> listByBids(List<String> bidList) {
        List<String> modelCodeList = cfgObjectRepository.findListByObjectBidList(bidList)
                .stream().map(CfgObjectPo::getModelCode).collect(Collectors.toList());
        return CfgObjectRoot.build().listByModelCodes(modelCodeList);
    }

    @Override
    public Map<String, List<CfgObjectAttributeVo>> findAttrsByObjectBidList(List<String> objectBidList) {
        List<CfgObjectPo> poList = cfgObjectRepository.findListByObjectBidList(objectBidList);
        List<String> modelCodeList = poList.stream().map(CfgObjectPo::getModelCode).collect(Collectors.toList());

        Map<String, List<CfgObjectAttributeVo>> modelCodeAndAttrListMap = CfgObjectAttrRoot.build()
                .findAttrsByModelCodeList(modelCodeList);

        //modelCode 转 objBid
        Map<String, String> modelCodeAndObjBidMap = poList.stream()
                .collect(Collectors.toMap(CfgObjectPo::getModelCode, CfgObjectPo::getBid));
        Map<String, List<CfgObjectAttributeVo>> result = Maps.newHashMap();
        modelCodeAndAttrListMap.forEach((modelCode, attrList) -> result.put(modelCodeAndObjBidMap.get(modelCode), attrList));

        return result;
    }

    @Override
    public List<CfgObjectVo> listByBaseModel(String baseModel) {
        return CfgObjectRoot.build().listByBaseModel(baseModel);
    }

    @Override
    public CfgObjectVo saveOrUpdate(ObjectAddParam objectAddParam) {
        if (StringUtil.isBlank(objectAddParam.getBid())) {
            return add(objectAddParam);
        } else {
            return update(objectAddParam);
        }
    }

    private CfgObjectVo update(ObjectAddParam objectAddParam) {
        return CfgObject.buildWithAddOrUpdate(objectAddParam)
                .update();
    }

    @Override
    public CfgObjectVo getOneHistoryByBidAndVersion(String bid, Integer version) {
        String modelCode = CfgObject.buildWithBid(bid).toModelCode();
        ObjectHistoryGetOneParam objectHistoryGetOneParam = ObjectHistoryGetOneParam.of().setModelCode(modelCode).setVersion(version);
        return CfgObjectHistory.buildWithGetOne(objectHistoryGetOneParam)
                .populateBaseInfo()
                .populateAttrList()
                .build();
    }

    @Override
    public CfgObjectVo getObjectAndAttributeOrDraft(String bid) {
        // 1.判断是否获取草稿数据 存在草稿（modelCode+工号）
        CfgObjectVo cfgObjectVo = readDraft(bid);
        if (cfgObjectVo != null) {
            return cfgObjectVo;
        }
        return getObjectAndAttribute(bid);
    }

    @Override
    public List<Object> getExportExcelData(Object param) {
        List<String> heardList = new ArrayList<>();
        Arrays.asList("对象名称","对象编码","基类编码","分类","状态").forEach(heardList::add);
        List<CfgObjectPo> list = cfgObjectRepository.findAll();
        List<List<Object>> dataList = new ArrayList<>();
        Map<String,String> typeMap = new HashMap<>();
        typeMap.put("RELATION","关系对象");
        typeMap.put("NORMAL","常规对象");
        typeMap.put("VERSION","版本对象");
        Map<Integer,String> enableMap = new HashMap<>();
        enableMap.put(0,"未启用");
        enableMap.put(1,"启用");
        enableMap.put(2,"禁用");
        for(CfgObjectPo cfgObjectPo:list){
            if(StringUtil.isBlank(cfgObjectPo.getType())){
                continue;
            }
            List<Object> data = new ArrayList<>();
            data.add(cfgObjectPo.getName());
            data.add(cfgObjectPo.getModelCode());
            data.add(cfgObjectPo.getBaseModel());
            data.add(typeMap.get(cfgObjectPo.getType()));
            data.add(enableMap.get(cfgObjectPo.getEnableFlag()));
            dataList.add(data);
        }
        List<Object> result = new ArrayList<>();
        result.add(heardList);
        result.add(dataList);
        return result;
    }

    @Override
    public boolean importData(List<Map<Integer,Object>> dataList, ImportDto importDto) {
        return false;
    }
}
