package com.transcend.plm.datadriven.apm.permission.driven.standard.factory.category;

import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CheckOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectAddParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.ListViewOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectAddResult;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectOperationResult;
import com.transcend.plm.datadriven.apm.permission.driven.standard.product.plm.PlmAddTypeCollectPermissionProduct;
import com.transcend.plm.datadriven.apm.permission.driven.standard.factory.IPermissionFactory;
import com.transcend.plm.datadriven.apm.permission.driven.standard.product.plm.PlmInsOperationCheckPermissionProduct;
import com.transcend.plm.datadriven.apm.permission.driven.standard.product.plm.PlmInsOperationCollectPermissionProduct;
import com.transcend.plm.datadriven.apm.permission.driven.standard.product.plm.PlmViewConditionCollectPermissionProduct;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public class PlmPermissionFactory implements IPermissionFactory {

    /**
     * @param param
     * @return
     */
    @Override
    public List<CollectAddResult> collectAdd(CollectAddParam param) {
        return new PlmAddTypeCollectPermissionProduct().execute(param);
    }

    /**
     * @param param
     * @return
     */
    @Override
    public String collectView(ListViewOperationParam param) {
        return new PlmViewConditionCollectPermissionProduct().execute(param);
    }

    /**
     * @param param
     * @return
     */
    @Override
    public Map<String, List<CollectOperationResult>> collectOperation(CollectOperationParam param) {
        return new PlmInsOperationCollectPermissionProduct().execute(param);
    }

    /**
     * @param param
     * @return
     */
    @Override
    public Boolean checkOperation(CheckOperationParam param) {
        return new PlmInsOperationCheckPermissionProduct().execute(param);
    }
}
