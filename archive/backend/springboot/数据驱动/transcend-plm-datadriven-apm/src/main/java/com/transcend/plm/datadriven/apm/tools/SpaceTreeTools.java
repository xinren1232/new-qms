package com.transcend.plm.datadriven.apm.tools;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SpaceTreeTools {

    private static final int MAX_TREE_DEPTH = 10;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    /* ========== 从子查父 ========== */

    /**
     * 获取所有祖先BID（从子向上查找所有父代）
     * @param modelCode 模型编码
     * @param parentBidList 子对象BID集合
     * @return 包含所有祖先BID的列表（包含输入的子BID）
     */
    @NotNull
    public List<MObject> findAllAncestorDataList(String modelCode, List<String> parentBidList, Integer level) {
        Set<String> allBids = new LinkedHashSet<>(parentBidList);
        Set<String> currentBids = new HashSet<>(parentBidList);

        List<MObject> parentObjectList = Lists.newArrayList();
        int depth = 0;

        while (!currentBids.isEmpty() && depth < level) {
            List<MObject> currentObjects = listByBids(modelCode, currentBids);
            if (CollectionUtils.isEmpty(currentObjects)) {
                break;
            }

            Set<String> parentBids = currentObjects.stream()
                    .map(m->m.get(ObjectTreeEnum.PARENT_BID.getCode()).toString())
                    .filter(parentBid -> !allBids.contains(parentBid))
                    .collect(Collectors.toSet());

            if (parentBids.isEmpty()) {
                break;
            }

            allBids.addAll(parentBids);
            parentObjectList.addAll(currentObjects);
            currentBids = parentBids;
            depth++;
        }

        return parentObjectList;
    }

    /* ========== 核心查询方法 ========== */

    /**
     * 根据父BID查询直接子对象
     */
    private List<MObject> listByParentBids(String modelCode, Collection<String> parentBids) {
        if (CollectionUtils.isEmpty(parentBids)) {
            return Collections.emptyList();
        }

        QueryWrapper qo = new QueryWrapper();
        qo.in(ObjectTreeEnum.PARENT_BID.getCode(), parentBids);
        return objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(qo));
    }

    /**
     * 根据BID列表查询对象
     */
    private List<MObject> listByBids(String modelCode, Collection<String> bids) {
        if (CollectionUtils.isEmpty(bids)) {
            return Collections.emptyList();
        }

        QueryWrapper qo = new QueryWrapper();
        qo.in(BaseDataEnum.BID.getColumn(), bids);
        return objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(qo));
    }


}