package com.transcend.plm.pi.demandmanagement.event.handler;

import cn.hutool.core.date.DateUtil;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.constant.NumberConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.transcend.plm.pi.demandmanagement.constants.DemandManagementConstant.*;


/**
 * @Describe PI流程编码生成
 * @Author yuanhu.huang
 * @Date 2024/7/9
 */
@Slf4j
@Component
public class PIGenerateCodeAddEventHandler extends AbstractAddEventHandler {

    private static final String RR_OBJ_BID = "1253640663282315264";

    private static final String IR_OBJ_BID = "1253649025684303872";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${transcend.plm.apm.special.piSpecialSpaceAppBid:1258356418957418496}")
    private String piSpecialFlowSpaceAppBid;

    @Override
    public AddEventHandlerParam preHandle(AddEventHandlerParam param) {
        String resultCode = generateCode(IM02_PREFIX, param);
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        mSpaceAppData.put(PROCESS_CODING, resultCode);
        return super.preHandle(param);
    }

    /**
     * 生成编码
     * @param prefix 编码前缀
     * @param param 请求参数
     * @return 编码
     */
    private String generateCode(String prefix, AddEventHandlerParam param) {
        StringBuilder resultCode = new StringBuilder(prefix);
        resultCode.append(DASH);
        String currentDate = DateUtil.format(new Date(), "yyyyMM");
        resultCode.append(currentDate);
        // 通过Redis自增获取编码编号 如果Redis中没有这个Key则会生成一个新的Key 并且初始值为0
        Long currentSerialNumber = redisTemplate.opsForValue().increment(IM02_REDIS_KEY + REDIS_DELIMITER + currentDate, 1);
        //计算月底最后一天到当前时间剩余分钟数
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 获取本月最后一天的日期
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        // 获取当天的最后一刻
        LocalDateTime endOfDay = LocalDateTime.of(lastDayOfMonth, LocalTime.MAX);
        // 计算当前时间到月底剩余的分钟数
        long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), endOfDay);
        // 给个过期时间 这个Key的过期时间为一天 绝对不会在第三天还存在
        redisTemplate.expire(IM02_REDIS_KEY + REDIS_DELIMITER + currentDate, minutesLeft, TimeUnit.MINUTES);
        resultCode.append(handleSerialNumber(currentSerialNumber));
        return resultCode.toString();
    }

    public String handleSerialNumber(Long currentSerialNumber) {
        if (currentSerialNumber < NumberConstant.TEN) {
            return "000" + currentSerialNumber;
        } else if (currentSerialNumber < NumberConstant.HUNDRED) {
            return "00" + currentSerialNumber;
        } else if (currentSerialNumber < NumberConstant.THOUSAND) {
            return "0" + currentSerialNumber;
        }else {
            return String.valueOf(currentSerialNumber);
        }
    }

    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        String spaceAppBid = param.getApmSpaceApp().getBid();
        if(StringUtils.isEmpty(spaceAppBid) || StringUtils.isEmpty(piSpecialFlowSpaceAppBid)){
            return false;
        }
        return spaceAppBid.equals(piSpecialFlowSpaceAppBid);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
