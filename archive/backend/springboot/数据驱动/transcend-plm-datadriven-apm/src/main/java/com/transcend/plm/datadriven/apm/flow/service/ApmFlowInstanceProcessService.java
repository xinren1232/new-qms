package com.transcend.plm.datadriven.apm.flow.service;

import com.transcend.plm.datadriven.apm.flow.pojo.dto.ApmFlowInstanceProcessDto;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceProcessVo;

import java.util.List;

/**
 * TODO 描述
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/10/28 13:59
 * @since 1.0
 */
public interface ApmFlowInstanceProcessService {
    /**
     * 根据流程实例标识获取流程实例过程列表。
     *
     * @param instanceBid 流程实例标识
     * @return 流程实例过程列表
     */
    List<ApmFlowInstanceProcessVo> listByInstanceBid(String instanceBid);

    /**
     * 保存流程实例过程。
     *
     * @param dto 流程实例过程的数据传输对象
     * @return 是否保存成功，成功为true，失败为false
     */
    Boolean save(ApmFlowInstanceProcessDto dto);
}
