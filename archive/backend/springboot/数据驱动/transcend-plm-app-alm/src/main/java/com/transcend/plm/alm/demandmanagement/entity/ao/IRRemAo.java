package com.transcend.plm.alm.demandmanagement.entity.ao;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/6/25
 */

@Builder(toBuilder = true)
@Data
public class IRRemAo {

    @NotBlank(message = "domainBid is required")
    private String domainBid;

    @NotBlank(message = "productBid is required")
    private String rrBid;

    @NotBlank(message = "irBid is required")
    private String irBid;
}
