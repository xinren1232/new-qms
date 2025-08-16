package com.transcend.plm.datadriven.apm.consumer.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户需求mq 接收参数
 * @author yinbin
 * @version:
 * @date 2023/11/01 20:32
 */
@Data
public class UserDemandBody implements Serializable {
    private Long id;

    private String changes;

    private LocalDateTime createTime;

    private String creator;

    private String dataBid;

    private String modifier;

    private LocalDateTime modifyTime;

    private String tableName;
}
