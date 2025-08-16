package com.transcend.plm.datadriven.notify.mapstruct;

import com.transcend.plm.datadriven.notify.domain.NotifyTimeRule;
import com.transcend.plm.datadriven.notify.vo.NotifyTimeRuleVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface NotifyTimeRuleConverter {
    NotifyTimeRuleConverter INSTANCE = Mappers.getMapper(NotifyTimeRuleConverter.class);

    /**
     * po2Vo
     *
     * @param entity entity
     * @return {@link NotifyTimeRuleVo}
     */
    NotifyTimeRuleVo po2Vo(NotifyTimeRule entity);
}
