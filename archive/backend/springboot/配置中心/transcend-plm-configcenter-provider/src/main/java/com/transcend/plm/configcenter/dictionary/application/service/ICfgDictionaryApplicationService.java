package com.transcend.plm.configcenter.dictionary.application.service;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryListQo;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryAndCheckVo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryVo;
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
public interface ICfgDictionaryApplicationService {

    PagedResult<CfgDictionaryVo> page(BaseRequest<CfgDictionaryQo> pageQo);

    CfgDictionaryVo getByBid(String bid);

    CfgDictionaryVo add(CfgDictionaryDto dto);

    CfgDictionaryVo getDictionaryAndItemByBid(String bid);

    Boolean blukSaveOrUpdateDictionaryAndItem(List<CfgDictionaryDto> dtos);

    List<CfgDictionaryAndCheckVo> checkReturnBlukDictionaryAndItem(List<CfgDictionaryDto> dtos);

    CfgDictionaryVo saveOrUpdate(CfgDictionaryDto dto);

    CfgDictionaryVo saveOrUpdateDictionaryAndItem(CfgDictionaryDto dto);

    Boolean changeEnableFlag(String bid, Integer enableFlag);

    Boolean logicalDelete(String bid);

    List<CfgDictionaryVo> listDictionaryAndItemByCodesAndEnableFlags(@NotNull List<String> codes, List<Integer> enableFlags);
}
