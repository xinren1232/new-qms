package com.transcend.plm.configcenter.table.application.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableAndCheckVo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import com.transsion.framework.dto.BaseRequest;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 对象应用组合服务接口
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/2/20 10:24
 * @since 1.0
 */
public interface ICfgTableApplicationService {

    PagedResult<CfgTableVo> page(BaseRequest<CfgTableQo> pageQo);

    CfgTableVo getByBid(String bid);

    CfgTableVo add(CfgTableDto dto);

    CfgTableVo getTableAndAttributeByBid(String bid);

    Boolean blukSaveOrUpdateTableAndAttribute(List<CfgTableDto> dtos);

    List<CfgTableAndCheckVo> checkReturnBlukTableAndAttribute(List<CfgTableDto> dtos);

    CfgTableVo saveOrUpdate(CfgTableDto dto);

    CfgTableVo saveOrUpdateTableAndAttribute(CfgTableDto dto);

    Boolean changeEnableFlag(String bid, Integer enableFlag);

    Boolean logicalDelete(String bid);

    List<CfgTableVo> listTableAndAttributeByTableNamesAndEnableFlags(@NotNull List<String> codes, List<Integer> enableFlags);
}
