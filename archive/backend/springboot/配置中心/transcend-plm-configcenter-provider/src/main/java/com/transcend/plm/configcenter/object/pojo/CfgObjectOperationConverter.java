package com.transcend.plm.configcenter.object.pojo;

import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectOperationPo;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectOperationVo;
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
public interface CfgObjectOperationConverter {


    CfgObjectOperationConverter INSTANCE = Mappers.getMapper(CfgObjectOperationConverter.class);

    /**
     * po 转化为 vo
     * @param po
     * @return
     */
    CfgObjectOperationVo po2vo(CfgObjectOperationPo po);

    /**
     * pos 转化为 vos
     * @param po
     * @return
     */
    List<CfgObjectOperationVo> pos2vos(List<CfgObjectOperationPo> po);
    /**
     * vo 转化为 po
     * @param vo
     * @return
     */
    CfgObjectOperationPo vo2po(CfgObjectOperationVo vo);


}
