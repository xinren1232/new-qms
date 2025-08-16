package com.transcend.plm.datadriven.filemanager.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件复制执行记录
 *
 * @author bin.yin
 * @date 2024/07/24
 */
@TableName(value ="file_copy_execution_record")
@Data
public class FileCopyExecutionRecordPo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 源文件库
     */
    private String sourceFileLibrary;

    /**
     * 目标文件库
     */
    private String targetFileLibrary;

    /**
     * 复制模式 1:立即 2:延迟 3:计划 4:手动
     */
    private Integer copyMode;

    /**
     * 操作类型 copy:复制 move:移动
     */
    private String operationType;

    /**
     * 处理状态 0:未处理 1:执行中 2:执行成功 3:执行失败 4:数据错误
     */
    private Integer executeState;

    /**
     * 超时时间(秒)
     */
    private Integer timeout;

    /**
     * 下次执行时间
     */
    private Date nextExecuteTime;

    /**
     * 插入时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 执行次数
     */
    private Integer executionTimes;

    /**
     * 执行结果
     */
    private String executionResult;

    /**
     * 源路径
     */
    private String sourcePath;

    /**
     * 目标路径
     */
    private String targetPath;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}