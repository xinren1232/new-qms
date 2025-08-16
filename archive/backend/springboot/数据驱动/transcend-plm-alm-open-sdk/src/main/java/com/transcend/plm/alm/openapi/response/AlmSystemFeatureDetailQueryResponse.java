package com.transcend.plm.alm.openapi.response;

import com.transcend.plm.alm.openapi.Response;
import com.transcend.plm.alm.openapi.dto.AlmSystemFeatureDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 特性树详情查询响应类
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 17:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AlmSystemFeatureDetailQueryResponse extends Response {

    /**
     * 特性数据
     */
    private AlmSystemFeatureDTO data;
}
