package com.transcend.plm.configcenter.object.application.service.impl;

import com.github.pagehelper.page.PageMethod;
import com.google.common.collect.Lists;
import com.transcend.framework.core.constantenum.ObjectTypeEnum;
import com.transcend.plm.configcenter.common.constant.CommonConst;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.pool.CommonThreadPool;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectAppService;
import com.transcend.plm.configcenter.object.application.service.ICfgRelationObjectConfigAppService;
import com.transcend.plm.configcenter.object.infrastructure.common.constant.RelationObjectConstants;
import com.transcend.plm.configcenter.object.infrastructure.po.ObjectRelationPO;
import com.transcend.plm.configcenter.object.infrastructure.repository.RelationObjectConfigRepository;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectAddParam;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectRelationAttrDTO;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectRelationDTO;
import com.transcend.plm.configcenter.api.model.object.qo.ObjectRelationQO;
import com.transcend.plm.configcenter.api.model.object.vo.*;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;
import com.transsion.framework.common.StringUtil;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * relationFlagectConfigAppServiceImpl
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/19 11:12
 */
@Service
@Slf4j
public class RelationObjectConfigAppServiceImpl implements ICfgRelationObjectConfigAppService {


    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RelationObjectConfigRepository relationFlagectConfigRepository;

    @Resource
    private ICfgObjectAppService objectModelAppService;
//
//    @Resource
//    private LifeCycTemplateRemoteService objectLifecycleApi;

    @Override
    public List<ObjectRelationVO> add(List<ObjectRelationDTO> relationList) {
        if (CollectionUtils.isEmpty(relationList)) {
            return new ArrayList<>();
        }
        validateField(relationList);
        checkrelationFlagectRepeat(relationList);
        checkInstanceNumber(relationList);

        transactionTemplate.execute(transactionStatus -> {
            createBindObject(relationList);
            relationFlagectConfigRepository.bachAdd(BeanUtil.copy(relationList, ObjectRelationPO.class));
            relationList.forEach(e -> {
                relationFlagectConfigRepository.removerAttr(Arrays.asList(e.getBid()));
                List<ObjectRelationAttrDTO> objectRelationAttrDTOS = e.getRelationAttr();
                if (CollectionUtil.isNotEmpty(objectRelationAttrDTOS)) {
                    objectRelationAttrDTOS.forEach(k -> {
                        k.setBid(SnowflakeIdWorker.nextIdStr());
                        k.setRelationBid(e.getBid());
                    });
                }
                relationFlagectConfigRepository.batchInsertAttr(objectRelationAttrDTOS);
            });
            return true;
        });

        return BeanUtil.copy(relationList, ObjectRelationVO.class);
    }

