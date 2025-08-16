package com.transcend.plm.configcenter.lifecycle.domain.service;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelListener<T> extends AnalysisEventListener<T> {
    private static final Logger log = LoggerFactory.getLogger(ExcelListener.class);
    private List<String> headList = new ArrayList();
    private List<T> dataList = new ArrayList();

    public ExcelListener() {
    }

    public void invoke(T t, AnalysisContext analysisContext) {
        Map<String, String> map = (Map)JSON.parseObject(JSON.toJSONString(t), Map.class);
        if (map.size() != 0) {
            this.dataList.add(t);
        }
    }

    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        log.info("表头" + headMap);
        Iterator var3 = headMap.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<Integer, String> map = (Entry)var3.next();
            String value = ((String)map.getValue()).replace(" ", "");
            this.headList.add(value);
        }

    }

    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("导入的数据" + this.dataList.toString());
        log.info("处理空格后的表头" + this.headList);
    }

    public List<String> getHeadList() {
        return this.headList;
    }

    public List<T> getDataList() {
        return this.dataList;
    }

    public void setHeadList(final List<String> headList) {
        this.headList = headList;
    }

    public void setDataList(final List<T> dataList) {
        this.dataList = dataList;
    }
}
