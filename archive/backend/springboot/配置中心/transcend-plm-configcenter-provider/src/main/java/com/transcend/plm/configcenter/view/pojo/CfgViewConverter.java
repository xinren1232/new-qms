package com.transcend.plm.configcenter.view.pojo;

import com.transcend.plm.configcenter.view.infrastructure.repository.po.CfgViewPo;
import com.transcend.plm.configcenter.api.model.view.dto.CfgViewDto;
import com.transcend.plm.configcenter.api.model.view.qo.CfgViewQo;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgViewConverter {

    CfgViewConverter INSTANCE = Mappers.getMapper(CfgViewConverter.class);

    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgViewPo dto2po(CfgViewDto dto);

    /**
     * do 转化为 vo
     * @param cfgViewPo
     * @return
     */
    CfgViewVo po2vo(CfgViewPo cfgViewPo);

    /**
     * qo 转化为 po
     * @param cfgViewQo
     * @return
     */
    CfgViewPo qo2po(CfgViewQo cfgViewQo);

    /**
     * do 转化为 vo
     * @param cfgViewPos
     * @return
     */
    List<CfgViewVo> pos2vos(List<CfgViewPo> cfgViewPos);

    /**
     * dto 批量转化为 po
     * @param cfgViewDtos
     * @return
     */
    List<CfgViewPo> dtos2pos(List<CfgViewDto> cfgViewDtos);

    /**
     * vo 转化为 dto
     * @param cfgViewVo
     * @return
     */
    CfgViewDto vo2dto(CfgViewVo cfgViewVo);
}
