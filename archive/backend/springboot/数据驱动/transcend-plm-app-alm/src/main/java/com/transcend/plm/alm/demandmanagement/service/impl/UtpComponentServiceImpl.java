package com.transcend.plm.alm.demandmanagement.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.alm.demandmanagement.entity.ao.UtpComponentTreeListAo;
import com.transcend.plm.alm.demandmanagement.entity.vo.UtpComponentTreeVo;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.alm.demandmanagement.service.UtpComponentService;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.TranscendBaseWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import com.transcend.plm.datadriven.common.wapper.TranscendTreeObjectWrapper;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.utp.open.api.Client;
import com.transsion.utp.open.api.Constant;
import com.transsion.utp.open.api.request.QueryComponentTreeRequest;
import com.transsion.utp.open.api.response.QueryComponentTreeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * utp模块服务
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/22 16:42
 */
@Log4j2
@Service
@AllArgsConstructor
public class UtpComponentServiceImpl implements UtpComponentService {

    private final Client client;
    private final ObjectModelStandardI<MObject> objectModelCrudI;


    @Async
    @Override
    public void dataCorrection() {
        //远程信息
        Map<String, List<String>> subComponentNameMap = utpSubComponentNameMap();
        Assert.notEmpty(subComponentNameMap, "UTP模块信息为空");

        //本地测试模块信息
        List<TranscendTreeObjectWrapper> localComponentList = getAllLocalComponent();

        Map<String, String> localComponentBidNameMap = localComponentList.stream()
                .filter(o -> "0".equals(o.getParentBid()))
                .collect(Collectors.toMap(TranscendBaseWrapper::getBid, TranscendObjectWrapper::getName));

        Map<String, List<String>> subComponentMap = localComponentList.stream()
                .filter(o -> StringUtils.isNotBlank(o.getParentBid()) && !"0".equals(o.getParentBid()))
                .map(o -> {
                    String nameKey = getComponentNameKey(localComponentBidNameMap.get(o.getParentBid()), o.getName());
                    List<String> uptCodeListValue = subComponentNameMap.get(nameKey);
                    if (uptCodeListValue == null) {
                        log.warn("UTP模块信息缺失，bid: {} name: {}", o.getBid(), nameKey);
                        return null;
                    }
                    return new AbstractMap.SimpleEntry<>(o.getBid(), uptCodeListValue);
                }).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //更新数据
        updateData(localComponentBidNameMap, subComponentMap);
    }


    @Override
    public List<UtpComponentTreeVo> getComponentTreeList(UtpComponentTreeListAo ao) {
        QueryComponentTreeRequest request = new QueryComponentTreeRequest();
        request.setOwnerCodeList(ao.getOwnerUserList());
        request.setSearchKey(ao.getSearchKey());
        request.setIsDelete(true);
        try {
            QueryComponentTreeResponse response = client.syncInvoke(request);
            if (response.isSuccess()) {
                return toComponentTreeVoList(response.getData());
            }
        } catch (Exception e) {
            log.error("调用UTP系统获取模块信息异常", e);
        }
        return Collections.emptyList();
    }


    //region 私有方法

    /**
     * 转换为测试模块树视图对象集合
     *
     * @param list 模块数据集合
     * @return 视图对象集合
     */
    private List<UtpComponentTreeVo> toComponentTreeVoList(List<QueryComponentTreeResponse.Data> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(this::toComponentTreeVo).collect(Collectors.toList());
    }

    /**
     * 转换未测试模块树视图对象
     *
     * @param data 模块数据
     * @return 视图对象
     */
    private UtpComponentTreeVo toComponentTreeVo(QueryComponentTreeResponse.Data data) {
        if (data == null) {
            return null;
        }
        //已失效的数据需要标记一下
        boolean disabled = Boolean.TRUE.equals(data.getIsDelete());
        String name = disabled ? String.format("[已失效]%s", data.getName()) : data.getName();

        UtpComponentTreeVo vo = new UtpComponentTreeVo();
        vo.setName(name);
        vo.setCode(data.getCode());
        vo.setParentCode(data.getParentCode());
        vo.setOwnerName(data.getOwnerName());
        vo.setOwnerCode(data.getOwnerCode());
        vo.setType(data.getType());
        vo.setDisabled(disabled);

        vo.setChildren(toComponentTreeVoList(data.getChildren()));

        return vo;
    }


