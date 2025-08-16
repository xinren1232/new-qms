package com.transcend.plm.alm.pi.dto;

import lombok.Data;

@Data
public class DomainDto {

    /**产品域名*/
    private String domainName;
    /**域名所在地Id*/
    private String locationId;
    /**域名环境：1=生产 2=UAT 3=测试 4=开发*/
    private String domainType;
    /**产品关联Id*/
    private String productId;
}
