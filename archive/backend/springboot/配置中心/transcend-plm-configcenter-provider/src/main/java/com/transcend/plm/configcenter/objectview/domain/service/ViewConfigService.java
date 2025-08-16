package com.transcend.plm.configcenter.objectview.domain.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.dto.CfgViewConfigDto;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.qo.CfgViewConfigQo;
import com.transcend.plm.configcenter.objectview.infrastructure.pojo.vo.CfgViewConfigVo;
import com.transcend.plm.configcenter.objectview.infrastructure.repository.CfgViewConfigRepository;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ViewConfigService {
    @Resource
    private CfgViewConfigRepository cfgViewConfigRepository;

    public boolean add(CfgViewConfigDto cfgViewConfigDto) {
        return cfgViewConfigRepository.add(cfgViewConfigDto);
    }

    public boolean edit(CfgViewConfigDto cfgViewConfigDto) {
        return cfgViewConfigRepository.edit(cfgViewConfigDto);
    }

    public PagedResult<CfgViewConfigVo> page(BaseRequest<CfgViewConfigQo> pageQo) {
        return cfgViewConfigRepository.page(pageQo);
    }

    public boolean delete(String bid) {
        return cfgViewConfigRepository.delete(bid);
    }

    public boolean setEnableFlag(CfgViewConfigDto cfgViewConfigDto) {
        return cfgViewConfigRepository.setEnableFlag(cfgViewConfigDto);
    }

    public boolean editViewInfo(CfgViewConfigDto cfgViewConfigDto){
        return cfgViewConfigRepository.editViewInfo(cfgViewConfigDto);
    }

    public boolean copy(CfgViewConfigDto cfgViewConfigDto) {
        return cfgViewConfigRepository.copy(cfgViewConfigDto);
    }

    public CfgViewConfigVo getOne(String bid) {
        return cfgViewConfigRepository.getOne(bid);
    }

    public List<CfgViewConfigVo> findViewByModelCode(String modelCode){
        return cfgViewConfigRepository.findViewByModelCode(modelCode);
    }
}
