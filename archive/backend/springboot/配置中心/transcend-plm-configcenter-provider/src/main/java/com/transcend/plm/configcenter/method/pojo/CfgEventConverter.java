package com.transcend.plm.configcenter.method.pojo;

import com.transcend.plm.configcenter.method.infrastructure.repository.po.CfgEvent;
import com.transcend.plm.configcenter.method.pojo.dto.CfgEventDto;
import com.transcend.plm.configcenter.method.pojo.qo.CfgEventQo;
import com.transcend.plm.configcenter.method.pojo.vo.CfgEventVo;
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
public interface CfgEventConverter {

    CfgEventConverter INSTANCE = Mappers.getMapper(CfgEventConverter.class);

    /**
     * dto2po
     * @param dto
     * @return
     */
    CfgEvent dto2po(CfgEventDto dto);

    /**
     * po2vo
     * @param po
     * @return
     */
    CfgEventVo po2vo(CfgEvent po);

    /**
     * qo2po
     * @param param
     * @return
     */
    CfgEvent qo2po(CfgEventQo param);

    /**
     * pos2vos
     * @param records
     * @return
     */
    List<CfgEventVo> pos2vos(List<CfgEvent> records);
}
