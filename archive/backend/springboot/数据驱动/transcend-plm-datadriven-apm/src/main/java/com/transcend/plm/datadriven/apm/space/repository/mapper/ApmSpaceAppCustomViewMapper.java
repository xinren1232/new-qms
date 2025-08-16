package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppViewModelCopyDto;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceAppCustomViewPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.qin
 * @description 【ApmSpaceAppViewModel】的数据库操作Mapper
 * @createDate 2023-09-20 16:15:29
 * @Entity com.transcend.plm.datadriven.apm.repository.entity.ApmDomain
 */
public interface ApmSpaceAppCustomViewMapper extends BaseMapper<ApmSpaceAppCustomViewPo> {

    /**
     * 方法描述
     *
     * @param spaceAppBid spaceAppBid
     * @param code        code
     * @return 返回值
     */
    ApmSpaceAppCustomViewPo getByCodeAndSpaceAppBid(@Param("spaceAppBid") String spaceAppBid, @Param("code") String code);

    /**
     * 方法描述
     *
     * @param oldSpaceAppBid oldSpaceAppBid
     * @param newSpaceAppBid newSpaceAppBid
     * @return 返回值
     */
    Boolean copyBySpaceAppBid(@Param("oldSpaceAppBid") String oldSpaceAppBid, @Param("newSpaceAppBid") String newSpaceAppBid);

    /**
     * 方法描述
     *
     * @param list list
     * @return 返回值
     */
    Boolean batchCopyBySpaceAppBid(@Param("list") List<ApmSpaceAppViewModelCopyDto> list);
}




