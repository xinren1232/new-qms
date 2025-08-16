package com.transcend.plm.datadriven.domain.object.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.domain.basedata.BaseDataService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基类对象-领域服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/5/5 14:50
 */
@Component
public class BaseObjectTreeService {

    @Resource
    private BaseDataService baseDataService;

    @Resource
    private ModelService modelService;

    /**
     * 列表 TODO 如果attr 有重复，需要+1
     *
     * @param modelCode
     * @param wrappers
     * @return List<MBaseData>
     */
    public List<MObjectTree> tree(String modelCode, List<QueryWrapper> wrappers) {
        List<MBaseData> list = baseDataService.list(modelCode, wrappers);
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }
        List<MObjectTree> treeList = list.stream().map(e ->{
            MObjectTree mObjectTree = new MObjectTree();
            mObjectTree.putAll(e);
            return mObjectTree;
        }).collect(Collectors.toList());
        return convert2Tree(treeList);
    }

    /**
     * @param modelCode
     * @param mObjectList
     * @return {@link Boolean }
     */
    public Boolean updateTreeByBidBatch(String modelCode, List<MObjectTree> mObjectList) {
        List<BatchUpdateBO<MObjectTree>> batchUpdateList = Lists.newArrayList();
        mObjectList.forEach(mObject -> {
            QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
            queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
            queryWrapper.setCondition("=");
            queryWrapper.setValue(mObject.getBid());
            BatchUpdateBO<MObjectTree> batchUpdateBO = new BatchUpdateBO<>();
            batchUpdateBO.setBaseData(mObject);
            batchUpdateBO.setWrappers(Arrays.asList(queryWrapper));
            batchUpdateList.add(batchUpdateBO);
        });
        return modelService.batchUpdateTree(modelCode, batchUpdateList);
    }


    /**
     * 组装树 时间复杂度需要2N，不能是 N*N
     * 注:排序在list入参前需要自行排序
     *
     * @param objectTrees 角色信息
     * @return List<MObjectTree>
     */
    @NotNull
    public List<MObjectTree> convert2Tree(List<? extends MObjectTree> objectTrees) {
        List<MObjectTree> treeVos = Lists.newArrayList();
        // 父bid为key,子列表为value, 使用LinkedHashMap保证传递过来的list有顺序
        Map<String, List<MObjectTree>> parentKeyChildrenMap = Maps.newLinkedHashMap();
        // 兼容为版本对象的实例
        Set<String> dataBidList = objectTrees.stream().map(e->e.get(BaseDataEnum.BID.getCode()).toString()).collect(Collectors.toSet());
        objectTrees.forEach(node -> {
            // 父bid
            String parentBid = node.getParentBid();
            if (CommonConst.DEFAULT_PARENT_BID.equals(parentBid)|| !dataBidList.contains(parentBid) ) {
                // a.收集根节点
                treeVos.add(node);
            } else {
                // b.收集父bid为key,子列表为value
                List<MObjectTree> children = parentKeyChildrenMap.get(parentBid);
                if(CollectionUtils.isEmpty(children)){
                    children = Lists.newArrayList();
                    parentKeyChildrenMap.put(parentBid, children);
                }
                children.add(node);
            }
        });
        // 递归设置子集合
        recursiveSetChildren(treeVos, parentKeyChildrenMap);
        return treeVos;
    }

    /**
     * 递归设置子
     *
     * @param roots
     * @param parentCodeAndChildrenMap
     */
    private void recursiveSetChildren(List<MObjectTree> roots,
                                      Map<String, List<MObjectTree>> parentCodeAndChildrenMap) {
        if (CollectionUtils.isEmpty(roots)) {
            return;
        }
        roots.forEach(root -> {
            String parentCode = root.getBid();
            List<MObjectTree> children= parentCodeAndChildrenMap.get(parentCode);

            if (children == null) {
                // 兼容为版本对象的实例
                parentCode = root.get(VersionObjectEnum.DATA_BID.getCode()).toString();
                children = parentCodeAndChildrenMap.get(parentCode);
            }

            root.setChildren(children);
            this.recursiveSetChildren(children, parentCodeAndChildrenMap);
        });
    }

    /**
     * 树形结构转换为列表
     *
     * @param treeList
     * @return List<MObjectTree>
     */
    public List<MObjectTree> flattenTree(List<MObjectTree> treeList) {
        List<MObjectTree> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(treeList)) {
            return result;
        }
        expandChildren(result, treeList);
        return result;
    }

    /**
     * @param result
     * @param treeList
     */
    private void expandChildren(List<MObjectTree> result, List<? extends Map<String, Object>> treeList) {
        if (CollectionUtils.isEmpty(treeList)) {
            return;
        }
        List<MObjectTree> children = Lists.newArrayList();
        treeList.forEach(tree -> {
            MObjectTree objectTree = new MObjectTree();
            objectTree.putAll(tree);
            children.add(objectTree);
        });
        result.addAll(children);
        children.forEach(e -> expandChildren(result, e.getChildren()));
    }
}
