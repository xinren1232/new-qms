package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.collection.CollUtil;
import com.transcend.plm.alm.demandmanagement.entity.wrapper.SystemFeatureWrapper;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataAddEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataBatchUpdateEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统特性层级处理
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/15 16:19
 */
@Service
@AllArgsConstructor
public class SystemFeatureLevelHandler {

    private final ObjectModelStandardI<MObject> objectModelCrudI;

    /**
     * 不匹配判定
     *
     * @param modelCode 模型编码
     * @return 是否匹配
     */
    private static boolean nonMatch(String modelCode) {
        return !TranscendModel.matchCode(modelCode, TranscendModel.SF, TranscendModel.RSF);
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataAddEvent event) {
        String modelCode = event.getModelCode();
        if (nonMatch(modelCode)) {
            return;
        }
        levelUpdate(modelCode, event.getData());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataUpdateEvent event) {
        String modelCode = event.getModelCode();
        if (nonMatch(modelCode)) {
            return;
        }

        childrenRefresh(event.getModelCode(), event.getData(), event.getWrappers());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BaseDataBatchUpdateEvent event) {
        String modelCode = event.getModelCode();
        if (nonMatch(modelCode)) {
            return;
        }

        event.getUpdateList().forEach(updateBo -> childrenRefresh(modelCode, updateBo.getBaseData(), updateBo.getWrappers()));
    }

    /**
     * 更新刷新
     *
     * @param modelCode 模型编码
     * @param data      数据
     * @param wrappers  查询条件
     */
    private void childrenRefresh(String modelCode, MBaseData data, List<QueryWrapper> wrappers) {
        if (data.containsKey(ObjectTreeEnum.PARENT_BID.getCode())) {
            List<MObject> list = objectModelCrudI.list(modelCode, new ArrayList<>(wrappers));
            list.forEach(obj -> levelUpdate(modelCode, obj));
            return;
        }

        if (data.containsKey(SystemFeatureWrapper.LEVEL)) {
            List<MObject> list = objectModelCrudI.list(modelCode, new ArrayList<>(wrappers));
            list.forEach(obj -> {
                QueryWrapper wrapper = new QueryWrapper().eq(ObjectTreeEnum.PARENT_BID.getCode(), obj.getBid())
                        .and().eq(BaseDataEnum.DELETE_FLAG.getColumn(), 0);
                List<MObject> childrenList = objectModelCrudI.list(modelCode, QueryWrapper.buildSqlQo(wrapper));
                levelBatchUpdate(modelCode, obj, childrenList);
            });
        }
    }


    /**
     * 层级更新
     *
     * @param modelCode 模型编码
     * @param data      数据
     */
    private void levelUpdate(String modelCode, MBaseData data) {
        SystemFeatureWrapper wrapper = new SystemFeatureWrapper(data);
        String parentBid = wrapper.getStr(ObjectTreeEnum.PARENT_BID.getCode());
        Integer level = null;
        //计算层级
        if (CommonConst.DEFAULT_PARENT_BID.equals(parentBid)) {
            level = 0;
        } else {
            MObject parent = objectModelCrudI.getByBid(modelCode, parentBid);
            if (parent != null) {
                level = new SystemFeatureWrapper(parent).getLevel() + 1;
            }
        }

        //判断是否需要更新
        if (level == null) {
            return;
        }
        Integer oldLevel = wrapper.getLevel();
        if (oldLevel != null && oldLevel.equals(level)) {
            return;
        }

        //执行更新
        MObject updateObj = new MObject();
        updateObj.put(SystemFeatureWrapper.LEVEL, level);
        objectModelCrudI.updateByBid(modelCode, data.getBid(), updateObj);
    }


    /**
     * 层级更新
     *
     * @param parent    父级
     * @param modelCode 模型编码
     * @param dataList  数据列表
     */
    private void levelBatchUpdate(String modelCode, MObject parent, List<MObject> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return;
        }

        int level = new SystemFeatureWrapper(parent).getLevel() + 1;
        List<String> bidList = dataList.stream().map(MBaseData::getBid).collect(Collectors.toList());
        MObject updateObj = new MObject();
        updateObj.put(SystemFeatureWrapper.LEVEL, level);
        objectModelCrudI.batchUpdatePartialContentByIds(modelCode, updateObj, bidList);
    }

}
