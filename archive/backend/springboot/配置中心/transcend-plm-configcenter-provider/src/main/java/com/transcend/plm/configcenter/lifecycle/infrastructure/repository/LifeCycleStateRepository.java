package com.transcend.plm.configcenter.lifecycle.infrastructure.repository;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.CfgLifeCycleStateConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.LifeCycleStateConverter;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.mapper.LifeCycleStateMapper;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleStatePo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.LifeCycleStatePo;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.service.CfgLifeCycleStateService;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
public class LifeCycleStateRepository {
    @Resource
    private CfgLifeCycleStateService cfgLifeCycleStateService;

    @Resource
    private LifeCycleStateMapper lifeCycleStatusMapper;


    public PagedResult<LifeCycleStateVo> page(BaseRequest<LifeCycleStateListQo> pageQo) {
        return cfgLifeCycleStateService.page(pageQo);
    }

    public List<LifeCycleStateVo> list(LifeCycleStateListQo lifeCycleStatusListQo){
        List<LifeCycleStatePo> poList = lifeCycleStatusMapper.list(lifeCycleStatusListQo);
        List<LifeCycleStateVo> list = LifeCycleStateConverter.INSTANCE.pos2vos(poList);
        return list;
    }

    public List<LifeCycleStateVo> allList(){
        List<LifeCycleStatePo> poList = lifeCycleStatusMapper.allList();
        List<LifeCycleStateVo> list = LifeCycleStateConverter.INSTANCE.pos2vos(poList);
        return list;
    }

    public boolean editByBid(LifeCycleStateDto dto) {
        return cfgLifeCycleStateService.editByBid(dto);
    }

    public LifeCycleStateVo add(LifeCycleStateDto dto) {
        CfgLifeCycleStatePo po = CfgLifeCycleStateConverter.INSTANCE.dto2po(dto);
        po.setUpdatedTime(LocalDateTime.now());
        po.setCreatedTime(LocalDateTime.now());
        //校验 名称或者编码是否重复
        boolean check = cfgLifeCycleStateService.checkNameAndCode(po);
        if(check){
            throw new BusinessException("名称或者编码重复");
        }
        boolean res = cfgLifeCycleStateService.save(po);
        if (res){
            return CfgLifeCycleStateConverter.INSTANCE.po2vo(po);
        }
        return null;
    }

    public List<LifeCycleStateVo> addBatch(List<LifeCycleStateDto> dtos) {
        List<CfgLifeCycleStatePo> list = CfgLifeCycleStateConverter.INSTANCE.dtos2pos(dtos);
        for(CfgLifeCycleStatePo po:list){
            po.setUpdatedTime(LocalDateTime.now());
            po.setCreatedTime(LocalDateTime.now());
        }
        boolean check = cfgLifeCycleStateService.checkNameAndCodes(list);
        if(check){
            throw new BusinessException("名称或者编码重复");
        }
        boolean res = cfgLifeCycleStateService.saveBatch(list);
        if (res){
            return CfgLifeCycleStateConverter.INSTANCE.pos2vos(list);
        }
        return null;
    }

    public LifeCycleStateVo edit(LifeCycleStateDto dto) {
        CfgLifeCycleStatePo po = CfgLifeCycleStateConverter.INSTANCE.dto2po(dto);
        po.setUpdatedTime(LocalDateTime.now());
        CfgLifeCycleStatePo cfgLifeCycleStatePo = cfgLifeCycleStateService.getByBid(dto.getBid());
        if(cfgLifeCycleStatePo == null){
            throw new BusinessException("数据不存在");
        }
        if(StringUtils.isNotEmpty(dto.getName()) && !dto.getName().equals(cfgLifeCycleStatePo.getName())){
            boolean check = cfgLifeCycleStateService.checkName(dto.getName());
            if(check){
                throw new BusinessException("名称重复");
            }
        }
        if(StringUtils.isNotEmpty(dto.getCode()) && !dto.getCode().equals(cfgLifeCycleStatePo.getCode())){
            boolean check = cfgLifeCycleStateService.checkCode(dto.getCode());
            if(check){
                throw new BusinessException("编码重复");
            }
        }

        boolean res = cfgLifeCycleStateService.editByBid(dto);
        if (res){
            return CfgLifeCycleStateConverter.INSTANCE.po2vo(po);
        }
        return null;
    }

    public boolean delete(String bid){
        return cfgLifeCycleStateService.deleteByBid(bid);
    }

    public List<LifeCycleStateVo> queryByCodes(List<String> codeList) {
        List<LifeCycleStatePo> poList = lifeCycleStatusMapper.queryByCodes(codeList);
        return LifeCycleStateConverter.INSTANCE.pos2vos(poList);
    }
}
