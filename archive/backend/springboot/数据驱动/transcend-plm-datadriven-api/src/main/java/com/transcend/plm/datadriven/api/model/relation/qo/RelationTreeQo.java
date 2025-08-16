package com.transcend.plm.datadriven.api.model.relation.qo;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bin.yin
 * @date 2024/05/11 15:53
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RelationTreeQo {

    private String sourceBid;

    private String relationModelCode;

    private String targetModelCode;

    private ModelMixQo modelMixQo;
}
