package com.transcend.plm.datadriven.apm.integration.instance.impl;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.ObjectEnum;
import com.transcend.plm.datadriven.apm.integration.instance.IInstanceOperateService;
import com.transcend.plm.datadriven.apm.integration.instance.InstanceOperateStrategyFactory;
import com.transcend.plm.datadriven.apm.integration.message.InstanceOperateMessage;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description  实例操作策略服务
 * @createTime 2023-12-20 09:30:00
 */
@Service
@Slf4j
public class DefaultInstanceOperateService implements IInstanceOperateService, InitializingBean {
    @Resource
    private IBaseApmSpaceAppDataDrivenService baseApmSpaceAppDataDrivenService;
    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Override
    public boolean add(InstanceOperateMessage message) {
        MSpaceAppData mSpaceAppData = MSpaceAppData.buildFrom(message.getMObject(), message.getSpaceBid(), message.getSpaceAppBid());
        //处理foreignBid
        mSpaceAppData.setForeignBid(mSpaceAppData.getBid());
        mSpaceAppData.setBid(null);
        baseApmSpaceAppDataDrivenService.add(message.getSpaceAppBid(), mSpaceAppData);
        return true;
    }

    @Override
    public boolean update(InstanceOperateMessage message) {
        MSpaceAppData mSpaceAppData = MSpaceAppData.buildFrom(message.getMObject(), message.getSpaceBid(), message.getSpaceAppBid());
        //根据foreignBid查询出原始数据
        MObject mObject = objectModelDomainService.getOneByProperty(message.getBizType(), ObjectEnum.FOREIGN_BID.getCode(), mSpaceAppData.getBid());
        if(mObject == null){
            log.error("更新实例失败，原始数据不存在，foreignBid:{}", mSpaceAppData.getBid());
        }
        mSpaceAppData.setBid(mObject.getBid());
        mSpaceAppData.setForeignBid(mObject.getForeignBid());
        baseApmSpaceAppDataDrivenService.update(message.getSpaceAppBid(), mObject.getBid(), mSpaceAppData);
        return true;
    }

    @Override
    public boolean delete(InstanceOperateMessage message) {
        MObject mObject = objectModelDomainService.getOneByProperty(message.getBizType(), ObjectEnum.FOREIGN_BID.getCode(), message.getMObject().getBid());
        if(mObject == null){
            log.error("删除实例失败，原始数据不存在，foreignBid:{}", message.getMObject().getBid());
        }
        baseApmSpaceAppDataDrivenService.logicalDelete(message.getSpaceAppBid(), mObject.getBid());
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        InstanceOperateStrategyFactory.register("default", this);
    }
}
