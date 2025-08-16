package com.transcend.plm.datadriven.api.model.ao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/4/10
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileActionAo {

    /**
     * 文件实例Id
     */
    private String fileId;
    
    /**
     * 操作人工号
     */
    private String jobNumber;


    /**
     * 复制事件，request.请求，change.变化时，其他具体事件
     */
    private String copyEvent;
}
