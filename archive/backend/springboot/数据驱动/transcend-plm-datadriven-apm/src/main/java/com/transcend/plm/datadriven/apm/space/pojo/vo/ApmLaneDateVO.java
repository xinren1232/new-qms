package com.transcend.plm.datadriven.apm.space.pojo.vo;

import com.transcend.plm.datadriven.api.model.MObject;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 泳道包装数据
 * @createTime 2023-11-04 15:18:00
 */
@ToString
public class ApmLaneDateVO extends MObject {
    public Map<String, Collection<MObject>> getChildData() {
        return (Map<String, Collection<MObject>>)this.get("childData");
    }
    /**
     * set方法
     */
    public void setChildData(Map<String, Collection<MObject>> childData) {
        this.put("childData", childData);
    }
}
