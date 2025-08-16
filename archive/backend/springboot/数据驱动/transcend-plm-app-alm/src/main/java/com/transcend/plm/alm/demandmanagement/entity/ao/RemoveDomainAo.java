package com.transcend.plm.alm.demandmanagement.entity.ao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author bin.yin
 * @description: RR需求移除领域AO
 * @version:
 * @date 2024/06/24 13:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemoveDomainAo {
    /**
     * RR需求bid
     */
    private String rrBid;
    /**
     * 需要移除的领域/应用/模块 bid列表
     */
    private List<String> bids;
}
