package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmDepartmentBackupPo;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmDepartmentBackupMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmDepartmentBackupService;
import com.transcend.plm.datadriven.apm.permission.service.IPlatformUserWrapper;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.MapWrapper;
import lombok.AllArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author xin.wu2
 * @description 针对表【apm_department_backup(本地部门备份表)】的数据库操作Service实现
 * createDate 2025-04-25 14:54:26
 */
@Service
@AllArgsConstructor
public class ApmDepartmentBackupServiceImpl extends ServiceImpl<ApmDepartmentBackupMapper, ApmDepartmentBackupPo>
        implements ApmDepartmentBackupService {
    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    private IPlatformUserWrapper platformUserWrapper;

    @Async
    @Override
    public void syncDepartment(Long departmentId) {
        try {
            lock();
            List<Tree<Long>> treeList = platformUserWrapper.queryChildDept(departmentId);
            Assert.notEmpty(treeList, "同步部门为空");
            List<Tree<Long>> flatList = new ArrayList<>();
            flattenTree(treeList, flatList);

            List<ApmDepartmentBackupPo> saveOrUpdateList = flatList.stream().map(MapWrapper::new).map(node -> {
                ApmDepartmentBackupPo po = new ApmDepartmentBackupPo();
                po.setId(node.getLong("id"));
                po.setBid(node.getStr("id"));
                po.setName(node.getStr("name"));
                po.setParentBid(node.getStr("parentId"));
                po.setDeptNo(node.getStr("deptNo"));
                po.setParentNo(node.getStr("parentNo"));
                po.setLevel(node.getInt("level"));
                return po;
            }).collect(Collectors.toList());

            ApmDepartmentBackupService proxy = (ApmDepartmentBackupService) AopContext.currentProxy();

            CollUtil.split(saveOrUpdateList,200).forEach(proxy::saveOrUpdateBatch);

        } finally {
            unlock();
        }
    }



    /**
     * 递归展平树
     *
     * @param trees    原始树结构
     * @param flatList 展平后的列表
     */
    private static <T> void flattenTree(List<Tree<T>> trees, List<Tree<T>> flatList) {
        for (Tree<T> tree : trees) {
            // 添加当前节点
            flatList.add(tree);
            if (tree.getChildren() != null) {
                // 递归处理子节点
                flattenTree(tree.getChildren(), flatList);
            }
        }
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




