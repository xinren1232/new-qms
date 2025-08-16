package com.transcend.plm.configcenter.object.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.dao.tool.PageResultTools;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectEventService;
import com.transcend.plm.configcenter.object.infrastructure.repository.CfgObjectEventRepository;
import com.transcend.plm.configcenter.api.model.object.qo.ModelEventQO;
import com.transcend.plm.configcenter.api.model.object.vo.ModelEventVO;
import com.transsion.framework.common.BeanUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CfgObjectEventServiceImpl implements ICfgObjectEventService {

    @Resource
    public CfgObjectEventRepository modelEventRepository;

    @Override
    public PagedResult<ModelEventVO> page(BaseRequest<ModelEventQO> modelEventQO) {
        Page<ModelEventVO> page = new Page<>();
        page.setCurrent(modelEventQO.getCurrent());
        page.setSize(modelEventQO.getSize());
        ModelEventQO eventQO = modelEventQO.getParam();
        List<ModelEventVO> eventVOS = BeanUtil.copy(modelEventRepository.query(page, eventQO).getRecords(), ModelEventVO.class);
        page.setRecords(eventVOS);
        return PageResultTools.create(page, eventVOS);
    }
}
