package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.collection.CollUtil;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataDeleteEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 特性删除拦截处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/27 09:05
 */
@Component
@AllArgsConstructor
public class SystemFeatureDeleteInterceptHandler {

    private final ObjectModelStandardI<MObject> objectModelCrudI;


    /**
     * 不匹配判定
     *
     * @param modelCode 模型编码
     * @return 是否匹配
     */
    private static boolean nonMatch(String modelCode) {
        return !TranscendModel.matchCode(modelCode, TranscendModel.SF);
    }


    @EventListener
    public void handle(BaseDataDeleteEvent event) {
        String modelCode = event.getModelCode();
        if (nonMatch(modelCode)) {
            return;
        }

        List<MObject> deleteDataList = objectModelCrudI.list(modelCode, event.getWrappers());
        if (CollUtil.isNotEmpty(deleteDataList)) {
            List<String> bidList = deleteDataList.stream().map(MBaseData::getBid).collect(Collectors.toList());
            QueryWrapper wrapper = new QueryWrapper().in(ObjectTreeEnum.PARENT_BID.getCode(), bidList)
                    .and().eq(BaseDataEnum.DELETE_FLAG.getCode(), 0);
            int count = objectModelCrudI.count(modelCode, QueryWrapper.buildSqlQo(wrapper));
            Assert.isFalse(count > 0, "特性存在子节点不能删除");
        }
    }
}
