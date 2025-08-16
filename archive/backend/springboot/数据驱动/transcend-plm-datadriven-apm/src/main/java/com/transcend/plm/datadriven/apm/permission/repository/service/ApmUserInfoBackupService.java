package com.transcend.plm.datadriven.apm.permission.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmUserInfoBackupSyncAo;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmUserInfoBackupPo;

/**
 * @author xin.wu2
 * @description 针对表【apm_user_info_backup(本地用户备份表,用于BI视图展现)】的数据库操作Service
 * createDate 2025-06-27 11:08:43
 */
public interface ApmUserInfoBackupService extends IService<ApmUserInfoBackupPo> {


    /**
     * 同步部门信息
     *
     * @param syncAo 同步参数
     */
    void syncUserInfo(ApmUserInfoBackupSyncAo syncAo);
}
