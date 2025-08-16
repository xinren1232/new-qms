package com.transcend.plm.datadriven.apm.dto;

import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jinpeng.bai
 * @version v1.0.0
 * @description
 * @date 2024/04/25 14:29
 **/
@Data
@Accessors(chain = true)
public class NotifyObjectPartialContentDto {

    /**
     * 对象编码
     */
    private String modelCode;

    /**
     * 数据bid
     */
    private String bid;

    /**
     * 跟新数据
     */
    private MSpaceAppData mSpaceAppData;

    public static NotifyObjectPartialContentDto of(){
        return new NotifyObjectPartialContentDto();
    }
}
