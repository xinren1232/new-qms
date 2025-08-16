package com.transcend.plm.datadriven.apm.space.service;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiTreeConfigVo;
import com.transcend.plm.datadriven.apm.space.utils.MultiTreeUtils;
import com.transcend.plm.datadriven.common.strategy.MultipleStrategy;
import com.transcend.plm.datadriven.common.tool.Assert;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 多对象树数据查询服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/7 09:27
 */
public interface MultiTreeDataQueryService extends MultipleStrategy<MultiTreeDataQueryService.Params> {


    /**
     * 查询条件
     */
    @lombok.Data
    @Accessors(chain = true)
    final class Params {
        /**
         * 用户工号
         */
        private String empNo;
        /**
         * 空间标识
         */
        private String spaceBid;
        /**
         * 是否校验权限
         */
        private Boolean checkPermission;
        /**
         * 多应用树配置
         */
        private MultiTreeConfigVo multiAppTreeConfig;
        /**
         * 查询条件集合 key 模型编码 value 查询条件
         */
        private Map<String, List<QueryWrapper>> queryWrapperMap;
        /**
         * 严格模式
         * 严格模式下，当查询条件中包含模型专属查询条件时，条件的下层必须有数据才能输出。
         */
        private Boolean strictMode;

        /**
         * 获取查询条件
         *
         * @param modelCode 模型编码
         * @return 查询条件
         */
        @NotNull
        public List<QueryWrapper> queryWrappers(String modelCode) {
            return Optional.ofNullable(queryWrapperMap).map(map -> map.get(modelCode)).orElse(Collections.emptyList());
        }



    }

    /**
     * 响应数据
     */
    @RequiredArgsConstructor
    final class Data {
        /**
         * 数据配置
         */
        @NonNull
        private MultiTreeConfigVo multiAppTreeConfig;

        /**
         * 数据集合
         * key 模型编码 value 实例数据
         */
        private final Map<String, List<MObject>> dataMap = new HashMap<>(16);

        /**
         * 获取数据
         *
         * @param key 模型编码
         * @return 数据列表
         */
        public List<MObject> getData(String key) {
            return dataMap.get(key);
        }

        /**
         * 设置数据
         *
         * @param modelCode 模型编码
         * @param dataList  数据列表
         */
        public void putData(String modelCode, List<MObject> dataList) {
            Assert.notBlank(modelCode, "modelCode is blank");
            if (dataList == null || dataList.isEmpty()) {
                return;
            }
            List<MObject> list = dataMap.get(modelCode);
            if (list == null) {
                dataMap.put(modelCode, dataList);
                return;
            }
            list.addAll(dataList);
        }

        /**
         * 设置数据
         *
         * @param dataMap 数据Map
         */
        public void putDataAll(Map<String, List<MObject>> dataMap) {
            if (dataMap == null) {
                return;
            }
            dataMap.forEach(this::putData);
        }

        /**
         * 获取实例数据
         *
         * @return 实例数据
         */
        public List<MObject> getInstanceList() {
            return getDataList(MultiTreeUtils.getInstanceFlatModelCodes(this.multiAppTreeConfig));
        }

        /**
         * 获取实例数据
         *
         * @return 实例数据
         */
        public List<MObject> getRelationList() {
            return getDataList(MultiTreeUtils.getRelationFlatModelCodes(this.multiAppTreeConfig));
        }


        /**
         * 获取数据列表
         *
         * @param modelCodes 模型编码集合
         * @return 数据列表
         */
        @NotNull
        private List<MObject> getDataList(List<String> modelCodes) {
            if (modelCodes == null || modelCodes.isEmpty()) {
                return Collections.emptyList();
            }
            return modelCodes.parallelStream().distinct().flatMap(modelCode -> {
                List<MObject> list = this.dataMap.get(modelCode);
                if (list == null) {
                    return Stream.empty();
                }
                //通过Bid去重
                return list.stream().collect(Collectors.toMap(MObject::getBid, Function.identity(),
                        (oldValue, newValue) -> oldValue)).values().stream();
            }).collect(Collectors.toList());
        }
    }

    /**
     * 查询数据的方法
     *
     * @param params 查询参数
     * @return 查询结果
     */
    Data query(Params params);

}
