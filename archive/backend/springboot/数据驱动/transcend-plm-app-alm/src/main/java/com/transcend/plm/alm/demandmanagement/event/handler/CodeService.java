package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.date.DateUtil;
import com.transcend.plm.datadriven.common.constant.NumberConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author: unknown
 */
@Service
public class CodeService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateCode(String prefix,String dash) {
        StringBuilder resultCode = new StringBuilder(prefix);
        resultCode.append(dash);
        String currentDate = DateUtil.format(new Date(), "yyMMdd");
        resultCode.append(currentDate);
        resultCode.append(dash);
        String redisKey = prefix + ":" + currentDate;
        // 通过Redis自增获取编码编号 如果Redis中没有这个Key则会生成一个新的Key 并且初始值为0
        Long currentSerialNumber = redisTemplate.opsForValue().increment(redisKey, 1);
        // 给个过期时间 这个Key的过期时间为一天 绝对不会在第三天还存在
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        resultCode.append(handleSerialNumber(currentSerialNumber));
        return resultCode.toString();
    }

    /**
     * 生成编码，如果middleCode为空则使用当前日期作为中间编码，否则使用用户传入的中间编码
     * @param prefix
     * @param middleCode
     * @param dash
     * @return
     */
    public String generateCode(String prefix,String middleCode, String dash) {
        StringBuilder resultCode = new StringBuilder(prefix);
        resultCode.append(dash);
        String currentDate = DateUtil.format(new Date(), "yyMMdd");
        if (StringUtils.isBlank(middleCode)) {
            middleCode = currentDate;
        }
        resultCode.append(middleCode);
        resultCode.append(dash);
        String redisKey = prefix + ":" + currentDate;
        // 通过Redis自增获取编码编号 如果Redis中没有这个Key则会生成一个新的Key 并且初始值为0
        Long currentSerialNumber = redisTemplate.opsForValue().increment(redisKey, 1);
        // 给个过期时间 这个Key的过期时间为一天 绝对不会在第三天还存在
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);
        resultCode.append(handleSerialNumber(currentSerialNumber));
        return resultCode.toString();
    }

    public String handleSerialNumber(Long currentSerialNumber) {
        if (currentSerialNumber < NumberConstant.TEN) {
            return "00" + currentSerialNumber;
        } else if (currentSerialNumber < NumberConstant.HUNDRED) {
            return "0" + currentSerialNumber;
        } else {
            return String.valueOf(currentSerialNumber);
        }
    }
}
