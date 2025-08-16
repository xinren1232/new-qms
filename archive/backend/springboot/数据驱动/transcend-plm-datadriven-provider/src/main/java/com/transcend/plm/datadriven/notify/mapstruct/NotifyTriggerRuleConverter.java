package com.transcend.plm.datadriven.notify.mapstruct;

import com.transcend.plm.datadriven.notify.domain.NotifyTriggerRule;
import com.transcend.plm.datadriven.notify.vo.NotifyTriggerRuleVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Mapper
public interface NotifyTriggerRuleConverter {
    NotifyTriggerRuleConverter INSTANCE = Mappers.getMapper(NotifyTriggerRuleConverter.class);

    /**
     * po2Vo
     *
     * @param notifyTriggerRule notifyTriggerRule
     * @return {@link NotifyTriggerRuleVo}
     */
    NotifyTriggerRuleVo po2Vo(NotifyTriggerRule notifyTriggerRule);

    /**
     * pos2Vos
     *
     * @param notifyTriggerRules notifyTriggerRules
     * @return {@link List<NotifyTriggerRuleVo>}
     */
    List<NotifyTriggerRuleVo> pos2Vos(List<NotifyTriggerRule> notifyTriggerRules);
}
