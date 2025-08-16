package com.transcend.plm.configcenter.object.domain.aggergate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.configcenter.common.enums.ErrorMsgEnum;
import com.transcend.plm.configcenter.common.spring.PlmContextHolder;
import com.transcend.plm.configcenter.common.util.ObjectCodeUtils;
import com.transcend.plm.configcenter.common.web.exception.ExceptionConstructor;
import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectRepository;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectPositionParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectTreeVo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.common.CollectionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ObjectModelRoot
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/29 11:03
 */
@Slf4j
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class CfgObjectRoot {

    @Resource
    private CfgObjectRepository cfgObjectRepository = PlmContextHolder.getBean(CfgObjectRepository.class);

    private List<CfgObjectPo> poList;

    private List<CfgObjectTreeVo> treeList;

    public static CfgObjectRoot build() {
        return new CfgObjectRoot();
    }

    /**
     * 批量编辑 - 针对同级拖拉 && 树结构上编辑名称
     */
    public Boolean bulkUpdatePosition(List<ObjectPositionParam> objectPositionParamList) {
        if (CollectionUtil.isEmpty(objectPositionParamList)) {
            return Boolean.TRUE;
        }
        List<CfgObjectPo> modelList = BeanUtil.copy(objectPositionParamList, CfgObjectPo.class);
        return cfgObjectRepository.batchUpdateAndHistory(modelList);
    }

    /**
     * 批量编辑校验 - 校验对象名称不能相同
     */
    public CfgObjectRoot checkWithBulkEditPosition(List<ObjectPositionParam> objectPositionParamList) {
        //size = 1 是编辑名称  size > 1 是同级拖拉
        //只有size = 1 的时候才校验名称
        if (CollectionUtil.isNotEmpty(objectPositionParamList) && objectPositionParamList.size() == 1) {
            ObjectPositionParam objectPositionParam = objectPositionParamList.get(0);
            String name = objectPositionParam.getName();
            Integer count = cfgObjectRepository.findObjectCountByName(name);
            if (count > 0) {
                throw ExceptionConstructor.runtimeBusExpConstructor(ErrorMsgEnum.ERROR.getCode(),
                        "对象中已存在相同名称【" + name + "】的对象");
            }
        }
        return this;
    }

    /**
     * 查询所有对象
     */
    public CfgObjectRoot findAll() {
        //查all - 过滤掉关系对象
        poList = cfgObjectRepository.findAll();
        return this;
    }

    /**
     * po转换成treeVO
     */
    public CfgObjectRoot conversionToTreeVo() {
        //查all - 过滤掉关系对象
        List<CfgObjectPo> all = poList.stream()
//                .filter(po -> Boolean.FALSE.equals(po.getRelationFlag()))
                .collect(Collectors.toList());
        treeList = BeanUtil.copy(all, CfgObjectTreeVo.class);
        return this;
    }

    /**
     * po转换成vo
     */
    public List<CfgObjectVo> conversionToVO() {
        return BeanUtil.copy(poList, CfgObjectVo.class);
    }

    /**
     * po转换成modelCode
     */
    public List<String> conversionToModelCode() {
        return poList.stream().map(CfgObjectPo::getModelCode).collect(Collectors.toList());
    }

    /**
     * 构建树 - 填充锁信息
     */
    public CfgObjectRoot populateLockInfo() {
        //填充锁信息
        treeList = CfgObjectLockRoot.build().populateLockInfo(treeList);
        return this;
    }

    /**
     * 构建树
     */
    public List<CfgObjectTreeVo> buildTree() {
        Map<String, List<CfgObjectTreeVo>> parentCodeAndChildrenMap = Maps.newHashMap();
        List<CfgObjectTreeVo> roots = Lists.newArrayList();
        treeList.forEach(treeVO -> {
            //Map<modelCode去掉后三位作为parentBid，子List>
            String modelCode = treeVO.getModelCode();
            String parentModelCode = modelCode.substring(0, modelCode.length() - 3);
            List<CfgObjectTreeVo> children = parentCodeAndChildrenMap.get(parentModelCode);
            if (CollectionUtil.isEmpty(children)) {
                children = Lists.newArrayList();
            }
            children.add(treeVO);
            parentCodeAndChildrenMap.put(parentModelCode, children);
            //根list
            if (modelCode.length() == ObjectCodeUtils.MODEL_CODE_SIZE_NUM) {
                roots.add(treeVO);
            }
        });
        //递归设置子
        this.recursiveSetChildren(roots, parentCodeAndChildrenMap);
        return roots.stream().sorted(Comparator.comparing(CfgObjectTreeVo::getSort)).collect(Collectors.toList());
    }

    /**
     * 递归设置子
     */
    private void recursiveSetChildren(List<CfgObjectTreeVo> roots,
                                      Map<String, List<CfgObjectTreeVo>> parentCodeAndChildrenMap) {
        if (CollectionUtil.isEmpty(roots)) {
            return;
        }
        roots.forEach(root -> {
            String parentCode = root.getModelCode();
            List<CfgObjectTreeVo> children = parentCodeAndChildrenMap.get(parentCode);
            if (CollectionUtil.isNotEmpty(children)) {
                children.sort(Comparator.comparing(CfgObjectTreeVo::getSort));
            }
            root.setChildren(children);
            this.recursiveSetChildren(children, parentCodeAndChildrenMap);
        });
    }

    /**
     * 根据modelCode查询其本身及其所有子对象列表
     */
    public CfgObjectRoot findChildrenListByModelCode(String modelCode) {
        poList = cfgObjectRepository.findAllChildrenByModelCode(modelCode);
        return this;
    }

    /**
     * 根据modelCodeList查询特定的对象
     */
    public List<CfgObjectVo> listByModelCodes(List<String> modelCodes) {
        List<CfgObjectVo> cfgObjectVoList = Lists.newArrayList();
        for (String code : modelCodes) {
            CfgObjectVo cfgObjectVO = CfgObject.buildWithModelCode(code)
                    .populateBaseInfoByModelCode()
                    .build();
            cfgObjectVoList.add(cfgObjectVO);
        }

        return cfgObjectVoList;
    }

    /**
     * 根据modelCode查询父对象的bidList（包含本身，从小到大排序）
     */
    public List<String> findParentBidListByModelCode(String modelCode) {
        LinkedHashSet<String> codeList = ObjectCodeUtils.splitModelCodeDesc(modelCode);
        List<String> bidList = Lists.newArrayList();
        for (String code : codeList) {
            CfgObjectPo po = cfgObjectRepository.getByModelCode(code);
            if (ObjectUtils.isNotEmpty(po)) {
                bidList.add(po.getBid());
            }
        }
        return bidList;
    }

    /**
     * 根据nameList查询对象List
     */
    public List<CfgObjectVo> findListByNameList(List<String> nameList) {
        List<CfgObjectPo> po = cfgObjectRepository.findListByNameList(nameList);
        return BeanUtil.copy(po, CfgObjectVo.class);
    }

    /**
     * 根据name模糊查询对象List
     */
    public List<CfgObjectVo> findLikeName(String name) {
        List<CfgObjectPo> po = cfgObjectRepository.findLikeName(name);
        return BeanUtil.copy(po, CfgObjectVo.class);
    }

    /**
     * 查询基类对象
     * @param baseModel  对象基类
     * @return List<ObjectModelVO>
     */
    public List<CfgObjectVo> listByBaseModel(String baseModel) {
        return BeanUtil.copy(cfgObjectRepository.findByBaseModel(baseModel), CfgObjectVo.class);
    }
}
