package com.transcend.plm.configcenter.draft.infrastructure.repository.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.draft.infrastructure.repository.po.CfgDraftPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-02-22 10:12
 **/
@Mapper
public interface CfgDraftMapper extends BaseMapper<CfgDraftPo> {


    CfgDraftPo getByCategoryAndBizCode(@Param("category") String category,
                                       @Param("bizCode") String bizCode);
}
