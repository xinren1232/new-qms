package com.transcend.plm.alm.cad.event.handler;

import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author bin.yin
 * @description: cad文档添加关系 前置事件 ,判断关系是否添加过，添加过的只更新num字段
 * @version:
 * @date 2024/06/13 17:57
 */
@Component
public class CadRelationAddEventHandler extends AbstractRelationAddEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    private static final String CAD_STRUCTURE_RELATION_BID = "1238139933135048704";

    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        String relationModelCode = param.getAddExpandAo().getRelationModelCode();
        String sourceBid = param.getAddExpandAo().getSourceBid();
        List<? extends MObject> targetDataList = param.getAddExpandAo().getTargetMObjects();
        Set<String> targetDataBids = targetDataList.stream().map(e -> (String) e.get(VersionObjectEnum.DATA_BID.getCode())).collect(Collectors.toSet());
        QueryWrapper qo = new QueryWrapper();
        qo.eq(RelationEnum.SOURCE_BID.getColumn(), sourceBid)
                .and()
                .in(RelationEnum.TARGET_DATA_BID.getColumn(), targetDataBids);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> queryList = objectModelCrudI.list(relationModelCode, queryWrappers);
        List<BatchUpdateBO<MObject>> batchUpdateBoList = Lists.newArrayList();
        Set<String> existTargetDataBids = Sets.newHashSet();
        queryList.forEach(e -> {
            QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
            queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
            queryWrapper.setCondition("=");
            queryWrapper.setValue(e.getBid());
            BatchUpdateBO<MObject> batchUpdateBO = new BatchUpdateBO<>();
            MObject updateObject = new MObject();
            updateObject.put("num", e.get("num") == null ? 1 : (Integer) e.get("num") + 1);
            batchUpdateBO.setBaseData(updateObject);
            batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
            existTargetDataBids.add((String) e.get(RelationEnum.TARGET_DATA_BID.getCode()));
            batchUpdateBoList.add(batchUpdateBO);
        });
        // 更新关系属性 数量
        if (CollectionUtils.isNotEmpty(batchUpdateBoList)) {
            objectModelCrudI.batchUpdateByQueryWrapper(relationModelCode, batchUpdateBoList, false);
            objectModelCrudI.batchUpdateByQueryWrapper(relationModelCode, batchUpdateBoList, true);
        }
        // 移除掉已经存在的数据
        targetDataList.removeIf(mObject -> existTargetDataBids.contains((String) mObject.get(VersionObjectEnum.DATA_BID.getCode())));
        return super.preHandle(param);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        return CAD_STRUCTURE_RELATION_BID.equals(param.getRelationCfgObjectVo().getBid());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
