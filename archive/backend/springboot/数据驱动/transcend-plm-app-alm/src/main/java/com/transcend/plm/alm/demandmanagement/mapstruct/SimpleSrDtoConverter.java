package com.transcend.plm.alm.demandmanagement.mapstruct;

import com.transcend.plm.alm.model.dto.SimpleSrDTO;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 简单SR对象转换类
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/6/5 14:21
 */
@Mapper
public interface SimpleSrDtoConverter {
    SimpleSrDtoConverter INSTANCE = Mappers.getMapper(SimpleSrDtoConverter.class);


    /**
     * 简单SR对象转换
     *
     * @param object 对象
     * @return 简单SR对象
     */
    default SimpleSrDTO toDto(MObject object) {
        if (object == null) {
            return null;
        }
        TranscendObjectWrapper wrapper = new TranscendObjectWrapper(object);
        SimpleSrDTO dto = new SimpleSrDTO();
        dto.setBid(wrapper.getBid());
        dto.setCoding(wrapper.getCoding());
        dto.setName(wrapper.getName());
        return dto;
    }

    /**
     * 简单SR对象列表转换
     *
     * @param objects 对象集合
     * @return 简单SR对象集合
     */
    List<SimpleSrDTO> toDtoList(List<MObject> objects);

}
