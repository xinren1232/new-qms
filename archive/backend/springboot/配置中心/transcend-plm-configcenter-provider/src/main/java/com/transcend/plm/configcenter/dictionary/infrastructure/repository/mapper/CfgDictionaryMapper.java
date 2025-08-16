package com.transcend.plm.configcenter.dictionary.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryPo;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryListQo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CfgDictionaryMapper extends BaseMapper<CfgDictionaryPo> {

	CfgDictionaryPo getByBid(String bid);

	List<CfgDictionaryPo> list(CfgDictionaryListQo dictionaryListQo);


	CfgDictionaryPo getByCode(String code);

    List<CfgDictionaryPo> listByByCodesAndEnableFlags(@Param("codes") List<String> codes,
														  @Param("enableFlags") List<Integer> enableFlags);
}