    /**
     * 获取ut对应名称map信息
     *
     * @return map信息
     */
    private Map<String, List<String>> utpSubComponentNameMap() {
        //准备对应数据信息
        QueryComponentTreeRequest request = new QueryComponentTreeRequest();
        QueryComponentTreeResponse response = client.syncInvoke(request);
        request.setIsDelete(true);
        Assert.isTrue(response.isSuccess(), "调用UTP系统获取模块信息异常");
        List<QueryComponentTreeResponse.Data> dataList = response.getData();

        //数据map化
        Map<String, List<String>> subComponentNameMap = new HashMap<>(16);
        recursiveSubComponentMap(subComponentNameMap, dataList, null, Collections.emptyList());
        return subComponentNameMap;
    }

    /**
     * 获取所有的本地模块信息
     *
     * @return 本地模块列表
     */
    @NotNull
    private List<TranscendTreeObjectWrapper> getAllLocalComponent() {
        String testModelCode = "A1H";
        return objectModelCrudI.list(testModelCode, new ArrayList<>()).stream()
                .map(TranscendTreeObjectWrapper::new).collect(Collectors.toList());
    }


    /**
     * 递归组装子模块编码组合
     *
     * @param subComponentNames 子模块编码组合
     * @param list              子模块列表
     * @param parentName        父模块名称
     * @param value             模块编码组合
     */
    private void recursiveSubComponentMap(Map<String, List<String>> subComponentNames,
                                          List<QueryComponentTreeResponse.Data> list,
                                          String parentName, List<String> value) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (QueryComponentTreeResponse.Data data : list) {
            List<String> newValue = new ArrayList<>(value);
            newValue.add(data.getCode());

            //遇到子模块则开始收集并跳出
            if (Constant.ComponentType.SUB_COMPONENT.equals(data.getType())) {
                String key = getComponentNameKey(parentName, data.getName());
                if (subComponentNames.containsKey(key)) {
                    List<String> valueList = subComponentNames.get(key);
                    String componentCode = valueList.get(2);
                    String subComponentCode = valueList.get(3);
                    if (!componentCode.equals(data.getParentCode()) || !subComponentCode.equals(data.getCode())) {
                        log.warn("重复的子模块编码组合:{} code: {} / {} | {} / {}",
                                key, componentCode, subComponentCode, data.getParentCode(), data.getCode());
                    }
                } else {
                    subComponentNames.put(key, newValue);
                }
                continue;
            }
            recursiveSubComponentMap(subComponentNames, data.getChildren(), data.getName(), newValue);
        }
    }

    @NotNull
    private static String getComponentNameKey(String parentName, String name) {
        return String.format("%s / %s", parentName, name);
    }


    /**
     * 更新数据操作
     *
     * @param localComponentBidNameMap 本地模块编码名称
     * @param subComponentMap          子模块名称对应编码集
     */
    private void updateData(Map<String, String> localComponentBidNameMap, Map<String, List<String>> subComponentMap) {
        String testModulesFieldName = "testModules";
        String testSubmoduleFieldName = "testSubmodule";

        Set<String> fields = new HashSet<>();
        fields.add(BaseDataEnum.BID.getCode());
        fields.add(testModulesFieldName);
        fields.add(testSubmoduleFieldName);

        int currentIndex = 0;
        do {
            currentIndex++;
            List<MObject> dataList = querySrList(currentIndex, fields);
            if (CollUtil.isEmpty(dataList)) {
                break;
            }

            List<BatchUpdateBO<MObject>> updateDataList = dataList.stream().map(TranscendObjectWrapper::new)
                    .map(data -> {
                        MObject updateData = new MObject();
                        //更新子模块
                        updateTestSubmodule(updateData, subComponentMap, data, testSubmoduleFieldName);
                        return createUpdateParams(data, updateData);
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            if (CollUtil.isNotEmpty(updateDataList)) {
                objectModelCrudI.batchUpdateByQueryWrapper(TranscendModel.SR.getCode(), updateDataList, false);
            }

            updateDataList = dataList.stream().map(TranscendObjectWrapper::new)
                    .map(data -> {
                        MObject updateData = new MObject();
                        //更新模块
                        updateTestModules(updateData, localComponentBidNameMap, data, testModulesFieldName);
                        return createUpdateParams(data, updateData);
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            if (CollUtil.isNotEmpty(updateDataList)) {
                objectModelCrudI.batchUpdateByQueryWrapper(TranscendModel.SR.getCode(), updateDataList, false);
            }
        } while (true);
    }

    /**
     * 创建更新参数
     *
     * @param data       数据
     * @param updateData 更新信息
     * @return 更新参数
     */
    @Nullable
    private BatchUpdateBO<MObject> createUpdateParams(TranscendObjectWrapper data, MObject updateData) {
        if (updateData.isEmpty()) {
            return null;
        }
        BatchUpdateBO<MObject> updateBO = new BatchUpdateBO<>();
        updateBO.setBaseData(updateData);
        updateBO.setWrappers(QueryWrapper.buildSqlQo(
                new QueryWrapper().eq(BaseDataEnum.BID.getColumn(), data.getBid())
        ));
        return updateBO;
    }


    /**
     * 更新子模块
     *
     * @param updateData      更新数据对象
     * @param subComponentMap 子模块名称对应编码集
     * @param data            数据库查询对象
     * @param fieldName       字段名称
     */
    private static void updateTestSubmodule(MObject updateData, Map<String, List<String>> subComponentMap,
                                            TranscendObjectWrapper data, String fieldName) {
        List<Object> testSubmodule = data.getList(Object.class, fieldName);
        if (CollUtil.isEmpty(testSubmodule)) {
            return;
        }

        boolean isUpdate = false;
        for (int i = 0; i < testSubmodule.size(); i++) {
            Object obj = testSubmodule.get(i);
            if (obj instanceof String) {
                List<String> utpCodeList = subComponentMap.get(obj);
                if (utpCodeList == null) {
                    continue;
                }

                isUpdate = true;
                testSubmodule.set(i, utpCodeList);
            }
        }

        if (isUpdate) {
            updateData.put(fieldName, testSubmodule);
        }
    }

    /**
     * 更新模块
     *
     * @param updateData               更新数据对象
     * @param localComponentBidNameMap 本地模块编码名称
     * @param data                     数据库查询对象
     * @param fieldName                字段名称
     */
    private static void updateTestModules(MObject updateData, Map<String, String> localComponentBidNameMap,
                                          TranscendObjectWrapper data, String fieldName) {
        List<String> testModules = data.getList(String.class, fieldName);
        if (CollUtil.isEmpty(testModules)) {
            return;
        }

        boolean isUpdate = false;
        for (int i = 0; i < testModules.size(); i++) {
            String bid = testModules.get(i);
            String name = localComponentBidNameMap.get(bid);
            if (name == null) {
                continue;
            }

            isUpdate = true;
            testModules.set(i, name);
        }

        if (isUpdate) {
            updateData.put(fieldName, testModules);
        }
    }

    /**
     * 查询SR数据
     *
     * @param currentIndex 当前页码
     * @param fields       需要的字段集
     * @return 数据列表
     */
    @Nullable
    private List<MObject> querySrList(int currentIndex, Set<String> fields) {
        //设置条件
        QueryCondition condition = QueryCondition.of()
                .setOrders(ListUtil.toList(Order.of().setProperty(BaseDataEnum.ID.getCode())));
        QueryWrapper wrapper = new QueryWrapper().eq(BaseDataEnum.DELETE_FLAG.getCode(), 0);

        condition.setQueries(QueryWrapper.buildSqlQo(wrapper));
        BaseRequest<QueryCondition> request = new BaseRequest<>();
        request.setParam(condition);
        request.setCurrent(currentIndex);
        request.setSize(500);

        //执行查询
        PagedResult<MObject> page = objectModelCrudI.page(TranscendModel.SR.getCode(), request, true, fields);
        return page.getData();
    }

    //endregion
}
