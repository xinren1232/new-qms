package com.transcend.plm.datadriven.filemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.transcend.plm.datadriven.filemanager.pojo.ao.FileCopyExecutionAo;
import com.transcend.plm.datadriven.filemanager.pojo.po.FileCopyExecutionRecordPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author bin.yin
 * @Entity com.transcend.plm.datadriven.filemanager.domain.FileCopyExecutionRecordPo
 */
@Mapper
public interface FileCopyExecutionRecordMapper extends BaseMapper<FileCopyExecutionRecordPo> {

    /**
     * 查询分页
     *
     * @param ao ao
     * @return {@link List<FileCopyExecutionRecordPo>}
     */
    List<FileCopyExecutionRecordPo> queryPageByCondition(@Param("ao") FileCopyExecutionAo ao);
}




