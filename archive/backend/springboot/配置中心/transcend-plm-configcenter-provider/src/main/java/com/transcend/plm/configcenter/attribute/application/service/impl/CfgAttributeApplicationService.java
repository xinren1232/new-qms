package com.transcend.plm.configcenter.attribute.application.service.impl;

import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import com.transcend.plm.configcenter.attribute.application.service.ICfgAttributeApplicationService;
import com.transcend.plm.configcenter.attribute.domain.service.CfgAttributeDomainService;
import com.transcend.plm.configcenter.attribute.pojo.dto.CfgAttributeDto;
import com.transcend.plm.configcenter.attribute.pojo.vo.CfgAttributeVo;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.common.util.ExcelUtil;
import com.transcend.plm.configcenter.common.util.LocalStringUtil;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.CfgDictionaryRepository;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.enums.ResultEnum;
import com.transsion.framework.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:36
 **/
@Service
@Qualifier("cfgAttributeApplicationService")
public class CfgAttributeApplicationService implements ICfgAttributeApplicationService, IExcelStrategy {
    @Resource
    private CfgAttributeDomainService cfgAttributeDomainService;

    @Resource
    private CfgDictionaryRepository cfgDictionaryRepository;
    /**
     * 保存或新增基础属性
     * @param cfgAttributeDto
     * @return
     */
    @Override
    public CfgAttributeVo saveOrUpdate(CfgAttributeDto cfgAttributeDto) {
        Assert.notNull(cfgAttributeDto,"attribute is null");
        cfgAttributeDto.setUpdatedTime(new Date());
        return StringUtil.isBlank(cfgAttributeDto.getBid()) ? cfgAttributeDomainService.save(cfgAttributeDto) : cfgAttributeDomainService.update(cfgAttributeDto);
    }

    @Override
    public List<Object> getExportExcelData(Object param) {
        List<CfgAttributeVo> cfgAttributeVos = cfgAttributeDomainService.listAll();
        List<String> heardList = new ArrayList<>();
        List<CfgDictionaryItemVo> cfgDictionaryItemVos = cfgDictionaryRepository.listDictionaryItemByDictionaryName("属性分组");
        Map<String,String> groupMap = new HashMap<>();
        for(CfgDictionaryItemVo cfgDictionaryItemVo:cfgDictionaryItemVos){
            groupMap.put(cfgDictionaryItemVo.getKeyCode(),cfgDictionaryItemVo.get("zh")+"");
        }
        Arrays.asList("属性名称","属性编码","属性组","数据类型","更新人","更新时间").forEach(heardList::add);
        List<List<Object>> dataList = new ArrayList<>();
        for (CfgAttributeVo cfgAttributeVo:cfgAttributeVos) {
            List<Object> data = new ArrayList<>();
            data.add(cfgAttributeVo.getName());
            data.add(cfgAttributeVo.getCode());
            data.add(groupMap.getOrDefault(cfgAttributeVo.getGroupName(),cfgAttributeVo.getGroupName()));
            data.add(cfgAttributeVo.getDataType());
            data.add(cfgAttributeVo.getCreatedBy());
            data.add(cfgAttributeVo.getUpdatedTime());
            dataList.add(data);
        }
        List<Object> result = new ArrayList<>();
        result.add(heardList);
        result.add(dataList);
        return result;
    }

    @Override
    public boolean importData(List<Map<Integer,Object>> dataList, ImportDto importDto) {
        List<CfgAttributeDto> cfgAttributeDtos = new ArrayList<>();
        String jobNumber = SsoHelper.getJobNumber();
        for(Map<Integer,Object> map : dataList){
            CfgAttributeDto cfgAttributeDto = new CfgAttributeDto();
            if(map.get(0) == null || map.get(1) == null || map.get(2) == null || map.get(3) == null){
                throw new BusinessException("必填数据不能为空");
            }
            cfgAttributeDto.setName(map.get(0)+"");
            cfgAttributeDto.setCode(map.get(1)+"");
            //判断code是否满足条件
            if(!LocalStringUtil.checkCode(cfgAttributeDto.getCode())){
                throw new BusinessException("编码不满足规则");
            }
            cfgAttributeDto.setDataType(map.get(2)+"");
            cfgAttributeDto.setGroupName(map.get(3)+"");
            cfgAttributeDto.setCreatedTime(new Date());
            cfgAttributeDto.setUpdatedTime(new Date());
            cfgAttributeDto.setBid(SnowflakeIdWorker.nextIdStr());
            cfgAttributeDto.setUpdatedBy(jobNumber);
            cfgAttributeDto.setCreatedBy(jobNumber);
            cfgAttributeDtos.add(cfgAttributeDto);
        }
        if(CollectionUtils.isNotEmpty(cfgAttributeDtos)){
            cfgAttributeDomainService.bulkAdd(cfgAttributeDtos);
        }
        return true;
    }
}
