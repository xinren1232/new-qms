package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.transcend.plm.datadriven.apm.flow.service.ISpecialFlowHandleService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transsion.framework.uac.service.IUacUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author unknown
 */
@Service
public class SpecialFlowHandleServiceImpl implements ISpecialFlowHandleService {

    @Resource
    private IUacUserService uacUserService;

    /**
     * 特殊角色解析
     *
     * @param spaceAppBid
     * @param instanceBid
     * @param mSpaceAppData
     * @return
     */
    @Override
    public boolean setSpecialApmRoleUsers(String spaceAppBid, String instanceBid, MSpaceAppData mSpaceAppData) {
        return false;
    }

}
