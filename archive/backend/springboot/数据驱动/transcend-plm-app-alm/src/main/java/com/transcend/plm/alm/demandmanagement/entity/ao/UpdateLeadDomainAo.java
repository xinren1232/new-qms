package com.transcend.plm.alm.demandmanagement.entity.ao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bin.yin
 * @description: RR需求更新主导领域AO
 * @version:
 * @date 2024/06/24 16:52
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLeadDomainAo {
    /**
     * RR需求bid
     */
    private String rrBid;
    /**
     * 更新bid
     */
    private String updateBid;
}
