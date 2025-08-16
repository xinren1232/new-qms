package com.transcend.plm.configcenter.object.infrastructure.repository.mapper;

import com.transcend.plm.configcenter.api.model.object.dto.ObjectRelationAttrDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jinpeng.bai
 * @Entity com.transsion.rd.object.dto.ObjectRelationAttr
 */
@Mapper
public interface ObjectRelationModelAttrMapper {

    /**
     * 查询列表
     * @param objectRelationAttrDTO
     * @return
     */
    List<ObjectRelationAttrDTO> queryList(@Param("ao") ObjectRelationAttrDTO objectRelationAttrDTO);


    /**
     * 新增
     * @param objectRelationAttrDTO
     * @return
     */
    int saveBatch(@Param("batchList") List<ObjectRelationAttrDTO> objectRelationAttrDTO);



    /**
     * 删除关系
     * @return
     */
    int remove(@Param("bids") List<String> bids);
}




