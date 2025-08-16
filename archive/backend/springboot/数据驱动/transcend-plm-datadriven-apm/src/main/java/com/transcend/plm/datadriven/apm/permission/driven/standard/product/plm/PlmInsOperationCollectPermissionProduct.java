package com.transcend.plm.datadriven.apm.permission.driven.standard.product.plm;

import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectOperationResult;
import com.transcend.plm.datadriven.apm.permission.driven.standard.product.IStandardPermissionProduct;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 * Plm实例操作权限收集
 */
public class PlmInsOperationCollectPermissionProduct implements IStandardPermissionProduct<CollectOperationParam, Map<String, List<CollectOperationResult>>> {

    /**
     * @param param
     * @return
     */
    @Override
    public Map<String, List<CollectOperationResult>> execute(CollectOperationParam param) {
        return null;
    }
}
