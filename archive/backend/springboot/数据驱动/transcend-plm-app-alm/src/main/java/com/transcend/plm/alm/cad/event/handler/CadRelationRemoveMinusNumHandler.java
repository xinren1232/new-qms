package com.transcend.plm.alm.cad.event.handler;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.event.entity.RelationBatchDeleteEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationBatchRemoveEventHandler;
import com.transcend.plm.datadriven.common.constant.DataBaseConstant;
import com.transcend.plm.datadriven.common.util.TranscendObjectUtil;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @program: transcend-plm-datadriven
 * @description: when remove cad relation,minus relation instance's  num field value
 * @author: peng.qin
 * @create: 2024-06-27 10:06
 * @version: 1.0.0
 **/
@Component
public class CadRelationRemoveMinusNumHandler extends AbstractRelationBatchRemoveEventHandler {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    private static final String CAD_STRUCTURE_RELATION_BID = "1238139933135048704";
    @Override
    public RelationBatchDeleteEventHandlerParam preHandle(RelationBatchDeleteEventHandlerParam param) {
        List<String> relationBids = param.getRelationDelAndRemParamAo().getRelationBids();
        List<MObject> relationDataList = objectModelCrudI.listByBids(relationBids, param.getRelationCfgObjectVo().getModelCode());
        List<String> removeBids = Lists.newArrayList();
        List<BatchUpdateBO<MObject>> updateList = Lists.newArrayList();
        for (MObject relationObject : relationDataList) {
            if (!TranscendObjectUtil.isFiledEmpty(relationObject, "num") && (Integer)relationObject.get("num") > 1) {
                QueryWrapper queryWrapper = new QueryWrapper(Boolean.FALSE);
                queryWrapper.setProperty(DataBaseConstant.COLUMN_BID);
                queryWrapper.setCondition("=");
                queryWrapper.setValue(relationObject.getBid());
                BatchUpdateBO<MObject> batchUpdateBO = new BatchUpdateBO<>();
                MObject updateObject = new MObject();
                updateObject.put("num", (Integer)relationObject.get("num") - 1);
                batchUpdateBO.setBaseData(updateObject);
                batchUpdateBO.setWrappers(Collections.singletonList(queryWrapper));
                updateList.add(batchUpdateBO);
            } else {
                removeBids.add(relationObject.getBid());
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            objectModelCrudI.batchUpdateByQueryWrapper(param.getRelationCfgObjectVo().getModelCode(), updateList, false);
            objectModelCrudI.batchUpdateByQueryWrapper(param.getRelationCfgObjectVo().getModelCode(), updateList, true);
        }
        param.getRelationDelAndRemParamAo().setRelationBids(removeBids);
        return super.preHandle(param);
    }

    @Override
    public boolean isMatch(RelationBatchDeleteEventHandlerParam param) {
        return CAD_STRUCTURE_RELATION_BID.equals(param.getRelationCfgObjectVo().getBid());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
