package com.transcend.plm.configcenter.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileViewer;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity generator.domain.CfgFileViewer
 */
public interface CfgFileViewerMapper extends BaseMapper<CfgFileViewer> {

    int deleteByBid(@Param("bid") String bid);
}




