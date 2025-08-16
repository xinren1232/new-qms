package com.transcend.plm.datadriven.apm.strategy;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.ApmSpaceApplicationService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @Author Qiu Yuhao （Mickey）
 * @Date 2024/3/20 11:30
 * @Describe 分页查询条件客制化策略 针对 空间bid+应用bid 对应 拓展策略
 */
@Slf4j
@Component
public class CustomBatchDeleteExtendStrategy {

    @Resource
    private ApmSpaceAppService apmSpaceAppService;
    @Resource
    private IApmSpaceAppDataDrivenService iApmSpaceAppDataDrivenService;
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI<MObject> objectModelCrudI;
    @Resource
    private ObjectModelDomainService objectModelDomainService;

    private Map<String, BiConsumer<ApmSpaceApp, List<String>>> preStrategyMap;

    /**
     * 删除TOS特性集，删除关联TOS特性的SF特性 //(A1B是源对象，A1D是关系对象，A1AA01是目标对象)
     */
    @Value("${custom.batchDelete.extend.tosModelCode:A1B}")
    private String tosModelCode;

    @Value("${custom.batchDelete.extend.tree.modelCodeSet:A1AA00,A1AA01}")
    private Set<String> treeModelCodeSet;


    // 定义常量 (A1B是源对象，A1D是关系对象，A1AA01是目标对象)
    private static final String RELATION_TYPE_A1D = "A1D";
    private static final String TARGET_MODEL_CODE = "A1AA01";
    private static final String BID_FIELD = BaseDataEnum.BID.getCode();
    private static final String TARGET_BID_FIELD = RelationObjectEnum.TARGET_BID.getCode();

    public void preHandler(String spaceAppBid, List<String> bids) {
        Optional.ofNullable(apmSpaceAppService.getByBid(spaceAppBid))
                .filter(app -> tosModelCode.equals(app.getModelCode()))
                .ifPresent(app -> deleteRelatedObjects(bids));
    }

    /**
     * 删除与 bids 关联的 A1AA01 和 A1D 类型的对象
     */
    private void deleteRelatedObjects(List<String> bids) {
        List<MObject> relationData = listRelationDataBySourceBid(bids, RELATION_TYPE_A1D);
        if (CollectionUtils.isEmpty(relationData)) {
            return;
        }

        // 合并一次流处理，减少循环次数
        List<String> targetBidList = new ArrayList<>();
        List<String> relationBidList = new ArrayList<>();

        for (MObject obj : relationData) {
            Object targetBid = obj.get(TARGET_BID_FIELD);
            Object relationBid = obj.get(BID_FIELD);

            if (targetBid != null) {
                targetBidList.add(targetBid.toString());
            }
            if (relationBid != null) {
                relationBidList.add(relationBid.toString());
            }
        }

        if (!targetBidList.isEmpty()) {
            objectModelCrudI.batchLogicalDeleteByBids(TARGET_MODEL_CODE, targetBidList);
        }

        if (!relationBidList.isEmpty()) {
            objectModelCrudI.batchLogicalDeleteByBids(RELATION_TYPE_A1D, relationBidList);
        }
    }

    private List<MObject> listRelationDataBySourceBid(List<String> sourceBidList, String relationModelCode){
        //先查询关系(历史)实例表
        QueryWrapper qo = new QueryWrapper();
        qo.in(RelationEnum.SOURCE_BID.getColumn(), sourceBidList);
        List<QueryWrapper> queryWrappers = QueryWrapper.buildSqlQo(qo);
        List<MObject> mRelationObjectList;
        try {
            QueryCondition queryCondition = new QueryCondition();
            // 默认更新时间倒序
            queryCondition.setOrders(Lists.newArrayList(Order.of().setProperty(BaseDataEnum.UPDATED_TIME.getCode()).setDesc(true)));
            queryCondition.setQueries(queryWrappers);
            mRelationObjectList = objectModelDomainService.list(relationModelCode, queryCondition);
        } catch (Exception e) {
            throw new BusinessException(String.format("查询关系实例表异常，可能是数据问题或者表不存在，关系对象模型编码[%s]", relationModelCode));
        }
        return mRelationObjectList;
    }



}
