package com.transcend.plm.datadriven.apm.permission.pojo.bo;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 用于定义Identity的访问接口
 * @createTime 2023-09-21 15:03:00
 */
public interface ApmIdentity {
    /**
     *
     * 方法描述
     * @return 返回值
     */
    List<ApmUser> getApmUserList();
}
