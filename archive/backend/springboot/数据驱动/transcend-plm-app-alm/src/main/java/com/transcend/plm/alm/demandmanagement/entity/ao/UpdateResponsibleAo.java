package com.transcend.plm.alm.demandmanagement.entity.ao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bin.yin
 * @description: RR需求更新责任人AO
 * @version:
 * @date 2024/06/24 17:14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateResponsibleAo {
    /**
     * RR需求bid
     */
    private String rrBid;
    /**
     * 更新bid
     */
    private String updateBid;
    /**
     * 责任人工号
     */
    private String responsiblePerson;
}
