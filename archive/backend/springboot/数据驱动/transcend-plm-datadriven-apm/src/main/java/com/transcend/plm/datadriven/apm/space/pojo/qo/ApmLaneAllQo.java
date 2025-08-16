package com.transcend.plm.datadriven.apm.space.pojo.qo;

import com.transcend.plm.datadriven.api.model.qo.ModelMixQo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/2/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApmLaneAllQo {

    private String spaceAppTabBid;

    private ModelMixQo mixQo;
}