    @Override
    public Integer edit(List<ObjectRelationDTO> relationList) {
        if (CollectionUtil.isEmpty(relationList)) {
            return 0;
        }
        validateField(relationList);
        checkrelationFlagectRepeat(relationList);
        checkInstanceNumber(relationList);
        Map<String, Integer> bidAndStateMap = getRelationState(relationList);
        relationList.forEach(relation -> {
            if (CommonConst.ENABLE_FLAG_FORBIDDEN.equals(relation.getEnableFlag()) && !CommonConst.ENABLE_FLAG_FORBIDDEN.equals(bidAndStateMap.get(relation.getBid()))) {
                String msg = String.format("不能将关系【%s】的状态改为未启用", relation.getName());
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), msg);
            }
        });
        Integer count = 0;
        List<ObjectRelationPO> pos = BeanUtil.copy(relationList, ObjectRelationPO.class);
        pos.forEach(po -> po.setUpdatedBy(SsoHelper.getJobNumber()));
        return transactionTemplate.execute(transactionStatus -> {
            relationFlagectConfigRepository.edit(pos);
            relationList.forEach(e -> {
                relationFlagectConfigRepository.removerAttr(Arrays.asList(e.getBid()));
                List<ObjectRelationAttrDTO> objectRelationAttrDTOS = e.getRelationAttr();
                if (CollectionUtil.isNotEmpty(objectRelationAttrDTOS)) {
                    objectRelationAttrDTOS.forEach(k -> {
                        k.setBid(SnowflakeIdWorker.nextIdStr());
                        k.setRelationBid(e.getBid());
                    });
                }
                relationFlagectConfigRepository.batchInsertAttr(objectRelationAttrDTOS);
            });
            return relationFlagectConfigRepository.edit(pos);
        });
    }

    @Override
    public List<ObjectRelationVO> find(ObjectRelationQO qo) {
        if (qo == null) {
            return new ArrayList<>();
        }
        List<ObjectRelationVO> vos = BeanUtil.copy(relationFlagectConfigRepository.find(qo), ObjectRelationVO.class);
        setObjName(vos);
        setRelationAttr(vos);
        return vos;
    }

    @Override
    public PagedResult<ObjectRelationVO> query(BaseRequest<ObjectRelationQO> qo) {
        ObjectRelationQO searchParams = qo.getParam();
        if (searchParams == null) {
            throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), "查询条件为空！");
        }
        com.github.pagehelper.Page<ObjectRelationVO> page = PageMethod.startPage(qo.getCurrent(), qo.getSize());
        List<ObjectRelationPO> pageList = relationFlagectConfigRepository.query(searchParams);
        List<ObjectRelationVO> dataList = BeanUtil.copy(pageList, ObjectRelationVO.class);
        setObjName(dataList);
        setRelationAttr(dataList);
        return PageResultTools.create(page, dataList);
