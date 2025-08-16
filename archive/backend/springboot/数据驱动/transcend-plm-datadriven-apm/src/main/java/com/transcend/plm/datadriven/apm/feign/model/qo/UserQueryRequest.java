package com.transcend.plm.datadriven.apm.feign.model.qo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author Qiu Yuhao
 * @Date 2023/11/15 9:55
 * @Describe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserQueryRequest {

    private String keyword;
}
