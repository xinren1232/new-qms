package com.transcend.plm.configcenter.code.domain.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.transcend.plm.configcenter.code.infrastructure.repository.CfgCodeRuleRepository;
import com.transcend.plm.configcenter.code.infrastructure.repository.po.CfgCodeRulePo;
import com.transcend.plm.configcenter.code.pojo.dto.CfgCodeRulePoDto;
import com.transcend.plm.configcenter.code.pojo.qo.CfgCodeRulePoQo;
import com.transcend.plm.configcenter.code.pojo.vo.CfgCodeRulePoVo;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.common.pojo.dto.ImportDto;
import com.transcend.plm.configcenter.common.service.IExcelStrategy;
import com.transsion.framework.common.StringUtil;
import com.transsion.framework.dto.BaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
@Service
public class CfgCodeRuleDomainService implements IExcelStrategy {
    @Resource
    private CfgCodeRuleRepository repository;

    public CfgCodeRulePoVo getByBid(String bid) {
        return repository.getByBid(bid);
    }

    public PagedResult<CfgCodeRulePoVo> page(BaseRequest<CfgCodeRulePoQo> pageQo) {
        return repository.page(pageQo);
    }

    public List<CfgCodeRulePoVo> bulkAdd(List<CfgCodeRulePoDto> cfgCodeRuleDtos) {
        return repository.bulkAdd(cfgCodeRuleDtos);
    }

    public boolean logicalDeleteByBid(String bid) {
        return repository.logicalDeleteByBid(bid);
    }

    public CfgCodeRulePoVo saveOrUpdate(CfgCodeRulePoDto cfgCodeRuleDto) {
        return StringUtil.isBlank(cfgCodeRuleDto.getBid()) ? repository.save(cfgCodeRuleDto) : repository.update(cfgCodeRuleDto);
    }

    @Override
    public List<Object> getExportExcelData(Object param) {
        CfgCodeRulePoQo qo = JSON.parseObject(JSON.toJSONString(param), CfgCodeRulePoQo.class);
        List<CfgCodeRulePo> cfgCodeRulePos = repository.listByQo(qo);
        List<String> header = Lists.newArrayList("编码名称", "状态", "更新人", "更新时间", "说明");
        List<List<Object>> data = Lists.newArrayList();
        cfgCodeRulePos.forEach(cfgCodeRulePo -> {
            List<Object> rowData = Lists.newArrayList();
            rowData.add(cfgCodeRulePo.getName());
            rowData.add(cfgCodeRulePo.getEnableFlag() == 1 ? "启用" : "禁用");
            rowData.add(cfgCodeRulePo.getUpdatedBy());
            rowData.add(cfgCodeRulePo.getUpdatedTime());
            rowData.add(cfgCodeRulePo.getDescription());
            data.add(rowData);
        });
        List<Object> result = Lists.newArrayList();
        result.add(header);
        result.add(data);
        return result;
    }

    @Override
    public boolean importData(List<Map<Integer,Object>> dataList, ImportDto importDto) {
        return false;
    }
}
