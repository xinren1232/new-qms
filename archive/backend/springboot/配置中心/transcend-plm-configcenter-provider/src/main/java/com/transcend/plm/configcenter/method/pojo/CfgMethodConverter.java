package com.transcend.plm.configcenter.method.pojo;

import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgMethod;
import com.transcend.plm.configcenter.method.pojo.dto.CfgMethodDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgMethodQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgMethodVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-24 10:18
 **/
@Mapper
public interface CfgMethodConverter {

    CfgMethodConverter INSTANCE = Mappers.getMapper(CfgMethodConverter.class);

    /**
     * dto2po
     * @param dto
     * @return
     */
    CfgMethod dto2po(CfgMethodDto dto);

    /**
     * po2vo
     * @param po
     * @return
     */
    CfgMethodVo po2vo(CfgMethod po);

    /**
     * qo2po
     * @param param
     * @return
     */
    CfgMethod qo2po(CfgMethodQo param);

    /**
     * pos2vos
     * @param records
     * @return
     */
    List<CfgMethodVo> pos2vos(List<CfgMethod> records);
}
