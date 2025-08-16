package com.transcend.plm.datadriven.apm.permission.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmSphere;
import com.transcend.plm.datadriven.apm.permission.repository.mapper.ApmSphereMapper;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmSphereService;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transsion.framework.common.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

/**
* @author peng.qin
* @description 针对表【apm_domain】的数据库操作Service实现
* @createDate 2023-09-20 16:15:29
*/
@Service
public class ApmSphereServiceImpl extends ServiceImpl<ApmSphereMapper, ApmSphere>
    implements ApmSphereService {

    @Override
    public ApmSphere getByBizBidAndType(String bizBid, String type) {
        if(StringUtil.isBlank(bizBid) || StringUtil.isBlank(type)){
            return null;
        }
        //根据bizBid和type进行数据库查询
        return  getOne(Wrappers.lambdaQuery(ApmSphere.class)
                .eq(ApmSphere::getBizBid, bizBid)
                .eq(ApmSphere::getType, type)
                .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
                .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
        );
    }

    @Override
    public Boolean deleteByBizBidAndType(String bizBid, String type) {
        Assert.hasText(bizBid,"业务bid为空");
        Assert.hasText(type,"业务类型为空");
        return remove(Wrappers.lambdaQuery(ApmSphere.class).eq(ApmSphere::getBizBid, bizBid)
                .eq(ApmSphere::getType, type));
    }

    @Override
    public ApmSphere getByBid(String bid) {
        Assert.hasText(bid,"sphereBid为空");
        return getOne(Wrappers.lambdaQuery(ApmSphere.class)
                .eq(ApmSphere::getBid, bid)
                .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
                .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
        );
    }

    @Override
    public List<ApmSphere> getByBizBids(String type, Set<String> bizBids) {
        Assert.notEmpty(bizBids,"bizBids");
        return list(Wrappers.lambdaQuery(ApmSphere.class)
                .in(ApmSphere::getBizBid, bizBids)
                .eq(StringUtils.isNotBlank(type), ApmSphere::getType, type)
                .eq(ApmSphere::getEnableFlag, CommonConst.ENABLE_FLAG_ENABLE)
                .eq(ApmSphere::getDeleteFlag, CommonConst.DELETE_FLAG_NOT_DELETED)
        );
    }

    @Override
    public List<String> querySpaceSphereBidBySphereBid(Set<String> sphereBids) {
        Assert.notEmpty(sphereBids,"sphereBids为空");
        return getBaseMapper().querySpaceSphereBidBySphereBid(sphereBids);
    }

    @Override
    public List<String> getSphereBidListByBid(String bid) {
        Assert.hasText(bid,"sphereBids为空");
        return getBaseMapper().getSphereBidListByBid(bid);
    }
}




