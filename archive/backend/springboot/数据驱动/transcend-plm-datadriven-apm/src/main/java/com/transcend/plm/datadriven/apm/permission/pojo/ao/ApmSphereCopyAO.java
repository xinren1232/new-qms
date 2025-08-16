package com.transcend.plm.datadriven.apm.permission.pojo.ao;

import lombok.Data;

import java.util.Map;

/**
 * @author unknown
 */
@Data
public class ApmSphereCopyAO {
    private String pbid;
    Map<String,String> appBidMap;
}
