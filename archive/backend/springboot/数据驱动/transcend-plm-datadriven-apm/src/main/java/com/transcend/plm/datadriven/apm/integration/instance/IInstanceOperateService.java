package com.transcend.plm.datadriven.apm.integration.instance;

import com.transcend.plm.datadriven.api.model.MBaseData;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateMessage;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 实例操作接口
 * @createTime 2023-12-20 09:28:00
 */
public interface IInstanceOperateService {
    /**
     * 新增
     *
     * @param message 消息
     * @return MBaseData 返回结果
     */
    boolean add(InstanceOperateMessage message);

    /**
     * 修改
     *
     * @param message 消息
     * @return MBaseData 返回结果
     */
    boolean update(InstanceOperateMessage message);

    /**
     * 删除
     *
     * @param message 消息
     * @return MBaseData 返回结果
     */
    boolean delete(InstanceOperateMessage message);
}
