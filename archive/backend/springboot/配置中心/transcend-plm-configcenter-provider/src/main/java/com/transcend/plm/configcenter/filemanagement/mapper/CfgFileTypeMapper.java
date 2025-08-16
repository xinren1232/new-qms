package com.transcend.plm.configcenter.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.configcenter.filemanagement.domain.CfgFileType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.CfgFileType
 */
public interface CfgFileTypeMapper extends BaseMapper<CfgFileType> {
    int deleteByBid(@Param("fileTypeBid") String fileTypeBid);

}




