package com.transcend.plm.alm.openapi.response;

import com.transcend.plm.alm.openapi.Response;
import com.transcend.plm.alm.openapi.dto.AlmSystemFeatureDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 特性树查询响应
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/27 17:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AlmSystemFeatureTreeQueryResponse extends Response {

    /**
     * 特性树数据列表
     */
    private List<AlmSystemFeatureDTO> data;
}
