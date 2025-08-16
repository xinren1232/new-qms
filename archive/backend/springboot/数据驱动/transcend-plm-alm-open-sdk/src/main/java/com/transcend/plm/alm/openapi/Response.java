package com.transcend.plm.alm.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应类
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version v1.0
 * @date 2023/10/27
 */
@NoArgsConstructor
@Data
public class Response implements Serializable {

    /**
     * 响应编码
     */
    @JsonProperty("code")
    private String code;
    /**
     * 响应信息
     */
    @JsonProperty("message")
    private String message;
    /**
     * 是否成功
     */
    @JsonProperty("success")
    private Boolean success;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
}
