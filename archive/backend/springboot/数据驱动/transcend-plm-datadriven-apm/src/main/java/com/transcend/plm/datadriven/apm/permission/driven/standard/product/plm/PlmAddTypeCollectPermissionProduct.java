package com.transcend.plm.datadriven.apm.permission.driven.standard.product.plm;

import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectAddParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectAddResult;
import com.transcend.plm.datadriven.apm.permission.driven.standard.product.IStandardPermissionProduct;

import java.util.List;

/**
 * @author unknown
 * Plm新增类型收集
 */
public class PlmAddTypeCollectPermissionProduct implements IStandardPermissionProduct<CollectAddParam, List<CollectAddResult>> {

    /**
     * @param param
     * @return
     */
    @Override
    public List<CollectAddResult> execute(CollectAddParam param) {
        return null;
    }
}
