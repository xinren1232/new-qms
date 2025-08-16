package com.transcend.plm.datadriven.apm.flow.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.flow.pi.domain.PiProductBtConfig;
import com.transcend.plm.datadriven.apm.flow.pi.mapper.PiProductBtConfigMapper;
import com.transcend.plm.datadriven.apm.flow.pi.service.PiProductBtConfigService;
import org.springframework.stereotype.Service;

/**
 * @author unknown
 */
@Service
public class PiProductBtConfigServiceImpl extends ServiceImpl<PiProductBtConfigMapper, PiProductBtConfig>
    implements PiProductBtConfigService {

    @Override
    public String getProductManager(String btDepartmentCode) {
        PiProductBtConfig piProductBtConfig = getOne(new LambdaQueryWrapper<PiProductBtConfig>().eq(PiProductBtConfig::getBtDepartmentCode, btDepartmentCode));
        if(piProductBtConfig != null){
            return piProductBtConfig.getProductManagerNumber();
        }
        return null;
    }

}




