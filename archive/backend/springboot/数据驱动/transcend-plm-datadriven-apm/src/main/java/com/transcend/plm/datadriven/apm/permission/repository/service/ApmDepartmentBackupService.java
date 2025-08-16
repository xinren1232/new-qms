package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmDepartmentBackupPo;

/**
* @author xin.wu2
* @description 针对表【apm_department_backup(本地部门备份表)】的数据库操作Service
* createDate 2025-04-25 14:54:26
*/
public interface ApmDepartmentBackupService extends IService<ApmDepartmentBackupPo> {


    /**
     * 同步部门信息
     * @param departmentId 部门id
     */
    void syncDepartment(Long departmentId);

}
