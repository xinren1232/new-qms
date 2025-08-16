package com.transcend.plm.configcenter.objectrelation.infrastructure.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectAddParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateNodeQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.qo.CfgLifeCycleTemplateObjRelQo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.CfgLifeCycleTemplateRepository;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateObjRelPo;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.object.infrastructure.common.constant.ObjectModelConstants;
import com.transcend.plm.configcenter.object.infrastructure.common.constant.RelationObjectConstants;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.mapper.CfgObjectMapper;
import com.transcend.plm.configcenter.objectrelation.converter.CfgObjectRelationAttrConverter;
import com.transcend.plm.configcenter.objectrelation.converter.CfgObjectRelationConverter;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationAttrDto;
import com.transcend.plm.configcenter.api.model.objectrelation.dto.CfgObjectRelationDto;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.CfgObjectRelationQo;
import com.transcend.plm.configcenter.api.model.objectrelation.qo.ObjectRelationRuleQo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationAttrVo;
import com.transcend.plm.configcenter.api.model.objectrelation.vo.CfgObjectRelationVo;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.po.CfgObjectRelationAttrPo;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.po.CfgObjectRelationPo;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.service.CfgObjectRelationAttrService;
import com.transcend.plm.configcenter.objectrelation.infrastructure.repository.service.CfgObjectRelationService;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象关系资源库
 *
 * @author yuanghu.huang
 * @version V1.0.0
 * @date 2023/3/1
 * @since 1.0
 */
@Slf4j
@Repository
public class ObjectRelationRepository {
    @Resource
    private CfgObjectRelationService cfgObjectRelationService;
    @Resource
    private CfgObjectRelationAttrService cfgObjectRelationAttrService;
    @Resource
    private CfgLifeCycleTemplateRepository cfgLifeCycleTemplateRepository;

    @Resource
    private ICfgObjectAppService cfgObjectAppService;

    @Resource
    private CfgObjectMapper cfgObjectMapper;

    @Transactional(rollbackFor = Exception.class)
    public boolean add(CfgObjectRelationDto cfgObjectRelationDto){
        CfgObjectRelationPo cfgObjectRelation = CfgObjectRelationConverter.INSTANCE.dto2po(cfgObjectRelationDto);
        cfgObjectRelation.setBid(SnowflakeIdWorker.nextIdStr());
        List<CfgObjectRelationAttrDto> cfgObjectRelationAttrDtoList = cfgObjectRelationDto.getRelationAttr();
        if(!CollectionUtils.isEmpty(cfgObjectRelationAttrDtoList)){
            List<CfgObjectRelationAttrPo> cfgObjectRelationAttrs = CfgObjectRelationAttrConverter.INSTANCE.dtos2pos(cfgObjectRelationAttrDtoList);
            for(CfgObjectRelationAttrPo cfgObjectRelationAttr:cfgObjectRelationAttrs){
                cfgObjectRelationAttr.setRelationBid(cfgObjectRelation.getBid());
            }
            cfgObjectRelationAttrService.saveBatch(cfgObjectRelationAttrs);
        }
        //对象表新增关系数据
        CfgObjectVo cfgObjectVo = createObject(cfgObjectRelation);
        cfgObjectRelation.setModelCode(cfgObjectVo.getModelCode());
        return cfgObjectRelationService.save(cfgObjectRelation);
    }

