package com.transcend.plm.datadriven.config.eventbus.posthandler.strategy.handler;

import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import org.springframework.core.Ordered;

/**
 * @Describe 职责链模式抽象基类处理者
 * @Author yuhao.qiu
 * @Date 2024/4/30
 */
public abstract class AbstractHandler implements Ordered {

    /**
     * 责任链中的下一个对象
     */
    private AbstractHandler nextHandler;

    /**
     * 执行当前链的操作。
     *
     * @param postEventBusDto 后置事件总线数据传输对象
     */
    protected abstract void doCurrentAction(NotifyPostEventBusDto postEventBusDto);

    /**
     * Determines if the given object bid matches the current object bid.
     *
     * @param currentObjBid The current object bid to compare against.
     * @return true if the object bid matches the current object bid, false otherwise.
     */
    public abstract boolean isMatch(String currentObjBid);

    /**
     * 具体参数拦截逻辑,给子类去实现
     * @param postEventBusDto 后置参数
     */
    public void action(NotifyPostEventBusDto postEventBusDto) {
        //执行当前链
        doCurrentAction(postEventBusDto);
        //如果还有下个链，执行下个链
        if (getNextHandler() != null) {
            getNextHandler().action(postEventBusDto);
        }
    }

    /**
     * 获取下一个链对象
     * @return
     */
    public AbstractHandler getNextHandler() {
        return nextHandler;
    }

    /**
     * 插入责任链的下一个处理器
     * @param nextHandler 下个处理器
     */
    public void setNextHandler(AbstractHandler nextHandler){
        this.nextHandler = nextHandler;
    }

}
