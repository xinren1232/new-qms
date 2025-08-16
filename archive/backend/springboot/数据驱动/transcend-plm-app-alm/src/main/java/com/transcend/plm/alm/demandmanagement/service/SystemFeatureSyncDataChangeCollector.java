package com.transcend.plm.alm.demandmanagement.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.plm.alm.demandmanagement.entity.wrapper.SystemFeatureWrapper;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.MapWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendBaseWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统特性同步数据变更收集器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/14 23:10
 */
@Getter
public class SystemFeatureSyncDataChangeCollector {

    /**
     * 排除对比字段
     */
    private final Set<String> excludeFields = new HashSet<>(ListUtil.toList(
            BaseDataEnum.ID.getCode(),
            BaseDataEnum.BID.getCode(),
            VersionObjectEnum.DATA_BID.getCode(),
            ObjectEnum.MODEL_CODE.getCode(),
            ObjectEnum.LC_MODEL_CODE.getCode(),
            BaseDataEnum.CREATED_TIME.getCode(),
            BaseDataEnum.UPDATED_TIME.getCode(),
            BaseDataEnum.CREATED_BY.getCode(),
            BaseDataEnum.UPDATED_BY.getCode(),
            ObjectEnum.OWNER.getCode(),
            ObjectEnum.PERSON_RESPONSIBLE.getCode(),
            ObjectTreeEnum.PARENT_BID.getCode(),
            ObjectEnum.SPACE_APP_BID.getCode(),
            ObjectEnum.SPACE_BID.getCode(),
            SystemFeatureWrapper.LEVEL,
            SystemFeatureWrapper.VERSION_NUMBER
    ));

    /**
     * 实例新增数据
     */
    private final List<MObject> instanceAddList = new ArrayList<>();
    /**
     * 实例更新数据
     */
    private final List<MObject> instanceUpdateList = new ArrayList<>();
    /**
     * 实例更新差异列表
     */
    private final List<DiffResult<MObject>> instanceUpdateDiffList = new ArrayList<>();
    /**
     * 实例删除数据
     */
    private final List<MObject> instanceDeleteList = new ArrayList<>();

    /**
     * 空间Bid
     */
    private final String spaceBid;
    /**
     * 空间应用Bid
     */
    private final String spaceAppBid;


    public SystemFeatureSyncDataChangeCollector(String spaceBid, String spaceAppBid) {
        Assert.notBlank(spaceBid, "目标空间bid不能为空");
        Assert.notBlank(spaceAppBid, "目标空间应用bid不能为空");
        this.spaceBid = spaceBid;
        this.spaceAppBid = spaceAppBid;
    }

    /**
     * 收集数据
     *
     * @param syncDataList  需要同步的数据
     * @param existDataList 当前存在的数据
     */
    public void collect(@NonNull List<MObject> syncDataList, @NonNull List<MObject> existDataList) {

        //1、准备数据
        syncDataList.sort(Comparator.comparing(obj ->
                Optional.of(obj).map(SystemFeatureWrapper::new).map(SystemFeatureWrapper::getLevel)
                        .orElse(0)
        ));

        Map<Object, MObject> existDataMap = existDataList.stream()
                .collect(Collectors.toMap(obj -> obj.get(VersionObjectEnum.DATA_BID.getCode()), Function.identity()));

        //2、对比数据
        syncDataList.forEach(syncData -> {
            MObject existData = existDataMap.get(syncData.get(VersionObjectEnum.DATA_BID.getCode()));

            //原始
            String removedLifeCycleCode = "REMOVED";
            boolean removed = removedLifeCycleCode.equals(syncData.getLifeCycleCode());

            //新增操作
            if (existData == null) {
                if (!removed) {
                    collectAddData(existDataMap, syncData);
                }
                return;
            }

            //需要删除的数据
            if (removed) {
                instanceDeleteList.add(existData);
                existDataMap.remove(existData.get(VersionObjectEnum.DATA_BID.getCode()));
                return;
            }

            //更新数据收集
            collectUpdateData(syncData, existData);
        });


        //对孤儿节点机进行删除
        List<MObject> danglingNodeList = findDanglingNode(existDataMap.values());
        if (CollUtil.isNotEmpty(danglingNodeList)) {
            danglingNodeList.forEach(danglingNode -> {
                //新增数据中有孤儿节点
                if (instanceAddList.contains(danglingNode)) {
                    instanceAddList.remove(danglingNode);
                    return;
                }
                //历史数据中有孤儿节点
                instanceDeleteList.add(danglingNode);
                existDataMap.remove(danglingNode.get(VersionObjectEnum.DATA_BID.getCode()));
            });
        }

    }