    private CfgObjectVo createObject(CfgObjectRelationPo cfgObjectRelation){
        ObjectAddParam objectAddParam = new ObjectAddParam();
        objectAddParam.setBid(cfgObjectRelation.getBid());
        objectAddParam.setName(cfgObjectRelation.getName());
        objectAddParam.setParentCode(ObjectModelConstants.ZERO);
        objectAddParam.setDescription(cfgObjectRelation.getDescription());
        objectAddParam.setType(ObjectTypeEnum.RELATION.getCode());
        CfgObjectVo cfgObjectVo = cfgObjectAppService.add(objectAddParam);
        return cfgObjectVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean edit(CfgObjectRelationDto cfgObjectRelationDto){
        CfgObjectRelationPo cfgObjectRelation = CfgObjectRelationConverter.INSTANCE.dto2po(cfgObjectRelationDto);
        List<CfgObjectRelationAttrDto> cfgObjectRelationAttrDtoList = cfgObjectRelationDto.getRelationAttr();
        cfgObjectRelationAttrService.remove(Wrappers.<CfgObjectRelationAttrPo> lambdaQuery().eq(CfgObjectRelationAttrPo::getRelationBid,cfgObjectRelation.getBid()).eq(CfgObjectRelationAttrPo::getDeleteFlag,0).lt(CfgObjectRelationAttrPo::getCreatedTime,new Date()));
        if(!CollectionUtils.isEmpty(cfgObjectRelationAttrDtoList)){
            List<CfgObjectRelationAttrPo> cfgObjectRelationAttrs = CfgObjectRelationAttrConverter.INSTANCE.dtos2pos(cfgObjectRelationAttrDtoList);
            for(CfgObjectRelationAttrPo cfgObjectRelationAttr:cfgObjectRelationAttrs){
                cfgObjectRelationAttr.setRelationBid(cfgObjectRelation.getBid());
            }
            cfgObjectRelationAttrService.saveBatch(cfgObjectRelationAttrs);
        }

        return cfgObjectRelationService.updateByBid(cfgObjectRelationDto);
    }

    public boolean changeEnableFlag(String bid, Integer enableFlag) {
        return cfgObjectRelationService.changeEnableFlag(bid,enableFlag);
    }

    public List<CfgObjectRelationVo> list(CfgObjectRelationQo cfgObjectRelationQo){
        return cfgObjectRelationService.list(cfgObjectRelationQo);
    }

    public PagedResult<CfgObjectRelationVo> page(BaseRequest<CfgObjectRelationQo> pageQo) {
        PagedResult<CfgObjectRelationVo> pagedResult = cfgObjectRelationService.page(pageQo);
        List<CfgObjectRelationVo> cfgObjectRelationVos = pagedResult.getData();
        if(!CollectionUtils.isEmpty(cfgObjectRelationVos)){
            setObjName(cfgObjectRelationVos);
            List<String> bids = new ArrayList<>();
            for(CfgObjectRelationVo cfgObjectRelationVo:cfgObjectRelationVos){
                bids.add(cfgObjectRelationVo.getBid());
            }
            List<CfgObjectRelationAttrPo> cfgObjectRelationAttrs = cfgObjectRelationAttrService.list(Wrappers.<CfgObjectRelationAttrPo> lambdaQuery().in(CfgObjectRelationAttrPo::getRelationBid,bids));
            if(!CollectionUtils.isEmpty(cfgObjectRelationAttrs)){
                List<CfgObjectRelationAttrVo> cfgObjectRelationAttrVos = CfgObjectRelationAttrConverter.INSTANCE.pos2vos(cfgObjectRelationAttrs);
                Map<String,List<CfgObjectRelationAttrVo>> attrVoMap = cfgObjectRelationAttrVos.stream().collect(Collectors.groupingBy(CfgObjectRelationAttrVo::getRelationBid));
                for(CfgObjectRelationVo cfgObjectRelationVo:cfgObjectRelationVos){
                    cfgObjectRelationVo.setRelationAttr(attrVoMap.get(cfgObjectRelationVo.getBid()));
                }
            }
        }
        return pagedResult;
    }

    private void setObjName(List<CfgObjectRelationVo> cfgObjectRelationVos){
        List<String> modelCodeList = new ArrayList<>();
        for(CfgObjectRelationVo cfgObjectRelationVo:cfgObjectRelationVos){
            if(!modelCodeList.contains(cfgObjectRelationVo.getSourceModelCode())){
                modelCodeList.add(cfgObjectRelationVo.getSourceModelCode());
            }
            if(!modelCodeList.contains(cfgObjectRelationVo.getTargetModelCode())){
                modelCodeList.add(cfgObjectRelationVo.getTargetModelCode());
            }
        }
        List<CfgObjectPo> objectPOS = cfgObjectMapper.findListByModelCodeList(modelCodeList);
        Map<String,String> objectMap = objectPOS.stream().collect(Collectors.toMap(CfgObjectPo::getModelCode, CfgObjectPo::getName));
        for(CfgObjectRelationVo cfgObjectRelationVo:cfgObjectRelationVos){
            cfgObjectRelationVo.setSourceObjName(objectMap.get(cfgObjectRelationVo.getSourceModelCode()));
            cfgObjectRelationVo.setTargetObjName(objectMap.get(cfgObjectRelationVo.getTargetModelCode()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String bid) {
        CfgObjectRelationPo cfgObjectRelation = cfgObjectRelationService.getOne(Wrappers.<CfgObjectRelationPo> lambdaQuery().eq(CfgObjectRelationPo::getBid,bid));
        Assert.isTrue((cfgObjectRelation != null && cfgObjectRelation.getEnableFlag() == 0),String.format("数据不存在或关系【%s】不是未启用状态，无法删除！", cfgObjectRelation.getName()));
        Map<String,Object> attrMap = new HashMap<String,Object>();
        attrMap.put("relation_bid",cfgObjectRelation.getBid());
        cfgObjectRelationAttrService.removeByMap(attrMap);
        Map<String,Object> paraMap = new HashMap<String,Object>();
        paraMap.put("bid",bid);
        return cfgObjectRelationService.removeByMap(paraMap);
    }


    public String getRelationRuleRes(ObjectRelationRuleQo objectRelationRuleQo){
        CfgObjectRelationQo cfgObjectRelationQo = new CfgObjectRelationQo();
        cfgObjectRelationQo.setModelCode(objectRelationRuleQo.getRelationModelCode());
        List<CfgObjectRelationVo>  cfgObjectRelationVos = list(cfgObjectRelationQo);
        if(CollectionUtils.isEmpty(cfgObjectRelationVos)){
            throw new BusinessException(objectRelationRuleQo.getTargetModelCode()+"关系不存在");
        }
        //查询关系表
        CfgObjectRelationVo cfgObjectRelationVo = cfgObjectRelationVos.get(0);
        //查询前生命周期关系
        String fromRel = getLifeCycleCodeRel(cfgObjectRelationVo,objectRelationRuleQo.getTargetModelCode(),objectRelationRuleQo.getLcTemplateBid(),objectRelationRuleQo.getVersion(),objectRelationRuleQo.getFromLifeCycleCode());
        if(StringUtils.isEmpty(objectRelationRuleQo.getToLifeCycleCode())){
            //直接返回当前的状态
            return fromRel;
        }
        //查询后生命周期关系
        String toRel = getLifeCycleCodeRel(cfgObjectRelationVo,objectRelationRuleQo.getTargetModelCode(),objectRelationRuleQo.getLcTemplateBid(),objectRelationRuleQo.getVersion(),objectRelationRuleQo.getToLifeCycleCode());
        return fromRel+"2"+toRel;
    }

    private String getLifeCycleCodeRel(CfgObjectRelationVo cfgObjectRelationVo,String modelCode,String lcTemplateBid,String version,String lifeCycleCode){
        //判断关系是否是强固定或者强浮动
        if(RelationObjectConstants.STRONG_FLOAT.equals(cfgObjectRelationVo.getBehavior().toUpperCase())){
            return RelationObjectConstants.FLOAT;
        }
        if(RelationObjectConstants.STRONG_FIXED.equals(cfgObjectRelationVo.getBehavior().toUpperCase())){
            return RelationObjectConstants.FIXED;
        }
        //查询生命周期模板节点表
        String res = "";
        CfgLifeCycleTemplateNodeQo cfgLifeCycleTemplateNodeQo = new CfgLifeCycleTemplateNodeQo();
        cfgLifeCycleTemplateNodeQo.setTemplateBid(lcTemplateBid);
        cfgLifeCycleTemplateNodeQo.setVersion(version);
        cfgLifeCycleTemplateNodeQo.setLifeCycleCode(lifeCycleCode);
        CfgLifeCycleTemplateNodePo cfgLifeCycleTemplateNodePo = cfgLifeCycleTemplateRepository.getCfgLifeCycleTemplateNode(cfgLifeCycleTemplateNodeQo);
        // 判断是否有作用于全局的关系行为
        if(cfgLifeCycleTemplateNodePo.getBehaviorScope() == 1){
            res =  cfgLifeCycleTemplateNodePo.getBehavior();
        }else{
            //查询对象
            CfgLifeCycleTemplateObjRelQo cfgLifeCycleTemplateObjRelQo = new CfgLifeCycleTemplateObjRelQo();
            cfgLifeCycleTemplateObjRelQo.setTemplateBid(lcTemplateBid);
            cfgLifeCycleTemplateObjRelQo.setVersion(version);
            cfgLifeCycleTemplateObjRelQo.setLifeCycleCode(lifeCycleCode);
            cfgLifeCycleTemplateObjRelQo.setModelCode(modelCode);
            CfgLifeCycleTemplateObjRelPo cfgLifeCycleTemplateObjRelPo = cfgLifeCycleTemplateRepository.getCfgLifeCycleTemplateObjRel(cfgLifeCycleTemplateObjRelQo);
            if(cfgLifeCycleTemplateObjRelPo != null){
                res = cfgLifeCycleTemplateObjRelPo.getTargetObjRel();
            }
        }
        if(StringUtils.isEmpty(res)){
            res = cfgObjectRelationVo.getBehavior();
        }
        res = getRelationChange(res);
        return res;
    }
    //强 软 转换
    private String getRelationChange(String rela){
        String res = rela;
        if(RelationObjectConstants.STRONG_FLOAT.equals(rela.toUpperCase())){
            res = RelationObjectConstants.FLOAT;
        }
        if(RelationObjectConstants.STRONG_FIXED.equals(rela.toUpperCase())){
            res = RelationObjectConstants.FIXED;
        }
        if(RelationObjectConstants.SOFT_FLOAT.equals(rela.toUpperCase())){
            res = RelationObjectConstants.FLOAT;
        }
        if(RelationObjectConstants.SOFT_FIXED.equals(rela.toUpperCase())){
            res = RelationObjectConstants.FIXED;
        }
        return res.toUpperCase();
    }

    /**
     * 根据模型编码查询关系表
     *
     * @param modelCodes
     * @return
     */
    public List<CfgObjectRelationVo> listRelationTab(List<String> modelCodes) {
        List<CfgObjectRelationPo> relationPoList = cfgObjectRelationService.list(Wrappers.<CfgObjectRelationPo>lambdaQuery()
                //2025年3月31日 需要展示父级关系tab
                .and(query -> query.in(CfgObjectRelationPo::getTargetModelCode, modelCodes)
                        .or().in(CfgObjectRelationPo::getSourceModelCode, modelCodes))

                .eq(CfgObjectRelationPo::getEnableFlag, 1).eq(CfgObjectRelationPo::getDeleteFlag, 0));
        //用CfgObjectRelationConverter转换成CfgObjectRelationVo
        return CfgObjectRelationConverter.INSTANCE.pos2vos(relationPoList);
    }

    /**
     * 根据模型目标对象编码查询关系表
     *
     * @param modelCodes
     * @return
     */
    public List<CfgObjectRelationVo> listRelationByTargetModelCode(List<String> modelCodes) {
        List<CfgObjectRelationPo> relationPoList = cfgObjectRelationService.list(Wrappers.<CfgObjectRelationPo>lambdaQuery()
                .in(CfgObjectRelationPo::getTargetModelCode, modelCodes).eq(CfgObjectRelationPo::getEnableFlag,1).eq(CfgObjectRelationPo::getDeleteFlag,0));
        //用CfgObjectRelationConverter转换成CfgObjectRelationVo
        return CfgObjectRelationConverter.INSTANCE.pos2vos(relationPoList);
    }

    /**
     * 根据模型源对象编码查询关系表
     *
     * @param modelCodes
     * @return
     */
    public List<CfgObjectRelationVo> listRelationBySourceModelCodes(List<String> modelCodes) {
        List<CfgObjectRelationPo> relationPoList = cfgObjectRelationService.list(Wrappers.<CfgObjectRelationPo>lambdaQuery()
                .in(CfgObjectRelationPo::getSourceModelCode, modelCodes).eq(CfgObjectRelationPo::getEnableFlag,1).eq(CfgObjectRelationPo::getDeleteFlag,0));
        //用CfgObjectRelationConverter转换成CfgObjectRelationVo
        return CfgObjectRelationConverter.INSTANCE.pos2vos(relationPoList);
    }

    /**
     * 根据模型目标对象编码查询关系表
     *
     * @param sourceModelCodes
     * @return
     */
    public List<CfgObjectRelationVo> listRelationBySTModelCode(List<String> sourceModelCodes,List<String> targetModelCodes) {
        List<CfgObjectRelationPo> relationPoList = cfgObjectRelationService.list(Wrappers.<CfgObjectRelationPo>lambdaQuery()
                .in(CfgObjectRelationPo::getTargetModelCode, targetModelCodes).in(CfgObjectRelationPo::getSourceModelCode,sourceModelCodes).eq(CfgObjectRelationPo::getEnableFlag,1).eq(CfgObjectRelationPo::getDeleteFlag,0));
        //用CfgObjectRelationConverter转换成CfgObjectRelationVo
        return CfgObjectRelationConverter.INSTANCE.pos2vos(relationPoList);
    }

    public CfgObjectRelationVo getByModelCode(String modelCode) {
        CfgObjectRelationPo objectRelationPo = cfgObjectRelationService.getOne(Wrappers.<CfgObjectRelationPo>lambdaQuery().eq(CfgObjectRelationPo::getModelCode, modelCode));
        return CfgObjectRelationConverter.INSTANCE.po2vo(objectRelationPo);
    }
}