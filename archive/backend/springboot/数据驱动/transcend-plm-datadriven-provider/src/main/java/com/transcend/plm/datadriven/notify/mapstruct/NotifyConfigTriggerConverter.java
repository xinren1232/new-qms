package com.transcend.plm.datadriven.notify.mapstruct;

import com.transcend.plm.datadriven.notify.domain.NotifyConfigTrigger;
import com.transcend.plm.datadriven.notify.dto.NotifyConfigTriggerDto;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigTriggerVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface NotifyConfigTriggerConverter {

    NotifyConfigTriggerConverter INSTANCE = Mappers.getMapper(NotifyConfigTriggerConverter.class);

    /**
     * po -> vo
     *
     * @param notifyConfigTrigger notifyConfigTrigger
     * @return {@link NotifyConfigTriggerDto }
     */
    NotifyConfigTriggerDto po2Dto(NotifyConfigTrigger notifyConfigTrigger);

    /**
     * dto -> po
     *
     * @param NotifyConfigTriggerDto
     * @return {@link NotifyConfigTrigger }
     */
    NotifyConfigTrigger dto2Po(NotifyConfigTriggerDto NotifyConfigTriggerDto);

    /**
     * pos2Dtos
     *
     * @param notifyConfigTriggers notifyConfigTriggers
     * @return {@link List<NotifyConfigTriggerDto>}
     */
    List<NotifyConfigTriggerDto> pos2Dtos(List<NotifyConfigTrigger> notifyConfigTriggers);

    /**
     * dtos2Pos
     *
     * @param NotifyConfigTriggerDtos NotifyConfigTriggerDtos
     * @return {@link List<NotifyConfigTrigger>}
     */
    List<NotifyConfigTrigger> dtos2Pos(List<NotifyConfigTriggerDto> NotifyConfigTriggerDtos);

    /**
     * po2Vo
     *
     * @param notifyConfigTrigger notifyConfigTrigger
     * @return {@link NotifyConfigTriggerVo}
     */
    NotifyConfigTriggerVo po2Vo(NotifyConfigTrigger notifyConfigTrigger);
}
