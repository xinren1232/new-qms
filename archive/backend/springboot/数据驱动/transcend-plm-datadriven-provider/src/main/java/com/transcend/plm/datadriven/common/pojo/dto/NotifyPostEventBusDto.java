package com.transcend.plm.datadriven.common.pojo.dto;

import com.transcend.plm.datadriven.api.model.MObject;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/4/9
 */
@Data
@Accessors(chain=true)
@AllArgsConstructor
@NoArgsConstructor
public class NotifyPostEventBusDto {

    private MObject object;

    private String jobNumber;

    /**
     * 可以根据方法名字再次拓展单个对象的不同策略
     */
    private String methodName;


}
