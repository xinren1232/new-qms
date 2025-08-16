package com.transcend.plm.datadriven.apm.space.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpace;
import com.transcend.plm.datadriven.apm.space.repository.mapper.ApmSpaceMapper;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service
public class ApmSpaceServiceImpl extends ServiceImpl<ApmSpaceMapper, ApmSpace>
    implements ApmSpaceService {

    @Resource
    ApmSpaceMapper apmSpaceMapper;

    @Override
    public List<String> getSpaceBids() {
        List<ApmSpace> apmSpaces = list(Wrappers.<ApmSpace>lambdaQuery().eq(ApmSpace::getDeleteFlag,false));
        return apmSpaces.stream().map(ApmSpace::getBid).distinct().collect(Collectors.toList());
    }

    @Override
    public boolean physicsRemoveByBid(String bid) {
        if (StringUtils.isBlank(bid)) {
            return false;
        }
        return apmSpaceMapper.physicsRemoveByBid(bid);
    }

    /**
     * updateSphereBid
     *
     * @param bid       bid
     * @param sphereBid sphereBid
     */
    @Override
    public Boolean updateSphereBid(String bid, String sphereBid) {
        return apmSpaceMapper.updateSphereBid(bid, sphereBid) > 0;
    }
}




