package com.transcend.plm.alm.demandmanagement.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RelDemandBo {

    private String spaceBid;

    private String spaceAppBid;

    private String relDemandCode;

    private String relDemandBid;

}
