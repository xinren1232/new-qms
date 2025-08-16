package com.transcend.plm.configcenter.table.pojo;

import com.transcend.plm.configcenter.api.model.table.qo.CfgTableQo;
import com.transcend.plm.configcenter.api.model.table.vo.CfgTableVo;
import com.transcend.plm.configcenter.table.infrastructure.repository.po.CfgTablePo;
import com.transcend.plm.configcenter.table.pojo.dto.CfgTableDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-25 11:06
 **/
@Mapper
public interface CfgTableConverter {

    CfgTableConverter INSTANCE = Mappers.getMapper(CfgTableConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgTablePo dto2po(CfgTableDto dto);

    /**
     * do 转化为 vo
     * @param cfgAttribute
     * @return
     */
    CfgTableVo po2vo(CfgTablePo cfgAttribute);

    /**
     * qo 转化为 po
     * @param cfgTableQo
     * @return
     */
    CfgTablePo qo2po(CfgTableQo cfgTableQo);

    /**
     * do 转化为 vo
     * @param cfgAttributes
     * @return
     */
    List<CfgTableVo> pos2vos(List<CfgTablePo> cfgAttributes);

    /**
     * dto 批量转化为 po
     * @param cfgAttributeDtos
     * @return
     */
    List<CfgTablePo> dtos2pos(List<CfgTableDto> cfgAttributeDtos);

    CfgTableDto vo2dto(CfgTableVo cfgTableVo);
}
