package com.transcend.plm.datadriven.apm.strategy;

import cn.hutool.core.collection.CollUtil;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.Order;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.constants.CacheNameConstant;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @Author Qiu Yuhao
 * @Date 2024/3/12 15:00
 * @Describe 客制化应用字段自定义字段策略类 - 根据空间和应用来实现自定义字段的处理
 */
@Slf4j
@Component
public class HandleCustomFieldStrategy {

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private IAppDataService appDataService;

    private Map<String, BiConsumer<ApmSpaceApp, MSpaceAppData>> strategyMap;

    @Value("${custom.handle.field.productDemandCodeConfig:}")
    String productDemandCodeConfig;

    @PostConstruct
    public void init() {
        strategyMap = new HashMap<>(16);
        strategyMap.put(productDemandCodeConfig, this::handleProductDemandCode);
    }

    /**
     * 获取对应的策略方法
     *
     * @param app  应用信息
     * @param data 数据
     */
    public void execute(ApmSpaceApp app, MSpaceAppData data) {
        // 通过ModelCode获取对应的策略方法
        String strategyKey = app.getSpaceBid() + app.getBid();
        BiConsumer<ApmSpaceApp, MSpaceAppData> strategy = strategyMap.get(strategyKey);
        if (strategy == null) {
            return;
        }
        strategy.accept(app, data);
    }

    /**
     * 客制化空间 - 需求管理
     * 在需求池新增需求时，需求编号生成规则
     *
     * @param app
     * @param data
     */
    private void handleProductDemandCode(ApmSpaceApp app, MSpaceAppData data) {
        Long increment = redisTemplate.opsForValue().increment(CacheNameConstant.PRODUCT_DEMAND_CODE);
        if (increment == null) {
            RedissonClient redisson = Redisson.create();
            RLock lock = redisson.getLock("rLock");
            try {
                // 尝试获取分布式锁
                boolean flag = lock.tryLock(10, 10, TimeUnit.SECONDS);
                if (flag) {
                    // 再次判断共享资源是否已经被加载
                    increment = redisTemplate.opsForValue().increment(CacheNameConstant.PRODUCT_DEMAND_CODE);
                    if (increment == null) {
                        // 加载最新资源
                        List<MObject> list = queryLatestData(app);
                        if (CollUtil.isNotEmpty(list)) {
                            MObject mObject = list.get(0);
                            String code = String.valueOf(mObject.get("productDemandCode"));
                            String[] codeParts = code.split("-");
                            increment = Long.valueOf(codeParts[1]) + 1;
                            // 将资源重构到缓存中
                            try {
                                redisTemplate.opsForValue()
                                        .set(CacheNameConstant.PRODUCT_DEMAND_CODE, String.valueOf(increment));
                            } catch (Exception e) {
                                log.error("重构缓存失败", e);
                            }
                        } else {
                            throw new PlmBizException("系统异常，无法自动生成产品需求单号");
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupted();
                throw new PlmBizException("系统异常，无法自动生成产品需求单号");
            } finally {
                // 释放分布式锁
                lock.unlock();
            }
        }
        String productDemandCode = "PR-" + increment;
        data.put("productDemandCode", productDemandCode);
    }

    private List<MObject> queryLatestData(ApmSpaceApp app) {
        // 构造查询条件
        QueryCondition condition = new QueryCondition();
        ArrayList<Order> orderList = Lists.newArrayList();
        Order order = new Order();
        order.setProperty("createTime");
        order.setDesc(true);
        orderList.add(order);
        condition.setOrders(orderList);
        ArrayList<QueryWrapper> queryList = Lists.newArrayList();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.isNotNull("productDemandCode");
        queryList.add(queryWrapper);
        condition.setQueries(queryList);
        return appDataService.list(app.getSpaceBid(), app.getBid(), condition);
    }
}
