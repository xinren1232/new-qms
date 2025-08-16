package com.transcend.plm.datadriven.api.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

/**
 * 文件属性
 * @author jinpeng.bai
 * @version v1.0.0
 * @date 2024/07/26 11:33
 **/
@ToString
public class FileVO {
    @ApiModelProperty(
            value = "文件业务编码",
            example = "20210131003"
    )
    private String fileBid;
    @ApiModelProperty(
            value = "文件名",
            example = "设计文档"
    )
    private String name;
    @ApiModelProperty(
            value = "文件大小",
            example = "20k"
    )
    private String size;
    @ApiModelProperty(
            value = "文件格式",
            example = "xlsx"
    )
    private String format;
    @ApiModelProperty(
            value = "文件存储路径",
            example = "/opt/data/test"
    )
    private String url;

    public String getFileBid() {
        return fileBid;
    }

    public void setFileBid(String fileBid) {
        this.fileBid = fileBid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
