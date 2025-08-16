package com.transcend.plm.datadriven.filemanager.mapstruct;

import com.transcend.plm.datadriven.filemanager.pojo.bo.FileCopyExecutionBo;
import com.transcend.plm.datadriven.filemanager.pojo.po.FileCopyExecutionRecordPo;
import com.transcend.plm.datadriven.filemanager.pojo.vo.FileCopyExecutionVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author bin.yin
 * @date 2024/07/24
 */
@Mapper
public interface FileCopyExecutionRecordConverter {
    FileCopyExecutionRecordConverter INSTANCE = Mappers.getMapper(FileCopyExecutionRecordConverter.class);

    /**
     * pos2Vos
     *
     * @param fileCopyExecutionRecordPos fileCopyExecutionRecordPos
     * @return {@link List }<{@link FileCopyExecutionVo }>
     */
    List<FileCopyExecutionVo> pos2Vos(List<FileCopyExecutionRecordPo> fileCopyExecutionRecordPos);

    /**
     * bo2Po
     *
     * @param bo bo
     * @return {@link FileCopyExecutionRecordPo }
     */
    FileCopyExecutionRecordPo bo2Po(FileCopyExecutionBo bo);
}
