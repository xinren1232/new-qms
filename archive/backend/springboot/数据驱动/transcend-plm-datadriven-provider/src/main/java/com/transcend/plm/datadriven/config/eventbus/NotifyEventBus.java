package com.transcend.plm.datadriven.config.eventbus;

import com.transcend.plm.datadriven.common.pojo.dto.NotifyCrossRelationEventBusDto;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyEventBusDto;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyReversionRelTaskBusDto;
import com.transsion.framework.tool.eventbus.EventBusWapper;
import com.transsion.framework.tool.eventbus.EventIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://zhuanlan.zhihu.com/p/336469154
 * https://www.cnblogs.com/xd502djj/p/14436582.html
 * 事件总线
 * 1、NotifyEventBusService  注册事件订阅方法，如deliveryIMEI
 * 2、NotifyEventBus         发送事件，  如postDeliveryIMEI
 * 3、注册事件订阅方法和发送事件是通过事件类型处理粘性事件，所以通知和接受事件类型必须一致，一个事件只能有一个同类型参数
 *
 * @author zhihui.yu
 */
public class NotifyEventBus {
    static Logger log = LoggerFactory.getLogger(NotifyEventBus.class);
    public static final EventBusWapper EVENT_BUS;

    static {
        EVENT_BUS = EventBusWapper.getInstance(EventIdentifier.builder()
                .name("NOTIFY")
                .poolSize(5)
                .build());
    }

    /**
     * @param object
     */
    public static void register(Object object) {
        EVENT_BUS.register(object);
    }

    /**
     * 发起事件
     *
     * @param notifyEventBusDto 入参
     */
    public static void publishEvent(NotifyEventBusDto notifyEventBusDto) {
        EVENT_BUS.post(notifyEventBusDto);

    }

    /**
     * 通知事件 - 目前方法只针对迭代-需求，版本-需求关系
     * <p>
     * <p>
     * 处理的关系为 迭代-需求时， 1.将新增的数据 同步到 版本-需求 和  项目-需求
     * <p>
     * <p>
     * 当处理关系为 版本-需求时，将新增的数据 同步到 项目-需求
     *
     * @param notifyCrossRelationEventBusDto 入参
     */
    public static void publishCrossEvent(NotifyCrossRelationEventBusDto notifyCrossRelationEventBusDto) {
        EVENT_BUS.post(notifyCrossRelationEventBusDto);
    }

    /**
     * @param postEventBusDto
     */
    public static void publishPostEvent(NotifyPostEventBusDto postEventBusDto) {
        EVENT_BUS.post(postEventBusDto);
    }

    /**
     * 通知事件 - 目前方法只针对迭代-需求,需求关联的任务 需要关联到迭代上
     *
     * @param dto
     */
    public static void publishReversionRelTask(NotifyReversionRelTaskBusDto dto) {
        EVENT_BUS.post(dto);
    }
}
