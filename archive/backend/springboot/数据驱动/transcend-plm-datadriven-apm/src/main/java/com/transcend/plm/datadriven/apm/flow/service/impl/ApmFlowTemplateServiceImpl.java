package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowTemplateMapper;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplate;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowTemplateService;
import com.transsion.framework.common.StringUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmFlowTemplateServiceImpl extends ServiceImpl<ApmFlowTemplateMapper, ApmFlowTemplate>
        implements ApmFlowTemplateService {

    @Override
    public boolean updateByBid(ApmFlowTemplate apmFlowTemplate) {
        ApmFlowTemplate thisTemplate = getOne(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getBid, apmFlowTemplate.getBid()));
        thisTemplate.setVersion(apmFlowTemplate.getVersion());
        thisTemplate.setDescription(apmFlowTemplate.getDescription());
        thisTemplate.setName(apmFlowTemplate.getName());
        thisTemplate.setLayout(apmFlowTemplate.getLayout());
        if (StringUtil.isNotBlank(apmFlowTemplate.getModelCode())) {
            thisTemplate.setModelCode(apmFlowTemplate.getModelCode());
        }
        return updateById(thisTemplate);
    }

    @Override
    public long getStateFlowCount(String spaceAppBid) {
        long count = count(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getSpaceAppBid, spaceAppBid).eq(ApmFlowTemplate::getType, "state").eq(ApmFlowTemplate::getDeleteFlag, false));
        return count;
    }


    @Override
    public List<ApmFlowTemplate> listStateFlowBySpaceAppBid(String spaceAppBid) {
        List<ApmFlowTemplate> list = list(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getSpaceAppBid, spaceAppBid).eq(ApmFlowTemplate::getType, "state").eq(ApmFlowTemplate::getDeleteFlag, false));
        return list;
    }

    @Override
    public List<ApmFlowTemplate> listBySpaceAppBid(String spaceAppBid) {
        //这里排除掉状态流程，页面只需要选择工作流程
        List<ApmFlowTemplate> apmFlowTemplates = list(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getSpaceAppBid, spaceAppBid).eq(ApmFlowTemplate::getDeleteFlag, false));
        return apmFlowTemplates;
    }

    @Override
    public boolean delete(String templateBid) {
        return update(Wrappers.<ApmFlowTemplate>lambdaUpdate()
                .set(ApmFlowTemplate::getDeleteFlag, true)
                .set(ApmFlowTemplate::getUpdatedBy, SsoHelper.getJobNumber())
                .set(ApmFlowTemplate::getUpdatedTime, new Date())
                .eq(ApmFlowTemplate::getBid, templateBid));
    }

    @Override
    public ApmFlowTemplate getByBid(String bid) {
        ApmFlowTemplate apmFlowTemplate = getOne(Wrappers.<ApmFlowTemplate>lambdaQuery().eq(ApmFlowTemplate::getBid, bid));
        return apmFlowTemplate;
    }
}




