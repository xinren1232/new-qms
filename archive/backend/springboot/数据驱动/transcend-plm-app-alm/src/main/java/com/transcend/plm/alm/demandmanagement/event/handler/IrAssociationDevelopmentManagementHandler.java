package com.transcend.plm.alm.demandmanagement.event.handler;

import com.alibaba.fastjson.JSON;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.constant.TenantConst;
import com.transcend.plm.alm.demandmanagement.config.DemandManagementProperties;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.api.model.RelationEnum;
import com.transcend.plm.datadriven.apm.permission.service.impl.PermissionRuleService;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendRelationWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataBatchUpdateEvent;
import com.transcend.plm.datadriven.infrastructure.basedata.event.BaseDataUpdateEvent;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Optional;

/**
 * IR关联开发管理事件处理器
 * 增加判断条件，需要锁定后才能进入开发管理 time: 2025/3/10 09:22
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/10 09:22
 */
@RefreshScope
@Component
@Log4j2
public class IrAssociationDevelopmentManagementHandler {
    /**
     * 锁定字段名称，该字段为字符串
     * YES_LOCK 表示锁定
     * NO_LOCK 表示未锁定
     */
    private final String LOCK_FIELD_NAME = "lockFlag";

    private final ObjectModelStandardI<MObject> objectModelCrudI;
    private final PermissionRuleService permissionRuleService;
    private final String[] liefCycleCodeArray;

    public IrAssociationDevelopmentManagementHandler(ObjectModelStandardI<MObject> objectModelCrudI,
                                                     PermissionRuleService permissionRuleService,
                                                     DemandManagementProperties properties) {
        this.objectModelCrudI = objectModelCrudI;
        this.permissionRuleService = permissionRuleService;
        this.liefCycleCodeArray = Optional.ofNullable(properties.getLiefCycleCode())
                .map(map -> map.get(TranscendModel.IR.getCode())).orElseGet(() -> new String[]{});
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class)
    public void handle(BaseDataBatchUpdateEvent event) {
        if (!TranscendModel.matchCode(event.getModelCode(), TranscendModel.IR)) {
            return;
        }
        event.getUpdateList().forEach(bo -> handle(bo.getBaseData(), bo.getWrappers()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(rollbackFor = Exception.class)
    public void handle(BaseDataUpdateEvent event) {
        if (!TranscendModel.matchCode(event.getModelCode(), TranscendModel.IR)) {
            return;
        }
        handle(event.getData(), event.getWrappers());
    }

    /**
     * 处理关联操作
     *
     * @param data     当前更新的数据
     * @param wrappers 查询条件
     */
    private void handle(MBaseData data, List<QueryWrapper> wrappers) {
        TranscendObjectWrapper updateDate = new TranscendObjectWrapper(data);
        String lifeCycleCode = updateDate.getLifeCycleCode();
        String lockStatus = updateDate.getStr(LOCK_FIELD_NAME);
        if (lifeCycleCode == null && lockStatus == null) {
            //两个判定值都没变更则忽略
            return;
        }
        log.info("handle data:{},wrappers:{}", JSON.toJSONString(data), JSON.toJSONString(wrappers));

        List<MObject> instanceList = objectModelCrudI.list(TranscendModel.IR.getCode(), wrappers);
        instanceList.stream().map(TranscendObjectWrapper::new).forEach(instance -> {
            if (isAssociations(instance)) {
                //绑定关系
                associations(instance);
            }
        });
    }


    /**
     * 是否需要绑定关系
     *
     * @param instance 实例
     * @return true 绑定关系 / false 解除关系
     */
    private boolean isAssociations(TranscendObjectWrapper instance) {
        if (instance == null) {
            return false;
        }
        //同时满足状态条件和锁定条件
        return meetStatusCondition(instance) && meetLockCondition(instance);
    }

    /**
     * 是否满足状态条件
     *
     * @param instance 实例对象
     * @return true 满足条件 / false 不满足条件
     */
    private boolean meetStatusCondition(TranscendObjectWrapper instance) {
        String baseCode = "LOCK";
        int baseIndex = ArrayUtils.indexOf(liefCycleCodeArray, baseCode);
        if (baseIndex == -1) {
            return false;
        }
        String lifeCycleCode = instance.getLifeCycleCode();
        if (StringUtils.isBlank(lifeCycleCode)) {
            return false;
        }
        int index = ArrayUtils.indexOf(liefCycleCodeArray, lifeCycleCode);
        return index >= baseIndex;
    }

    /**
     * 是否满足锁定条件
     *
     * @param instance 实例对象
     * @return true 满足条件 / false 不满足条件
     */
    private boolean meetLockCondition(TranscendObjectWrapper instance) {
        return "YES_LOCK".equals(instance.getStr(LOCK_FIELD_NAME));
    }

    /**
     * 是否已经关联
     *
     * @param instance 实例对象
     * @return true 已经关联 / false 未关联
     */
    private boolean hasAssociated(TranscendObjectWrapper instance) {
        QueryWrapper wrapper = new QueryWrapper()
                .eq(RelationEnum.SOURCE_BID.getColumn(), instance.getSpaceBid())
                .and().eq(RelationEnum.TARGET_BID.getColumn(), instance.getBid());
        int count = objectModelCrudI.count(TranscendModel.RELATION_DEVELOPMENT_IR.getCode(), QueryWrapper.buildSqlQo(wrapper));
        return count > 0;
    }


    /**
     * 绑定关系
     *
     * @param instance 实例对象
     */
    private void associations(TranscendObjectWrapper instance) {
        if (hasAssociated(instance)) {
            return;
        }

        //添加关系数据
        TranscendRelationWrapper relation = new TranscendRelationWrapper();
        relation.setModelCode(TranscendModel.RELATION_DEVELOPMENT_IR.getCode());
        relation.setSourceBid(instance.getSpaceBid());
        relation.setSourceDataBid(instance.getSpaceBid());

        relation.setSpaceBid(instance.getSpaceBid());
        relation.setSpaceAppBid(instance.getSpaceAppBid());

        relation.setTargetBid(instance.getBid());
        relation.setTargetDataBid(instance.getBid());
        relation.setDraft(false);
        relation.setPermissionBid(permissionRuleService.getDefaultPermissionBid());
        relation.setBid(SnowflakeIdWorker.nextIdStr());
        relation.setTenantId(String.valueOf(TenantConst.TENANT_ID_DEFAULT));
        relation.setDeleteFlag(false);
        relation.setEnableFlag(true);

        MObject mObject = new MObject();
        mObject.putAll(relation.unWrapper());
        objectModelCrudI.add(TranscendModel.RELATION_DEVELOPMENT_IR.getCode(), mObject);
    }


}
