package com.transcend.plm.alm.pi.dto;

import lombok.Data;

@Data
public class SyncDto {
    //数据类型（domain-产品领域；product-产品）
    private String type;
    //签名方法，目前采用md5
    private String signMethod;

    private String sign;

    private String body;
}
