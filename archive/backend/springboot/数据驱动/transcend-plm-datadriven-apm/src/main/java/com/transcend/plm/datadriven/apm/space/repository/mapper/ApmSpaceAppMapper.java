package com.transcend.plm.datadriven.apm.space.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.apm.space.pojo.dto.ApmSpaceAppCopyDto;
import com.transcend.plm.datadriven.apm.space.pojo.vo.ApmSpaceAppVo;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author unknown
 * @Entity generator.domain.ApmSpaceApp
 */
@Mapper
public interface ApmSpaceAppMapper extends BaseMapper<ApmSpaceApp> {

    /**
     * 方法描述
     *
     * @param spaceAppBids spaceAppBids
     * @return List<ApmSpaceAppVo>
     */
    List<ApmSpaceAppVo> listSpaceInfo(@Param("spaceAppBids") List<String> spaceAppBids);

    /**
     * 方法描述
     *
     * @param oldSpaceBid    oldSpaceBid
     * @param newSpaceBid    newSpaceBid
     * @param newSpaceAppBid newSpaceAppBid
     * @return Boolean
     */
    Boolean copyBySpaceBid(@Param("oldSpaceBid") String oldSpaceBid,
                           @Param("newSpaceBid") String newSpaceBid,
                           @Param("newSpaceAppBid") String newSpaceAppBid);

    /**
     * 方法描述
     *
     * @param apmSpaceAppCopyDtoList apmSpaceAppCopyDtoList
     * @return Boolean
     */
    Boolean batchCopyBySpaceBid(@Param("list") List<ApmSpaceAppCopyDto>
                                        apmSpaceAppCopyDtoList);

    /**
     * updateSort
     *
     * @param apmSpaceApps apmSpaceApps
     */
    void updateSort(@Param("list") List<ApmSpaceApp> apmSpaceApps);


}




