package com.transcend.plm.datadriven.apm.log.strategy;

import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.datadriven.apm.log.model.dto.OperationLogCfgViewMetaDto;
import com.transcend.plm.datadriven.common.enums.ViewComponentEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Author Qiu Yuhao
 * @Date 2024/1/16 10:36
 * @Describe
 */
@Component(ViewComponentEnum.DATE_CONSTANT + AbstractValueChangeStrategy.STRATEGY_NAME_SUFFIX)
public class DateValueChangeStrategy extends AbstractValueChangeStrategy {

    DateTimeFormatter dateTimeFormatter;

    @PostConstruct
    public void init() {
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public String getChangeValue(String spaceAppBid, Object oldValue, Object newValue, OperationLogCfgViewMetaDto cfgViewMetaDto) {
        if (newValue == null) {
            newValue = "";
        }
        if (newValue instanceof Date) {
            LocalDateTime localDateTime = ((Date) newValue).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            // 将Date对象转换为指定格式的字符串
            newValue = dateTimeFormatter.format(localDateTime);
        }
        if (oldValue == null) {
            return super.getChangeValue(spaceAppBid, "", newValue, cfgViewMetaDto);
        }
        oldValue = dateTimeFormatter.format((LocalDateTime) oldValue);

        return super.getChangeValue(spaceAppBid, oldValue, newValue, cfgViewMetaDto);
    }
}
