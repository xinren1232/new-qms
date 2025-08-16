package com.transcend.plm.alm.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 简单SR数据传输对象
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/20 12:05
 */
@Data
public class SimpleSrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据唯一业务ID
     */
    private String bid;
    /**
     * SR编码
     */
    private String coding;
    /**
     * SR名称
     */
    private String name;

}
