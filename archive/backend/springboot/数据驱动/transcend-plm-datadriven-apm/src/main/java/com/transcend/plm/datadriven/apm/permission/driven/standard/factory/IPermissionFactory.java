package com.transcend.plm.datadriven.apm.permission.driven.standard.factory;

import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CheckOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectAddParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.CollectOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.dto.ListViewOperationParam;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectAddResult;
import com.transcend.plm.datadriven.apm.permission.driven.standard.pojo.vo.CollectOperationResult;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public interface IPermissionFactory {
    /**
     * collectAdd
     *
     * @param param param
     * @return {@link List<CollectAddResult>}
     */
    List<CollectAddResult> collectAdd(CollectAddParam param);

    /**
     * collectView
     *
     * @param param param
     * @return {@link String}
     */
    String collectView(ListViewOperationParam param);

    /**
     * collectOperation
     *
     * @param param param
     * @return {@link Map<String, List<CollectOperationResult>>}
     */
    Map<String, List<CollectOperationResult>> collectOperation(CollectOperationParam param);

    /**
     * checkOperation
     *
     * @param param param
     * @return {@link Boolean}
     */
    Boolean checkOperation(CheckOperationParam param);

}
