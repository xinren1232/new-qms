package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppViewModelCopyDto;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppViewModelPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.qin
 * @description 【ApmSpaceAppViewModel】的数据库操作Mapper
 * @createDate 2023-09-20 16:15:29
 * @Entity com.transcend.plm.datadriven.apm.repository.entity.ApmDomain
 */
public interface ApmSpaceAppViewModelMapper extends BaseMapper<ApmSpaceAppViewModelPo> {
    /**
     * getByCodeAndSpaceAppBid
     *
     * @param spaceAppBid spaceAppBid
     * @param code        code
     * @return ApmSpaceAppViewModelPo
     */
    ApmSpaceAppViewModelPo getByCodeAndSpaceAppBid(@Param("spaceAppBid") String spaceAppBid, @Param("code") String code);

    /**
     * copyBySpaceAppBid
     *
     * @param oldSpaceAppBid oldSpaceAppBid
     * @param newSpaceAppBid newSpaceAppBid
     * @return Boolean
     */
    Boolean copyBySpaceAppBid(@Param("oldSpaceAppBid") String oldSpaceAppBid, @Param("newSpaceAppBid") String newSpaceAppBid);

    /**
     * batchCopyBySpaceAppBid
     *
     * @param list list
     * @return Boolean
     */
    Boolean batchCopyBySpaceAppBid(@Param("list") List<ApmSpaceAppViewModelCopyDto> list);
}




