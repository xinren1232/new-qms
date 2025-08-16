package com.transcend.plm.configcenter.lifecycle.application.service.impl;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.api.model.lifecycle.qo.LifeCycleStateListQo;
import com.transcend.plm.configcenter.api.model.lifecycle.vo.LifeCycleStateVo;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.util.LocalStringUtil;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.CfgDictionaryRepository;
import com.transcend.plm.configcenter.lifecycle.application.service.ILifeCycleApplicationService;
import com.transcend.plm.configcenter.lifecycle.domain.service.LifeCycleStateService;
import com.transcend.plm.configcenter.lifecycle.domain.service.LifeCycleTemplateService;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.LifeCycleStateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.pojo.dto.TemplateDto;
import com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po.CfgLifeCycleTemplateNodePo;
import com.transcend.plm.configcenter.object.application.service.ICfgObjectLifecycleAppService;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 生命周期应用组合实现服务
 *
 * @author yuanhu.huang
 * @version V1.0.0
 * @date 2023/2/22 10:26
 * @since 1.0
 */
@Service
@Qualifier("lifeCycleApplicationServiceImpl")
public class LifeCycleApplicationServiceImpl implements ILifeCycleApplicationService, IExcelStrategy {
    @Resource
    private LifeCycleStateService lifeCycleStatusService;

    @Resource
    private CfgDictionaryRepository cfgDictionaryRepository;

    @Resource
    private ICfgObjectLifecycleAppService lifeCycleApplicationService;

    @Resource
    private LifeCycleTemplateService lifeCycleTemplateService;

    @Override
    public PagedResult<LifeCycleStateVo> page(BaseRequest<LifeCycleStateListQo> pageQo) {
        return lifeCycleStatusService.page(pageQo);
    }

    @Override
    public LifeCycleStateVo add(LifeCycleStateDto dto) {
        return lifeCycleStatusService.add(dto);
    }

    @Override
    public LifeCycleStateVo edit(LifeCycleStateDto dto) {
        return lifeCycleStatusService.edit(dto);
    }

    @Override
    public boolean delete(String id) {
        return lifeCycleStatusService.delete(id);
    }

    @Override
    public List<Object> getExportExcelData(Object param) {
        LifeCycleStateListQo lifeCycleStateListQo = new LifeCycleStateListQo();
        List<LifeCycleStateVo> list = lifeCycleStatusService.list(lifeCycleStateListQo);
        List<String> heardList = new ArrayList<>();
        Arrays.asList("状态名称","状态编码","分组","状态","更新人","更新时间").forEach(heardList::add);
        List<CfgDictionaryItemVo> cfgDictionaryItemVos = cfgDictionaryRepository.listDictionaryItemByDictionaryName("生命周期状态分组");
        Map<String,String> groupMap = new HashMap<>();
        for(CfgDictionaryItemVo cfgDictionaryItemVo:cfgDictionaryItemVos){
            groupMap.put(cfgDictionaryItemVo.getKeyCode(),cfgDictionaryItemVo.get("zh")+"");
        }
        Map<Integer,String> enableMap = new HashMap<>();
        enableMap.put(0,"未启用");
        enableMap.put(1,"启用");
        enableMap.put(2,"禁用");
        List<List<Object>> dataList = new ArrayList<>();
        list.forEach(lifeCycleStateVo -> {
            List<Object> data = new ArrayList<>();
            data.add(lifeCycleStateVo.getName());
            data.add(lifeCycleStateVo.getCode());
            data.add(groupMap.getOrDefault(lifeCycleStateVo.getGroupCode(),lifeCycleStateVo.getGroupCode()));
            data.add(enableMap.get(lifeCycleStateVo.getEnableFlag()));
            data.add(lifeCycleStateVo.getUpdatedBy());
            data.add(lifeCycleStateVo.getUpdatedTime());
            dataList.add(data);
        });
        List<Object> result = new ArrayList<>();
        result.add(heardList);
        result.add(dataList);
        return result;
    }

    @Override
    public boolean importData(List<Map<Integer,Object>> dataList, ImportDto importDto) {
        List<LifeCycleStateDto> dtos = new ArrayList<>();
        String jobNumber = SsoHelper.getJobNumber();
        for(Map<Integer,Object> map : dataList){
            if(map.get(0) == null || map.get(1) == null || map.get(2) == null ){
                throw new BusinessException("必填数据不能为空");
            }
            if(!LocalStringUtil.checkUpperCode(map.get(1)+"")){
                throw new BusinessException("编码必须大写");
            }
            LifeCycleStateDto lifeCycleStateDto = new LifeCycleStateDto();
            lifeCycleStateDto.setName(map.get(0)+"");
            lifeCycleStateDto.setCode(map.get(1)+"");
            lifeCycleStateDto.setGroupCode(map.get(2)+"");
            if(map.get(3) != null){
                lifeCycleStateDto.setDescription(map.get(3)+"");
            }
            lifeCycleStateDto.setCreatedTime(new Date());
            lifeCycleStateDto.setUpdatedTime(new Date());
            lifeCycleStateDto.setBid(SnowflakeIdWorker.nextIdStr());
            lifeCycleStateDto.setUpdatedBy(jobNumber);
            lifeCycleStateDto.setCreatedBy(jobNumber);
            dtos.add(lifeCycleStateDto);
        }
        if(CollectionUtils.isNotEmpty(dtos)){
            lifeCycleStatusService.saveImport(dtos);
        }
        return true;
    }

    @Override
    public List<CfgLifeCycleTemplateNodePo> getTemplateNodesOrderByLine(TemplateDto templateDto) {
        ObjectModelLifeCycleVO objectModelLifeCycleVO = lifeCycleApplicationService.findObjectLifecycleByModelCode(templateDto.getModelCode());
        if(objectModelLifeCycleVO == null){
            return null;
        }
        templateDto.setTemplateBid(objectModelLifeCycleVO.getLcTemplBid());
        if(objectModelLifeCycleVO.getLcTemplVersion().startsWith("V")){
            templateDto.setVersion(objectModelLifeCycleVO.getLcTemplVersion());
        }else{
            templateDto.setVersion("V"+ objectModelLifeCycleVO.getLcTemplVersion());
        }
        return lifeCycleTemplateService.getCfgLifeCycleTemplateNodePos(templateDto);
    }
}
