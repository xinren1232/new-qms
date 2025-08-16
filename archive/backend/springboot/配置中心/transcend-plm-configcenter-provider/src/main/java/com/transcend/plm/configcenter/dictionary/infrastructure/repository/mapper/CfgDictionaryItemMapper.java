package com.transcend.plm.configcenter.dictionary.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryDetail;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryItemPo;
import com.transcend.plm.configcenter.dictionary.infrastructure.repository.po.CfgDictionaryPo;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryDto;
import com.transcend.plm.configcenter.api.model.dictionary.dto.CfgDictionaryItemDto;
import com.transcend.plm.configcenter.api.model.dictionary.qo.CfgDictionaryListQo;
import com.transcend.plm.configcenter.api.model.dictionary.vo.CfgDictionaryItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface CfgDictionaryItemMapper extends BaseMapper<CfgDictionaryItemPo> {

    int blukUpdateByBid(@Param("items") List<CfgDictionaryItemPo> cfgDictionaryItems);

    List<CfgDictionaryItemPo> listDictionaryItemByDictionaryBids(@Param("dictioanryBids") Set<String> bids,
                                                                 @Param("enableFlag") Integer enableFlag);

    List<CfgDictionaryItemPo> listDictionaryItemByDictionaryName(@Param("name") String name);

    List<CfgDictionaryDetail> listDictionaryDetails();

}
