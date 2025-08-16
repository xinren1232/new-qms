package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.event.entity.RelationAddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationAddEventHandler;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.AddExpandAo;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 自关联拦截处理器
 * 例如RR 关联 RR时，被关联的RR不能有子级，也就是自关联层级不会超过2层
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/1 14:27
 */
@Service
@AllArgsConstructor
public class RecursiveInterceptorHandler extends AbstractRelationAddEventHandler {
    private final ObjectModelStandardI<MObject> objectModelCrudI;


    @Override
    public RelationAddEventHandlerParam preHandle(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        String relationModelCode = addExpandAo.getRelationModelCode();

        //获取所有被关联的Bid
        List<String> targetBidList = Optional.ofNullable(addExpandAo.getTargetMObjects())
                .map(list -> list.stream().map(MObject::getBid).collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
        Optional.ofNullable(addExpandAo.getRelationMObject()).map(MBaseData::getBid).ifPresent(targetBidList::add);

        targetBidList = targetBidList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

        if (!targetBidList.isEmpty()) {
           Assert.isFalse(targetBidList.stream().anyMatch(targetBid -> targetBid.equals(addExpandAo.getSourceBid())),
                   "操作非法，请勿自关联");

            QueryWrapper wrapper = new QueryWrapper().in(RelationEnum.SOURCE_BID.getColumn(), targetBidList);
            int count = objectModelCrudI.count(relationModelCode, QueryWrapper.buildSqlQo(wrapper));

            Assert.isTrue(count <= 0, "无法关联已有子集的数据");
        }

        return super.preHandle(param);
    }

    @Override
    public boolean isMatch(RelationAddEventHandlerParam param) {
        AddExpandAo addExpandAo = param.getAddExpandAo();
        if (addExpandAo == null || Boolean.FALSE.equals(addExpandAo.getSelected())) {
            return false;
        }

        String relationModelCode = addExpandAo.getRelationModelCode();
        return TranscendModel.matchCode(relationModelCode, TranscendModel.RELATION_RR_RR, TranscendModel.RELATION_IR_IR);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
