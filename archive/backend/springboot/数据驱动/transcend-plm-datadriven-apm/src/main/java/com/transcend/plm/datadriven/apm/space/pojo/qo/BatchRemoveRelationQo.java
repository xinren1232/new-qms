package com.transcend.plm.datadriven.apm.space.pojo.qo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author haidong.liu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BatchRemoveRelationQo {

    private String relationModelCode;
    private List<String> sourceBids;
    private List<String> targetBids;

}
