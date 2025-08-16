package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.datadriven.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * @author shu.zhang
 * @version 1.0
 * @className CadBatchAddInstanceRecord
 * @description desc
 * @date 2024/8/16 15:19
 */
@TableName(value ="apm_cad_record", autoResultMap = true)
@Data
public class CadBatchAddInstanceRecord extends BasePoEntity implements Serializable {

    /**
     * 空间业务id
     */
    private String spaceBid;

    /**
     * 领域Bid
     */
    private String spaceAppBid;

    /**
     * 唯一id
     */
    private String requestId;

    /**
     * 实例数据
     */
    private String instanceData;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
