package com.transcend.plm.datadriven.apm.permission.driven.standard.factory.category;

import com.transcend.plm.datadriven.apm.permission.driven.standard.factory.IPermissionFactory;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CheckOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectAddParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.ListViewOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectAddResult;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectOperationResult;
import com.transcend.plm.datadriven.common.exception.PlmBizException;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public class AlmPermissionFactory implements IPermissionFactory {
    /**
     * @param param
     * @return
     */
    @Override
    public List<CollectAddResult> collectAdd(CollectAddParam param) {
        throw new PlmBizException("没有实现ALM权限！");
    }

    /**
     * @param param
     * @return
     */
    @Override
    public String collectView(ListViewOperationParam param) {
        throw new PlmBizException("没有实现ALM权限！");
    }

    /**
     * @param param
     * @return
     */
    @Override
    public Map<String, List<CollectOperationResult>> collectOperation(CollectOperationParam param) {
        throw new PlmBizException("没有实现ALM权限！");
    }

    /**
     * @param param
     * @return
     */
    @Override
    public Boolean checkOperation(CheckOperationParam param) {
        throw new PlmBizException("没有实现ALM权限！");
    }



}
