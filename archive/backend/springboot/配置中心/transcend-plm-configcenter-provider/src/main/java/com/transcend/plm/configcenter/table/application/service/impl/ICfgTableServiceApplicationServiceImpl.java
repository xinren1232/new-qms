package com.transcend.plm.configcenter.table.application.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.common.util.CollectionUtils;
import com.transcend.plm.configcenter.table.application.service.ICfgTableApplicationService;
import com.transcend.plm.configcenter.table.domain.service.CfgTableDomainService;
import com.transcend.plm.configcenter.table.infrastructure.repository.CfgTableRepository;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAndCheckVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAttributeVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.table.pojo.CfgTableConverter;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import com.transsion.framework.dto.BaseRequest;
import com.transsion.framework.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对象应用组合实现服务
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/2/20 10:26
 * @since 1.0
 */
@Service
public class ICfgTableServiceApplicationServiceImpl implements ICfgTableApplicationService {

    @Resource
    private CfgTableDomainService cfgTableDomainService;
    @Resource
    private CfgTableRepository cfgTableRepository;

    @Override
    public PagedResult<CfgTableVo> page(BaseRequest<CfgTableQo> pageQo) {
        return cfgTableDomainService.page(pageQo);
    }

    @Override
    public CfgTableVo getByBid(String modelCode) {
        return cfgTableDomainService.getByBid(modelCode);
    }

    @Override
    public CfgTableVo add(CfgTableDto dto) {
        return cfgTableDomainService.add(dto);
    }

    @Override
    public CfgTableVo getTableAndAttributeByBid(String bid) {
        return getTableAndAttributeByBid(bid);
    }

    @Override
    public Boolean blukSaveOrUpdateTableAndAttribute(List<CfgTableDto> dtos) {

        return null;
    }

    @Override
    public List<CfgTableAndCheckVo> checkReturnBlukTableAndAttribute(List<CfgTableDto> dtos) {
        return null;
    }

    @Override
    public CfgTableVo saveOrUpdate(CfgTableDto dto) {
        return cfgTableDomainService.saveOrUpdate(dto);
    }

    @Override
    public CfgTableVo saveOrUpdateTableAndAttribute(CfgTableDto dto) {
        return cfgTableDomainService.saveOrUpdateTableAndAttribute(dto);
    }

    @Override
    public Boolean changeEnableFlag(String bid, Integer enableFlag) {
        CfgTableVo cfgTableVo = getByBid(bid);
        cfgTableVo.setEnableFlag(enableFlag);
        CfgTableDto dto = CfgTableConverter.INSTANCE.vo2dto(cfgTableVo);
        update(dto);
        return true;
    }

    @Override
    public Boolean logicalDelete(String bid) {
        return cfgTableDomainService.logicalDelete(bid);
    }

    private CfgTableVo update(CfgTableDto dto) {
        return cfgTableDomainService.update(dto);
    }


    /**
     * 此接口后期压力大，需要做缓存 TODO
     *
     * @param tableNames
     * @param enableFlags
     * @return
     */
    @Override
    public List<CfgTableVo> listTableAndAttributeByTableNamesAndEnableFlags(@NotNull List<String> tableNames, List<Integer> enableFlags) {
        // 1.获取表主信息
        List<CfgTableVo> tableVos = cfgTableDomainService.listByTableNamesAndEnableFlags(tableNames, enableFlags);
        if (CollectionUtils.isEmpty(tableVos)){
            throw new BusinessException("500", "找不到表！");
        }
        // 1.1收集bid集合
        Set<String> tableBids = tableVos.stream().map(CfgTableVo::getBid).collect(Collectors.toSet());
        // 2.获取表属性信息
        List<CfgTableAttributeVo> dictionaryItemVos = cfgTableDomainService.listDictionaryItemByDictionaryBids(tableBids);
        // 2.1 把表属性以表bid为key,属性list为value
        Map<String, List<CfgTableAttributeVo>> itemMap = Maps.newHashMap();
        dictionaryItemVos.forEach(cfgDictionaryItemVo -> {
                    String tableBid = cfgDictionaryItemVo.getTableBid();
                    List<CfgTableAttributeVo> cfgDictionaryItemVos = itemMap.get(tableBid);
                    if (CollectionUtils.isEmpty(cfgDictionaryItemVos)) {
                        cfgDictionaryItemVos = Lists.newArrayList();
                        itemMap.put(tableBid, cfgDictionaryItemVos);
                    }
                    // 补充属性
                    cfgDictionaryItemVos.add(cfgDictionaryItemVo);
                }
        );
        // 3. 给表主信息组合属性信息
        tableVos.forEach(cfgDictionaryVo -> {
            cfgDictionaryVo.setAttributes(itemMap.get(cfgDictionaryVo.getBid()));
        });
        return tableVos;
    }

}
