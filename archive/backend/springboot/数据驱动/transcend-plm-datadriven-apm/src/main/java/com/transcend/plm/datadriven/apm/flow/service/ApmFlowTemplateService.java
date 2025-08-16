package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplate;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmFlowTemplateService extends IService<ApmFlowTemplate> {

    /**
     * 根据业务ID更新ApmFlowTemplate对象。
     *
     * @param apmFlowTemplate ApmFlowTemplate对象
     * @return 如果更新成功，返回true；否则返回false
     */
    boolean updateByBid(ApmFlowTemplate apmFlowTemplate);

    /**
     * 根据空间应用业务ID查询对应的ApmFlowTemplate列表。
     *
     * @param spaceAppBid 空间应用业务ID
     * @return 包含ApmFlowTemplate对象的列表
     */
    List<ApmFlowTemplate> listBySpaceAppBid(String spaceAppBid);

    /**
     * 根据业务ID获取ApmFlowTemplate对象。
     *
     * @param bid 业务ID，字符串类型
     * @return 匹配的ApmFlowTemplate对象，如果找不到匹配项则返回null
     */
    ApmFlowTemplate getByBid(String bid);

    /**
     * 删除指定模板的方法。
     *
     * @param templateBid 模板的业务ID
     * @return 如果删除成功，返回true；否则返回false
     */
    boolean delete(String templateBid);

    /**
     *
     * 方法描述
     * @param spaceAppBid spaceAppBid
     * @return 返回值
     */
    long getStateFlowCount(String spaceAppBid);

    /**
     *
     * 方法描述
     * @param spaceAppBid spaceAppBid
     * @return 返回值
     */
    List<ApmFlowTemplate> listStateFlowBySpaceAppBid(String spaceAppBid);
}
