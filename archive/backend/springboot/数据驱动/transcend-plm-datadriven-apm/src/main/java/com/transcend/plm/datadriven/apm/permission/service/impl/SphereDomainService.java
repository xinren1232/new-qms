package com.transcend.plm.datadriven.apm.permission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmSphereAO;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.ApmSphereCopyAO;
import com.transcend.plm.datadriven.apm.enums.TypeEnum;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.apm.permission.service.ISphereDomainService;
import com.transcend.plm.datadriven.apm.permission.pojo.vo.ApmSphereVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 空间领域服务实现类
 * @createTime 2023-09-25 16:25:00
 */
@Service
public class SphereDomainService implements ISphereDomainService {
    @Resource
    private ApmSphereService apmSphereService;
    @Override
    public ApmSphere add(ApmSphereAO apmSphereAO) {
        ApmSphere apmSphere = new ApmSphere();
        ApmSphere parentSphere = apmSphereService.getByBizBidAndType(apmSphereAO.getPBizBid(), apmSphereAO.getPType());
        if (Objects.isNull(parentSphere)) {
            apmSphere.setPbid("0");
        } else {
            apmSphere.setPbid(parentSphere.getBid());
        }
        apmSphere.setBid(SnowflakeIdWorker.nextIdStr());
        apmSphere.setBizBid(apmSphereAO.getBizBid());
        apmSphere.setType(apmSphereAO.getType());
        apmSphere.setName(apmSphereAO.getName());
        apmSphere.setCreatedBy(SsoHelper.getJobNumber());
        apmSphere.setEnableFlag(1);
        apmSphereService.save(apmSphere);
        return apmSphere;
    }
    @Override
    public List<Map<String,String>> copySpaceAppSphere(String spaceAppBid,String nowSpaceBid,String nowAppBid){
        List<Map<String,String>> resList = new ArrayList<>();
        ApmSphere spaceApmSphere = apmSphereService.getByBizBidAndType(nowSpaceBid, TypeEnum.SPACE.getCode());
        ApmSphere apmSphere = apmSphereService.getByBizBidAndType(spaceAppBid, TypeEnum.OBJECT.getCode());
        //biz_bid和apmSphereBid关系
        Map<String,String> sphereBidMap = new HashMap<>(16);
        //旧biz_bid和新biz_bid关系
        Map<String,String> sphereBidOldAndNewMap = new HashMap<>(16);
        apmSphere.setId(null);
        apmSphere.setPbid(spaceApmSphere.getBid());
        String newBid = SnowflakeIdWorker.nextIdStr();
        sphereBidOldAndNewMap.put(apmSphere.getBid(),newBid);
        apmSphere.setBid(newBid);
        apmSphere.setBizBid(nowAppBid);
        sphereBidMap.put(apmSphere.getBizBid(),apmSphere.getBid());
        apmSphereService.save(apmSphere);
        resList.add(sphereBidMap);
        resList.add(sphereBidOldAndNewMap);
        return resList;
    }

    @Override
    public List<Map<String,String>> copySphere(ApmSphereCopyAO apmSphereCopyAO){
        List<Map<String,String>> resList = new ArrayList<>();
        List<ApmSphere> apmSpheres = apmSphereService.list(Wrappers.<ApmSphere>lambdaQuery().eq(ApmSphere::getType, TypeEnum.OBJECT.getCode()).in(ApmSphere::getBizBid, apmSphereCopyAO.getAppBidMap().keySet()));
        //biz_bid和apmSphereBid关系
        Map<String,String> sphereBidMap = new HashMap<>(16);
        //旧biz_bid和新biz_bid关系
        Map<String,String> sphereBidOldAndNewMap = new HashMap<>(16);
        for(ApmSphere apmSphere:apmSpheres){
            apmSphere.setId(null);
            apmSphere.setPbid(apmSphereCopyAO.getPbid());
            String newBid = SnowflakeIdWorker.nextIdStr();
            sphereBidOldAndNewMap.put(apmSphere.getBid(),newBid);
            apmSphere.setBid(newBid);
            apmSphere.setBizBid(apmSphereCopyAO.getAppBidMap().get(apmSphere.getBizBid()));
            sphereBidMap.put(apmSphere.getBizBid(),apmSphere.getBid());
        }
        apmSphereService.saveBatch(apmSpheres);
        resList.add(sphereBidMap);
        resList.add(sphereBidOldAndNewMap);
        return resList;
    }

    @Override
    public Boolean delete(ApmSphereAO apmSphereAO) {
        return apmSphereService.deleteByBizBidAndType(apmSphereAO.getBizBid(), apmSphereAO.getType());
    }

    @Override
    public ApmSphereVO update(ApmSphereAO apmSphereAO) {
        return null;
    }

    @Override
    public ApmSphereVO query(ApmSphereAO apmSphereAO) {
        return null;
    }
}
