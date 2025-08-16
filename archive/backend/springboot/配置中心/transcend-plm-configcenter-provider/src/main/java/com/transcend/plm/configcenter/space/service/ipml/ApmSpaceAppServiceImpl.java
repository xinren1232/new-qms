package com.transcend.plm.configcenter.space.service.ipml;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.space.repository.mapper.ApmSpaceAppMapper;
import com.transcend.plm.configcenter.space.repository.po.ApmSpaceApp;
import com.transcend.plm.configcenter.space.service.ApmSpaceAppService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ApmSpaceAppServiceImpl extends ServiceImpl<ApmSpaceAppMapper, ApmSpaceApp>
        implements ApmSpaceAppService {

    @Override
    public List<ApmSpaceApp> getByMc(String modelCode) {
        if (StringUtils.isEmpty(modelCode)){
            log.error("查询参数 modelCode 为空");
            return Collections.emptyList();
        }
        return baseMapper.getByMc(modelCode);
    }

    @Override
    public List<ApmSpaceApp> getByMcs(List<String> modelCodes) {
        if (CollectionUtils.isEmpty(modelCodes)){
            log.error("查询参数 modelCode 为空");
            return Collections.emptyList();
        }
        return baseMapper.getByMcs(modelCodes);
    }
}




