package com.transcend.plm.configcenter.common.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 基值导入excel解析监听器
 * @author: renhaijun
 * @create: 2021-11-02 11:25
 **/
@Getter
@Setter
@Slf4j
public class ExcelListener extends AnalysisEventListener<Map<Integer,Object>> {

    /**
     * 存储读取到的表头
     */
    private List<String> head = new ArrayList<>();
    /**
     * 存储读取到的 Excel 数据
     */
    private List<Map<Integer,Object>> data = new ArrayList<Map<Integer,Object>>();

    /**
     * 每解析一行都会回调invoke()方法
     * @param map  读取后的数据对象
     * @param context 内容
     */
    @Override
    public void invoke(Map<Integer,Object> map, AnalysisContext context) {
        if(map != null && !map.isEmpty()) {
            data.add(map);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    /**
     * 处理读取到的表头数据
     * @param headMap
     * @param context
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if(headMap != null && !headMap.isEmpty()) {
            head = headMap.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
        }
    }

    /**
     * 获取表头数据信息
     * @return
     */
    public List<String> getHead() {
        return this.head;
    }

    /**
     * 获取读取到的 Excel 数据
     * @return
     */
    public List<Map<Integer,Object>> getData() {
        return this.data;
    }
}