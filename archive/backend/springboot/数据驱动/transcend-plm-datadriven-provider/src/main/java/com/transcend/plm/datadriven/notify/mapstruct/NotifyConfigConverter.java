package com.transcend.plm.datadriven.notify.mapstruct;

import com.transcend.plm.datadriven.notify.domain.NotifyConfig;
import com.transcend.plm.datadriven.notify.dto.NotifyConfigDto;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface NotifyConfigConverter {
    NotifyConfigConverter INSTANCE = Mappers.getMapper(NotifyConfigConverter.class);

    /**
     * dto2Po
     *
     * @param notifyConfigDto notifyConfigDto
     * @return {@link NotifyConfig }
     */
    NotifyConfig dto2Po(NotifyConfigDto notifyConfigDto);

    /**
     * po2Vo
     *
     * @param notifyConfig notifyConfig
     * @return {@link NotifyConfigVo}
     */
    NotifyConfigVo po2Vo(NotifyConfig notifyConfig);
}
