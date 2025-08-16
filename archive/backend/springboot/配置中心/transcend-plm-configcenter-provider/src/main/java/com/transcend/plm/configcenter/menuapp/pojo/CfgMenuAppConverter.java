package com.transcend.plm.configcenter.menuapp.pojo;

import com.transcend.plm.configcenter.menuapp.infrastructure.repository.po.CfgMenuAppPo;
import com.transcend.plm.configcenter.menuapp.pojo.dto.CfgMenuAppDto;
import com.transcend.plm.configcenter.menuapp.pojo.qo.CfgMenuAppQo;
import com.transcend.plm.configcenter.menuapp.pojo.vo.CfgMenuAppVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author jie.luo1
 * @Version 1.0
 * @Date 2023-02-22 11:06
 **/
@Mapper
public interface CfgMenuAppConverter {

    CfgMenuAppConverter INSTANCE = Mappers.getMapper(CfgMenuAppConverter.class);


    /**
     * dto 转化为 do
     * @param dto
     * @return
     */
    CfgMenuAppPo dto2po(CfgMenuAppDto dto);

    /**
     * do 转化为 vo
     * @param cfgAttribute
     * @return
     */
    CfgMenuAppVo po2vo(CfgMenuAppPo cfgAttribute);

    /**
     * qo 转化为 po
     * @param cfgAttributeQo
     * @return
     */
    CfgMenuAppPo qo2po(CfgMenuAppQo cfgAttributeQo);

    /**
     * do 转化为 vo
     * @param cfgAttributes
     * @return
     */
    List<CfgMenuAppVo> pos2vos(List<CfgMenuAppPo> cfgAttributes);

    CfgMenuAppDto vo2dto(CfgMenuAppVo vo);
}
