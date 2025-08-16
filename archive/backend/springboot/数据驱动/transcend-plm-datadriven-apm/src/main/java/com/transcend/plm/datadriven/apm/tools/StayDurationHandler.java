package com.transcend.plm.datadriven.apm.tools;

import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.wapper.TranscendObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 停留时间处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/3/17 22:51
 */
@Slf4j
public class StayDurationHandler {

    /**
     * 批量处理停留时间
     *
     * @param list 需要处理的数据集合
     * @param <T>  支持的数据类型
     */
    public static <T extends Map<String, Object>> void handle(List<T> list) {
        if (list == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        list.forEach(data -> setStayDuration(data, now));
    }

    /**
     * 处理停留时间
     *
     * @param data 需要处理的数据
     * @param <T>  支持的数据类型
     */
    public static <T extends Map<String, Object>> void handle(T data) {
        setStayDuration(data, LocalDateTime.now());
    }

    /**
     * 设置停留时间
     *
     * @param data 需要处理的数据
     * @param now  当前时间
     * @param <T>  支持的数据类型
     */
    private static <T extends Map<String, Object>> void setStayDuration(T data, LocalDateTime now) {
        if (data == null) {
            return;
        }
        TranscendObjectWrapper wrapper = new TranscendObjectWrapper(data);
        LocalDateTime reachTime = wrapper.getLocalDateTime(ObjectEnum.REACH_TIME.getCode());
        if (reachTime == null) {
            return;
        }
        wrapper.put(CommonConst.STAY_DURATION, getTimeDiffStr(reachTime, now));
    }


    /**
     * 获取停留时间字符串
     *
     * @param reachTime 到达时间
     * @param now       当前时间
     * @return 停留时间字符串
     */
    private static String getTimeDiffStr(LocalDateTime reachTime, LocalDateTime now) {
        Duration duration = Duration.between(reachTime, now);
        long hoursDifference = duration.toHours();

        //一小时内
        if (hoursDifference <= 0) {
            return "刚刚";
        }

        //一天内
        int dayHours = 24;
        if (hoursDifference < dayHours) {
            return hoursDifference + "小时";
        }

        //多于一天
        long daysDifference = hoursDifference / dayHours;
        hoursDifference = hoursDifference % dayHours;
        return String.format("%d天%d小时", daysDifference, hoursDifference);
    }

}
