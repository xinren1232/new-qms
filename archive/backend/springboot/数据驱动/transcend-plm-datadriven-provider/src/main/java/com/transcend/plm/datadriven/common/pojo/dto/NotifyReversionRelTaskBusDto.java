package com.transcend.plm.datadriven.common.pojo.dto;

import com.transcend.plm.datadriven.api.model.MObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/28 13:45
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class NotifyReversionRelTaskBusDto {
    /** 空间bid **/
    private String spaceBid;
    /** 空间应用bid **/
    private String spaceAppBid;
    /** 当前源数据bid **/
    private String currentSourceBid;
    /** 当前源数据dataBid**/
    private String currentSourceDataBid;
    /** 当前目标数据列表 **/
    private List<? extends MObject> currentTargetList;
    /** 当前关系modelCode  **/
    private String currentRelationModelCode;
    /** 当前源modelCode **/
    private String currentSourceModelCode;
    /** 当前目标modelCode **/
    private String currentTargetModelCode;
    /** 配置 **/
    private String config;
    /** 由于框架底层UserContextHolder不支持多线程，这里传过来 **/
    private String jobNumber;
}