    /**
     * 查找孤儿节点
     *
     * @param fullTreeData 需要查找孤儿节点的数据-必须是完整的树数据
     * @return 孤儿节点数据
     */
    public static List<MObject> findDanglingNode(Collection<MObject> fullTreeData) {
        //按照层级排序
        List<SystemFeatureWrapper> dataList = fullTreeData.stream().map(SystemFeatureWrapper::new)
                .sorted(Comparator.comparing(SystemFeatureWrapper::getLevel)).collect(Collectors.toList());

        //先剔除根节点的bid，作为种子
        Set<String> validNodeBidList = dataList.stream().filter(sf -> sf.getLevel() == 0)
                .map(TranscendBaseWrapper::getBid).collect(Collectors.toSet());

        return dataList.stream().filter(sf -> sf.getLevel() != 0)
                .filter(sf -> {
                    //有上层的节点即为有效节点
                    boolean validNode = validNodeBidList.contains(sf.getParentBid());
                    if (validNode) {
                        //有效节点也能作为种子扩散子层
                        validNodeBidList.add(sf.getBid());
                    }
                    //过滤出无效节点
                    return !validNode;
                }).map(MapWrapper::getMetadata).map(o -> (MObject) o).collect(Collectors.toList());
    }


    /**
     * 收集新增数据
     *
     * @param existDataMap 存在的数据
     * @param syncData     同步数据
     */
    private void collectAddData(Map<Object, MObject> existDataMap, MObject syncData) {
        MObject instanceObj = new MObject();
        instanceObj.putAll(syncData);

        TranscendObjectWrapper instance = new TranscendObjectWrapper(instanceObj);
        instance.remove(BaseDataEnum.ID.getCode());
        instance.remove(BaseDataEnum.CREATED_BY.getCode());
        instance.remove(BaseDataEnum.UPDATED_BY.getCode());
        instance.remove(BaseDataEnum.CREATED_TIME.getCode());
        instance.remove(BaseDataEnum.UPDATED_TIME.getCode());

        instance.setBid(SnowflakeIdWorker.nextIdStr());
        instance.setSpaceBid(spaceBid);
        instance.setSpaceAppBid(spaceAppBid);
        instance.setModelCode(TranscendModel.RSF.getCode());
        instance.setLcModelCode(instance.getLcModelCode().replace(instance.getModelCode(), TranscendModel.RSF.getCode()));


        String parentBid = instance.getStr(ObjectTreeEnum.PARENT_BID.getCode());
        if (CommonConst.DEFAULT_PARENT_BID.equals(parentBid)) {
            instanceObj.put(SystemFeatureWrapper.LEVEL, 0);
        } else {
            MObject parentObj = existDataMap.get(parentBid);

            Assert.notNull(parentObj, "【同步失败】特性 {} 无上级特性，需关联上层数据后同步", instanceObj.getCoding());
            instanceObj.put(ObjectTreeEnum.PARENT_BID.getCode(), parentObj.getBid());
            int level = new SystemFeatureWrapper(parentObj).getLevel() + 1;
            instanceObj.put(SystemFeatureWrapper.LEVEL, level);
        }
        existDataMap.put(instanceObj.get(VersionObjectEnum.DATA_BID.getCode()), instanceObj);

        instanceAddList.add(instanceObj);
    }


    /**
     * 收集更新数据
     *
     * @param syncData  同步数据
     * @param existData 存在的数据
     */
    private void collectUpdateData(MObject syncData, MObject existData) {

        //对比数据
        DiffBuilder<MObject> diffBuilder = new DiffBuilder<>(syncData, existData, ToStringStyle.SHORT_PREFIX_STYLE);

        //设置需要对比的值
        Set<String> fields = new HashSet<>(syncData.keySet());
        fields.addAll(existData.keySet());
        fields.removeAll(excludeFields);
        fields.forEach(field -> diffBuilder.append(field, syncData.get(field), existData.get(field)));

        // 构建差异结果
        DiffResult<MObject> diffResult = diffBuilder.build();

        if (diffResult.getNumberOfDiffs() <= 0) {
            return;
        }
        //构建更新对象
        MObject updateObj = new MObject();
        diffResult.getDiffs().forEach(diff -> updateObj.put(diff.getFieldName(), diff.getLeft()));
        updateObj.setId(new MapWrapper(existData).getStr(BaseDataEnum.ID.getCode()));
        instanceUpdateList.add(updateObj);
        instanceUpdateDiffList.add(diffResult);
    }

}
