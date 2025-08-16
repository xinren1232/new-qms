package com.transcend.plm.configcenter.object.pojo;

import com.transcend.plm.configcenter.object.domain.entity.CfgObject;
import com.transcend.plm.configcenter.object.infrastructure.po.CfgObjectPo;
import com.transcend.plm.configcenter.api.model.object.dto.ObjectAddParam;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
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
public interface CfgObjectConverter {


    CfgObjectConverter INSTANCE = Mappers.getMapper(CfgObjectConverter.class);

    /**
     * po 转化为 vo
     * @param po
     * @return
     */
    CfgObjectVo po2vo(CfgObjectPo po);

    /**
     * pos 转化为 vos
     * @param po
     * @return
     */
    List<CfgObjectVo> pos2vos(List<CfgObjectPo> po);
    /**
     * vo 转化为 po
     * @param vo
     * @return
     */
    CfgObjectPo vo2po(CfgObjectVo vo);


    CfgObject vo2entity(CfgObjectVo byBid);

    CfgObjectPo entity2po(CfgObject cfgObject);

    CfgObject addParem2entity(ObjectAddParam objectAddParam);

    CfgObject po2entity(CfgObjectPo byModelCode);
}