//        return pageVO;
    }

    @Override
    public Integer delete(List<ObjectRelationDTO> relationList) {
        if (CollectionUtil.isEmpty(relationList)) {
            return 1;
        }
        Map<String, Integer> relationStateMap = getRelationState(relationList);
        relationList.forEach(relation -> {
            if (!CommonConst.ENABLE_FLAG_FORBIDDEN.equals(relationStateMap.get(relation.getBid()))) {
                String msg = String.format("关系【%s】不是未启用状态，无法删除！", relation.getName());
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), msg);
            }
        });

        List<ObjectRelationPO> objectRelationPOS = BeanUtil.copy(relationList, ObjectRelationPO.class);
        objectRelationPOS.forEach(po -> po.setUpdatedBy(SsoHelper.getJobNumber()));
        return transactionTemplate.execute(transactionStatus -> {
            relationFlagectConfigRepository.removerAttr(objectRelationPOS.stream().map(ObjectRelationPO::getBid).collect(Collectors.toList()));
            return relationFlagectConfigRepository.delete(objectRelationPOS);
        });
    }

    @Override
    public List<ObjectRelationApplyVO> getRelationApplyAttr(String sourceModeCode) {

        //查询该对象配置的关系
        ObjectRelationQO qo = new ObjectRelationQO()
                .setSourceModel(sourceModeCode);
        List<ObjectRelationPO> relationPOList = Optional
                .ofNullable(relationFlagectConfigRepository.find(qo))
                .orElse(new ArrayList<>());
        if (CollectionUtil.isEmpty(relationPOList)) {
            return new ArrayList<>();
        }

        //只获取启用状态的关系
        relationPOList = relationPOList.stream().filter(relation -> "enable".equals(relation.getEnableFlag()) && StringUtil.isNotBlank(relation.getTargetModelCode())).collect(Collectors.toList());
        List<ObjectRelationApplyVO> objectRelationApplyVOList = new CopyOnWriteArrayList<>();
//        CountDownLatch countDownLatch = new CountDownLatch(relationPOList.size());
        for (ObjectRelationPO relationPO : relationPOList) {
//            ModelThreadPool.INSTANCES.getPool().execute(
//                    () ->{
//                        try {
            buildObjectQuery(objectRelationApplyVOList, relationPO);
//                        } finally {
//                            countDownLatch.countDown();
//                        }
//                    }
//            );
        }
//        try {
////            countDownLatch.await();
//        } catch (InterruptedException e) {
//            log.error("多线程执行查询关系失败", e);
//            Thread.currentThread().interrupt();
//        }
        return objectRelationApplyVOList.stream().sorted(Comparator.comparing(ObjectRelationApplyVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ObjectRelationApplyVO::getUpdatedTime, Comparator.reverseOrder())).collect(Collectors.toList());
    }

    @Override
    public List<ObjectRelationApplyVO> getRelationExtendInfo(ObjectRelationQO objectRelationQO) {
        //获取关系数据
        List<ObjectRelationVO> relationVOS = getRelation(objectRelationQO);
        if (CollectionUtil.isEmpty(relationVOS)) {
            return Lists.newArrayList();
        }
        List<ObjectRelationPO> relationPOList = CollectionUtils.copyList(relationVOS, ObjectRelationPO.class);
        List<ObjectRelationApplyVO> objectRelationApplyVOList = new CopyOnWriteArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(relationPOList.size());
        //循环获取关系和关系目标对象的属性
        for (ObjectRelationPO relationPO : relationPOList) {
            CommonThreadPool.INSTANCES.getPool().execute(
                    () -> {
                        try {
                            buildObjectQuery(objectRelationApplyVOList, relationPO);
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
            );
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("多线程执行查询关系失败", e);
            Thread.currentThread().interrupt();
        }
        return objectRelationApplyVOList.stream().sorted(Comparator.comparing(ObjectRelationApplyVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ObjectRelationApplyVO::getUpdatedTime, Comparator.reverseOrder())).collect(Collectors.toList());
    }

    @Override
    public List<ObjectRelationVO> getRelationContainExtend(String sourceObjectBid, String scope) {
        List<String> responseVO = objectModelAppService.findParentBidListByBid(sourceObjectBid);
        if (CollectionUtil.isEmpty(responseVO)) {
            throw new BusinessException("当前对象类型为空");
        }
        List<ObjectRelationPO> relationPOList = relationFlagectConfigRepository.findBySourceModelCodes(responseVO);
        if (CollectionUtil.isEmpty(relationPOList)) {
            return new ArrayList<>();
        }
        if (StringUtil.isBlank(scope) || !"ALL".equals(scope)) {
            //只获取启用状态的关系
            relationPOList = relationPOList.stream().filter(relation -> "enable".equals(relation.getEnableFlag()) && StringUtil.isNotBlank(relation.getTargetModelCode())).collect(Collectors.toList());
        }
        return CollectionUtils.copyList(relationPOList, ObjectRelationVO.class);
    }


    @Override
    public List<ObjectRelationVO> getRelation(ObjectRelationQO relationQO) {
//        //查询关系列表，包含了继承关系
//        List<ObjectRelationVO> list = getRelationContainExtend(relationQO.getSourceModel(), relationQO.getScope());
//        if (CollectionUtil.isEmpty(list)) {
//            return Lists.newArrayList();
//        }
//        if (StringUtil.isBlank(relationQO.getOldLifeCode())) {
//            return list;
//        }
//        LifeCycTemObjRelQO lifeCycTemObjRelQO = new LifeCycTemObjRelQO();
//        lifeCycTemObjRelQO.setTemplateId(relationQO.getTemplateId());
//        lifeCycTemObjRelQO.setVersion(relationQO.getTemplateVersion());
//        List<String> lifeenableFlagList = Lists.newArrayList();
//        if (StringUtils.isNotBlank(relationQO.getLifeCode()) && !relationQO.getLifeCode().equals(relationQO.getOldLifeCode())) {
//            lifeenableFlagList.add(relationQO.getLifeCode());
//        }
//        lifeenableFlagList.add(relationQO.getOldLifeCode());
//        lifeCycTemObjRelQO.setLifeCodeList(lifeenableFlagList);
//        //根据生命周期模板生命周期查询生命周期配置的关系数据
//        List<LifeCycTemObjRelVO> responseVO = objectLifecycleApi.getObjRel(lifeCycTemObjRelQO);
//        List<LifeCycTemObjRelVO> lifeCycTemObjRelVO = CheckResponseUtils.getResult(responseVO);
//        lifeCycTemObjRelVO.forEach(e -> {
//            boolean oldenableFlag = false;
//            if (relationQO.getOldLifeCode().equals(e.getLifeCycleCode())) {
//                oldenableFlag = true;
//            }
//            //处理关系优先级
//            dealRelation(list, e, oldenableFlag);
//        });
//        if ((StringUtil.isNotBlank(relationQO.getLifeCode()) && relationQO.getLifeCode().equals(relationQO.getOldLifeCode()) || StringUtils.isBlank(relationQO.getLifeCode()))) {
//            list.forEach(e -> e.setBehavior(e.getBeforeBehavior()));
//        }
//        return list;
        return null;
    }


    /**
     * 处理关系优先级
     *
     * @param list
     * @param lifeCycTemObjRelVO
     * @param option             是当前生命周期，还是历史
     * @return
     */
//    private List<ObjectRelationVO> dealRelation(List<ObjectRelationVO> list, LifeCycTemObjRelVO lifeCycTemObjRelVO, Boolean option) {
//        if (lifeCycTemObjRelVO.getBehaviorScope() == 1) {
//            if (StringUtil.isNotBlank(lifeCycTemObjRelVO.getBehavior())) {
//                list.forEach(e -> {
//                    if (option) {
//                        e.setBeforeBehavior(getBehavior(e.getBehavior(), lifeCycTemObjRelVO.getBehavior()));
//                    } else {
//                        e.setBehavior(getBehavior(e.getBehavior(), lifeCycTemObjRelVO.getBehavior()));
//                    }
//                });
//                return list;
//            } else {
//                list.forEach(e -> {
//                    if (option) {
//                        e.setBeforeBehavior(e.getBehavior());
//                    }
//                });
//                return list;
//            }
//        }
//        List<LifeCycTemObjRel> lifeCycTemObjRels = lifeCycTemObjRelVO.getObjectRelationList();
//        if (CollectionUtil.isNotEmpty(lifeCycTemObjRels)) {
//            Map<String, LifeCycTemObjRel> lifeCycTemObjRelMap = lifeCycTemObjRels.stream().collect(Collectors.toMap(LifeCycTemObjRel::getTargetObjBid, Function.identity(), (a1, a2) -> a1));
//            list.forEach(e -> {
//                LifeCycTemObjRel ref = lifeCycTemObjRelMap.get(e.getTargetModelCode());
//                if (ref != null) {
//                    if (option) {
//                        e.setBeforeBehavior(getBehavior(e.getBehavior(), ref.getTargetObjrel()));
//                    } else {
//                        e.setBehavior(getBehavior(e.getBehavior(), ref.getTargetObjrel()));
//                    }
//                } else {
//                    if (option) {
//                        e.setBeforeBehavior(e.getBehavior());
//                    }
//                }
//            });
//        } else {
//            if (option) {
//                list.forEach(e -> {
//                    e.setBeforeBehavior(e.getBehavior());
//                });
//            }
//        }
//        return list;
//    }


    /**
     * 获取关系优先级
     *
     * @param relationBehavior 关系管理配置行为
     * @param lifeBehavior     生命周期节点配置行为
     * @return 优先级
     */
    public String getBehavior(String relationBehavior, String lifeBehavior) {

        if (relationBehavior.startsWith("strong_") && lifeBehavior.startsWith("strong_")) {
            return lifeBehavior;
        } else if (relationBehavior.startsWith("strong_") || lifeBehavior.startsWith("strong_")) {
            return relationBehavior.startsWith("strong_") ? relationBehavior : lifeBehavior;
        } else {
            return lifeBehavior;
        }
    }

    private void buildObjectQuery(List<ObjectRelationApplyVO> objectRelationApplyVOList, ObjectRelationPO relationPO) {
        //查询源对象的rootBid
        CfgObjectVo sourceObjectModel = objectModelAppService.getOneJustAttrByBid(relationPO.getSourceModelCode());
        String sourceModel = Optional.ofNullable(sourceObjectModel).orElse(new CfgObjectVo()).getBaseModel();
        //查询最新版本的关联对象
        CfgObjectVo latestTargetObj = objectModelAppService.getOneJustAttrByBid(relationPO.getTargetModelCode());
        if (latestTargetObj == null) {
            throw new BusinessException("关联对象查询为空");
        }
        List<ObjectRelationAttrDTO> objectRelationAttrDTOS = relationFlagectConfigRepository.queryAttrList(ObjectRelationAttrDTO.builder().relationBid(relationPO.getBid()).build());

        Map<String, ObjectRelationAttrDTO> relationAttrDTOMap = objectRelationAttrDTOS.stream().collect(
                Collectors.toMap(k -> k.getSourceModel() + k.getInnerName(), Function.identity()));
        //关联对象属性只用配置了在关系中显示的属性
        List<CfgObjectAttributeVo> targetObjModelAttrList = Optional
                .ofNullable(latestTargetObj.getAttrList()).orElse(new ArrayList<>())
                .stream()
//                .filter(attr -> Boolean.TRUE.equals(attr.getShow()))
                .collect(Collectors.toList());
        List<ObjectAttrVO> targetObjAttrList = CollectionUtils.copyList(targetObjModelAttrList, ObjectAttrVO.class);
        setrelationFlagectAttr(RelationObjectConstants.objectRelationAttr.TARGET, targetObjAttrList, relationAttrDTOMap);

        //查询最新版本的关系对象
        CfgObjectVo latestrelationFlag = objectModelAppService.getOneJustAttrByBid(relationPO.getBid());
        //处理特殊属性
        if (latestrelationFlag != null && CollectionUtil.isNotEmpty(latestrelationFlag.getAttrList())) {
            setrelationFlagectAttr(RelationObjectConstants.objectRelationAttr.RELATION, CollectionUtils.copyList(latestrelationFlag.getAttrList(), ObjectAttrVO.class), relationAttrDTOMap);
        }

        List<CfgObjectAttributeVo> relationFlagModelAttrList = CollectionUtil.isEmpty(latestrelationFlag.getAttrList()) ? new ArrayList<>() : latestrelationFlag.getAttrList();
        List<ObjectAttrVO> relationFlagAttrList = CollectionUtils.copyList(relationFlagModelAttrList, ObjectAttrVO.class);

        targetObjAttrList = targetObjAttrList.stream().sorted(Comparator.comparing(ObjectAttrVO::getRealRelativeSort,
                Comparator.nullsLast(Integer::compareTo))).collect(Collectors.toList());
        relationFlagAttrList = relationFlagAttrList.stream().sorted(Comparator.comparing(ObjectAttrVO::getRealRelativeSort,
                Comparator.nullsLast(Integer::compareTo))).collect(Collectors.toList());
        ObjectRelationApplyVO relationApplyVO = ObjectRelationApplyVO
                .of()
                .setSourceModel(sourceModel)
                .setTargetModel(latestTargetObj.getBaseModel())
                .setTargetObjBid(relationPO.getTargetModelCode())
                .setTargetObjVersion(latestTargetObj.getVersion() + "")
                .setTargetObjName(latestTargetObj.getName())
                .setTargetObjAttrList(CollectionUtils.copyList(targetObjAttrList, ObjectAttrVO.class))
                .setRelationObjBid(relationPO.getBid())
                .setRelationObjVersion(latestrelationFlag.getVersion() + "")
                .setRelationObjName(latestrelationFlag.getName())
                .setRelationObjAttrList(relationFlagAttrList)
                .setBehavior(relationPO.getBehavior())
                .setType(relationPO.getType())
                .setMaxNumber(relationPO.getMaxNumber())
                .setMinNumber(relationPO.getMinNumber())
                .setHideTab(relationPO.getHideTab())
                .setTabName(relationPO.getTabName())
                .setTargetModelCode(latestTargetObj.getModelCode())
                .setRequired(relationPO.getRequired()).setSort(relationPO.getSort()).setUpdatedTime(relationPO.getUpdatedTime());

        objectRelationApplyVOList.add(relationApplyVO);
    }

    private void setrelationFlagectAttr(String target, List<ObjectAttrVO> list, Map<String, ObjectRelationAttrDTO> relationAttrDTOMap) {
        list.forEach(e -> {
            ObjectRelationAttrDTO attrDTO = relationAttrDTOMap.get(target + e.getInnerName());
            if (attrDTO != null) {
                e.setUseInQuery(attrDTO.getUseInQuery());
                e.setRealUseInView(attrDTO.getRealUseInView());
                e.setColumnWidth(attrDTO.getColumnWidth());
                e.setRealRelativeSort(attrDTO.getRealRelativeSort());
            } else {
                e.setRealUseInView(true);
            }
        });
    }

    //校验必填字段
    private void validateField(List<ObjectRelationDTO> relationList) {
        relationList.forEach(relation -> {
            if (StringUtil.isBlank(relation.getName()) || relation.getName().length() > 128) {
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), "关系名称名称长度必须大于0，小于128！");
            }
            if (StringUtil.isBlank(relation.getTabName()) || relation.getTabName().length() > 128) {
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), "tab名称长度必须大于0，小于128！");
            }
            if (StringUtil.isBlank(relation.getType())) {
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), "关系类型(仅新建、仅选取、两者皆可) 必填！");
            }
            if (StringUtil.isBlank(relation.getBehavior())) {
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), "关系行为(浮动、固定) 必填！");
            }
            if (StringUtil.isBlank(relation.getSourceModelCode()) && StringUtil.isBlank(relation.getTargetModelCode())) {
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), "源对象编码和关联对象编码不能同时为空");
            }
            if (relation.getDescription() != null && relation.getDescription().length() > 500) {
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), "描述信息最大长度为500");
            }
        });
    }

    //创建绑定的对象,创建的对象编码直接使用关系的编码，方便后期查找
    private void createBindObject(List<ObjectRelationDTO> relationList) {
        relationList.forEach(relation -> {
            ObjectAddParam objectAddParam = new ObjectAddParam();
            objectAddParam
                    .setBid(relation.getBid())
                    .setBaseModel(relation.getBid())
                    .setName(relation.getName())
                    //说明此对象在根节点
                    .setParentCode(RelationObjectConstants.ROOT_PARENT_BID)
                    .setType(ObjectTypeEnum.RELATION.getCode())
                    .setSort(123);
            objectModelAppService.add(objectAddParam);
        });

    }

    //校验关联关系是否已经存在
    private void checkrelationFlagectRepeat(List<ObjectRelationDTO> relationList) {

        //key:源对象编码+关联对象编码，value:关系bid
        Map<String, String> existRelationBidMapAndBid = Optional
                .ofNullable(
                        relationFlagectConfigRepository
                                .findByrelationFlagect(BeanUtil.copy(relationList, ObjectRelationPO.class))
                )
                .orElse(new ArrayList<>())
                .stream()
                .collect(Collectors
                        .toMap(obj -> obj.getSourceModelCode() + obj.getTargetModelCode(), ObjectRelationPO::getBid)
                );

        //如果已经存在相同的源对象编码+关联对象编码关联关系，且bid不相同，则报错
        relationList.forEach(relation -> {
            String key = relation.getSourceModelCode() + relation.getTargetModelCode();
            if (existRelationBidMapAndBid.containsKey(key) && !relation.getBid().equals(existRelationBidMapAndBid.get(key))) {
                String msg = String.format("存在重复的关联关系：源对象[%s],目标对象[%s]", relation.getSourceModelCode(), relation.getTargetModelCode());
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), msg);
            }
        });
    }

    //设置关联对象名称
    private void setObjName(List<ObjectRelationVO> vos) {
        if (CollectionUtil.isEmpty(vos)) {
            return;
        }
        List<String> objectPOS = new ArrayList<>();

        //查询最新版本对象
        vos.forEach(vo -> {
            if (StringUtil.isNotBlank(vo.getSourceModelCode())) {
                objectPOS.add(vo.getSourceModelCode());
            }
            if (StringUtil.isNotBlank(vo.getTargetModelCode())) {
                objectPOS.add(vo.getTargetModelCode());
            }
        });
        List<CfgObjectVo> objectModelList = objectModelAppService.listByBids(objectPOS);
        //key：对象Bid，value：对象名称
        Map<String, String> objectBidAndNameMap = Optional
                .ofNullable(objectModelList)
                .orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.toMap(CfgObjectVo::getBid, CfgObjectVo::getName));

        //设置源对象和关联对象的名称
        vos.forEach(vo -> {
            vo.setSourceObjName(objectBidAndNameMap.get(vo.getSourceModelCode()));
            vo.setTargetObjName(objectBidAndNameMap.get(vo.getTargetModelCode()));
        });

    }

    //设置关系对象属性配置列表
    private void setRelationAttr(List<ObjectRelationVO> vos) {
        if (CollectionUtil.isEmpty(vos)) {
            return;
        }
        //查询关系对象配置的属性
        vos.forEach(vo -> {
            List<ObjectRelationAttrDTO> relationAttrList = relationFlagectConfigRepository.queryAttrList(ObjectRelationAttrDTO.builder().relationBid(vo.getBid()).build());
            CfgObjectVo relationFlagAttrResponse = objectModelAppService.getOneJustAttrByBid(vo.getTargetModelCode());
            CfgObjectVo relationAttrResponse = objectModelAppService.getOneJustAttrByBid(vo.getBid());
            if (relationFlagAttrResponse == null || relationAttrResponse == null) {
                throw new BusinessException("关联对象或关系对象查询为空");
            }
            List<ObjectAttrVO> relationFlagObjectAttrList = CollectionUtils.copyList(relationFlagAttrResponse.getAttrList(), ObjectAttrVO.class);
            List<ObjectAttrVO> relationFlagectAttrList = CollectionUtils.copyList(relationAttrResponse.getAttrList(), ObjectAttrVO.class);
            List<ObjectRelationAttrDTO> objectRelationAttrVOS = Lists.newArrayList();
            Map<String, ObjectRelationAttrDTO> relationAttrDTOMap = relationAttrList.stream().collect(Collectors.toMap(k -> k.getSourceModelCode() + k.getInnerName(), Function.identity()));
            relationFlagObjectAttrList.forEach(e -> {
                ObjectRelationAttrDTO objectRelationAttrDTO = relationAttrDTOMap.get(vo.getTargetModelCode() + e.getInnerName());
                if (e.getShow() != null && e.getShow() && Objects.nonNull(objectRelationAttrDTO)) {
                    objectRelationAttrVOS.add(objectRelationAttrDTO);
                } else if (e.getShow() != null && e.getShow()) {
                    ObjectRelationAttrDTO dto = ObjectRelationAttrDTO.builder()
                            .columnWidth(0).sourceModel(RelationObjectConstants.objectRelationAttr.TARGET)
                            .sourceModelCode(vo.getTargetModelCode()).innerName(e.getInnerName()).columnWidth(null).
                                    explain(e.getExplain()).isDelete(false)
                            .realUseInView(true).realRelativeSort(e.getRelativeSort()).build();
                    dto.setBid(SnowflakeIdWorker.nextIdStr());
                    objectRelationAttrVOS.add(dto);
                }
            });
            relationFlagectAttrList.forEach(e -> {
                ObjectRelationAttrDTO objectRelationAttrDTO = relationAttrDTOMap.get(vo.getBid() + e.getInnerName());
                if (e.getShow() != null && e.getShow() && Objects.nonNull(objectRelationAttrDTO)) {
                    objectRelationAttrVOS.add(objectRelationAttrDTO);
                } else if (e.getShow() != null && e.getShow()) {
                    ObjectRelationAttrDTO attrDTO = ObjectRelationAttrDTO.builder()
                            .columnWidth(0).sourceModel(RelationObjectConstants.objectRelationAttr.RELATION)
                            .sourceModelCode(vo.getBid()).innerName(e.getInnerName()).columnWidth(null).
                                    explain(e.getExplain()).isDelete(false)
                            .realUseInView(true).realRelativeSort(e.getRelativeSort()).build();
                    attrDTO.setBid(SnowflakeIdWorker.nextIdStr());
                    objectRelationAttrVOS.add(attrDTO);
                }
            });

            List<ObjectRelationAttrVO> sortList = BeanUtil.copy(objectRelationAttrVOS, ObjectRelationAttrVO.class);
            sortList = sortList.stream().sorted(Comparator.comparing(ObjectRelationAttrVO::getRealRelativeSort, Comparator.nullsLast(Integer::compareTo))).collect(Collectors.toList());
            ;
            vo.setRelationAttr(sortList);
        });

    }

    //检查实例数量的大小设置
    private void checkInstanceNumber(List<ObjectRelationDTO> relationList) {
        relationList.forEach(relation -> {
            StringBuilder sb = new StringBuilder();
            if (relation.getMaxNumber() != null && relation.getMaxNumber() < 0) {
                sb.append("最大实例数量不能小于0!");
            }
            if (relation.getMinNumber() != null && relation.getMinNumber() < 0) {
                sb.append(" 最小实例数量不能小于0!");
            }
            if (relation.getMaxNumber() != null && relation.getMinNumber() != null && relation.getMaxNumber() < relation.getMinNumber()) {
                sb.append(" 最大实例数量必须大于等于最小实例数量!");
            }
            if (StringUtil.isNotBlank(sb)) {
                throw new BusinessException(ErrorMsgEnum.DATA_IS_WRONG.getCode(), sb.toString());
            }
        });

    }

    //获取关系的状态,key:关系bid,value:状态
    private Map<String, Integer> getRelationState(List<ObjectRelationDTO> relationList) {
        List<String> bids = relationList.stream().map(ObjectRelationDTO::getBid).collect(Collectors.toList());

        ObjectRelationQO qo = new ObjectRelationQO();
        qo.setBids(bids);

        return Optional
                .ofNullable(relationFlagectConfigRepository.find(qo))
                .orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.toMap(ObjectRelationPO::getBid, ObjectRelationPO::getEnableFlag));
    }

    @Override
    public List<ObjectRelationVO> getObjectRelationVOsBySourceModelCode(String sourceModelCode){
        //将 sourceModelCode转，例如A01A02转成A01,A01A02这种
        List<String> sourceModelCodes = new ArrayList<String>();
        LinkedHashSet<String> strings = ObjectCodeUtils.splitModelCode(sourceModelCode);
        sourceModelCodes.addAll(strings);
        List<ObjectRelationPO> relationPOList = relationFlagectConfigRepository.findBySourceModelCodes(sourceModelCodes);
        return CollectionUtils.copyList(relationPOList, ObjectRelationVO.class);
    }
}
