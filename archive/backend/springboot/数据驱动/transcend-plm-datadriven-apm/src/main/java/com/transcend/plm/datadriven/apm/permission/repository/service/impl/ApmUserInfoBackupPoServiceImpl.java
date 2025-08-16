package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONValidator;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.open.entity.EmployeeInfo;
import com.transcend.framework.open.service.OpenUserService;
import com.transcend.plm.datadriven.api.model.*;
import com.transcend.plm.datadriven.api.model.vo.ThreeDeptVO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmUserInfoBackupSyncAo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmUserInfoBackupPo;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmUserInfoBackupMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleService;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmUserInfoBackupService;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transsion.framework.dto.BaseRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author xin.wu2
 * @description 针对表【apm_user_info_backup(本地用户备份表,用于BI视图展现)】的数据库操作Service实现
 * createDate 2025-06-27 11:08:43
 */
@Service
@AllArgsConstructor
public class ApmUserInfoBackupPoServiceImpl extends ServiceImpl<ApmUserInfoBackupMapper, ApmUserInfoBackupPo>
        implements ApmUserInfoBackupService {
    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    private final ObjectModelStandardI<MObject> objectModelStandardI;
    private final OpenUserService openUserService;
    private final ApmRoleService apmRoleService;


    @Async
    @Override
    public void syncUserInfo(ApmUserInfoBackupSyncAo syncAo) {

        try {
            lock();
            //准备参数
            boolean fullRefresh = Boolean.TRUE.equals(syncAo.getFullRefresh());
            List<ApmUserInfoBackupSyncAo.SyncParams> syncParams = syncAo.getSyncParams().stream()
                    .filter(Objects::nonNull)
                    .filter(param -> StringUtils.isNotBlank(param.getModelCode()) && CollUtil.isNotEmpty(param.getFields()))
                    .collect(Collectors.toList());

            Set<String> processed = new HashSet<>();
            syncParams.forEach(param -> sync(param, processed, fullRefresh));
        } finally {
            unlock();
        }

    }

    @SuppressWarnings("all")
    private void sync(ApmUserInfoBackupSyncAo.SyncParams param, Set<String> processed, boolean fullRefresh) {
        int size = 1000, currentIndex = 1;
        Set<String> fields = new HashSet<>(param.getFields());
        PagedResult<MObject> page;
        do {
            QueryWrapper wrapper = new QueryWrapper().eq(BaseDataEnum.DELETE_FLAG.getCode(), 0);
            QueryCondition condition = QueryCondition.of()
                    .setOrders(ListUtil.toList(Order.of().setProperty(BaseDataEnum.ID.getCode())));
            condition.setQueries(QueryWrapper.buildSqlQo(wrapper));

            BaseRequest<QueryCondition> request = new BaseRequest<>();
            request.setParam(condition);
            request.setCurrent(currentIndex);
            request.setSize(size);

            //执行查询
            page = objectModelStandardI.page(param.getModelCode(), request, true, fields);

            List<String> jobNumberList = page.getData().stream()
                    .flatMap(obj -> fields.stream().map(obj::get).filter(Objects::nonNull))
                    .map(ApmUserInfoBackupPoServiceImpl::convertToList).flatMap(Collection::stream)
                    .distinct().filter(processed::add).collect(Collectors.toList());

            if (!fullRefresh && !jobNumberList.isEmpty()) {
                Set<String> existsList = new HashSet<>(this.listObjs(
                        this.lambdaQuery().select(ApmUserInfoBackupPo::getId).getWrapper()
                                .in(ApmUserInfoBackupPo::getId, jobNumberList), String::valueOf));
                jobNumberList = jobNumberList.stream().filter(jobNumber -> !existsList.contains(jobNumber))
                        .collect(Collectors.toList());
            }

            if (jobNumberList.isEmpty()) {
                continue;
            }

            List<List<String>> splitList = CollUtil.split(jobNumberList, 50);

            splitList.forEach(list -> {
                Map<String, String> employeeMap = openUserService.batchFindByEmoNo(list)
                        .stream().collect(Collectors.toMap(EmployeeInfo::getJobNumber, EmployeeInfo::getName, (v1, v2) -> v1));
                List<ApmUserInfoBackupPo> poList = list.stream().map(jobNumber -> {
                    try {
                        ThreeDeptVO deptVO = apmRoleService.queryThreeDeptInfo(jobNumber);
                        if (deptVO == null || deptVO.getFirstDeptId() == null) {
                            return null;
                        }
                        ApmUserInfoBackupPo po = new ApmUserInfoBackupPo();
                        po.setId(jobNumber);
                        po.setName(employeeMap.getOrDefault(jobNumber, jobNumber));
                        po.setLevel1DepartmentId(deptVO.getFirstDeptId());
                        po.setLevel1DepartmentName(deptVO.getFirstDeptName());
                        po.setLevel2DepartmentId(deptVO.getSecondDeptId());
                        po.setLevel2DepartmentName(deptVO.getSecondDeptName());
                        po.setLevel3DepartmentId(deptVO.getThirdDeptId());
                        po.setLevel3DepartmentName(deptVO.getThirdDeptName());
                        return po;
                    } catch (Exception ignore) {
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                this.saveOrUpdateBatch(poList);

            });

            currentIndex++;
        } while (currentIndex <= page.getPages());
    }

    @Nonnull
    private static List<String> convertToList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }

        // 如果是字符串类型
        if (value instanceof String) {
            String strValue = (String) value;
            // 检查是否是 JSON 数组
            try (JSONValidator validator = JSONValidator.from(strValue)) {
                if (validator.validate() && validator.getType() == JSONValidator.Type.Array) {
                    return JSONArray.parseArray(strValue, String.class);
                }
            } catch (Exception ignored) {
                return Collections.emptyList();
            }

            // 检查是否是逗号分隔的字符串
            String separator = ",";
            if (strValue.contains(separator)) {
                return Arrays.asList(strValue.split(separator));
            }

            // 单个字符串
            return Collections.singletonList(strValue);
        }

        // 如果是集合类型
        if (value instanceof Collection) {
            return Convert.toList(String.class, value);
        }

        // 如果是数组类型
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            return Arrays.stream(array).map(String::valueOf).collect(Collectors.toList());
        }

        // 其他类型，返回空列表
        return Collections.emptyList();
    }

    /**
     * 获取锁，获取失败抛出异常
     */
    private void lock() {
        if (!isLocked.compareAndSet(false, true)) {
            throw new IllegalStateException("Lock is already acquired by another thread!");
        }
    }

    /**
     * 释放锁
     */
    private void unlock() {
        if (!isLocked.compareAndSet(true, false)) {
            throw new IllegalStateException("Cannot unlock - Lock is not held by any thread!");
        }
    }


}




