package com.transcend.plm.datadriven.apm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author unknown
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendFeishuRequest {
    private String title;
    private String content;
    private List<String> contents;
    private List<String> receivers;
}
