package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.flow.pojo.dto.ApmFlowInstanceProcessDto;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowInstanceProcessMapper;
import com.transcend.plm.datadriven.apm.flow.maspstruct.AmpFlowTemplateProcessConverter;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceProcessPo;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceProcessService;
import com.transcend.plm.datadriven.apm.flow.pojo.vo.ApmFlowInstanceProcessVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmFlowInstanceProcessServiceImpl extends ServiceImpl<ApmFlowInstanceProcessMapper, ApmFlowInstanceProcessPo>
        implements ApmFlowInstanceProcessService {

    @Resource
    private ApmFlowInstanceProcessMapper apmFlowInstanceProcessMapper;

    @Override
    public List<ApmFlowInstanceProcessVo> listByInstanceBid(String instanceBid) {
        List<ApmFlowInstanceProcessPo> poList = this.list(Wrappers.<ApmFlowInstanceProcessPo>lambdaQuery()
                .eq(ApmFlowInstanceProcessPo::getFlowInstanceBid, instanceBid)
                .orderBy(true, false, ApmFlowInstanceProcessPo::getEndTime));

        return AmpFlowTemplateProcessConverter.INSTANCE.pos2vos(poList);
    }

    @Override
    public Boolean save(ApmFlowInstanceProcessDto dto) {
        return save(AmpFlowTemplateProcessConverter.INSTANCE.dto2po(dto));
    }
}




