package com.transcend.plm.datadriven.apm.space.pojo.dto;

import com.transcend.plm.datadriven.apm.space.pojo.vo.MultiTreeConfigVo;
import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class MultiAppConfig {
    private List<MultiAppTree> multiAppTree;
    private boolean multiTreeViewMode = true;
    private MultiTreeConfigVo multiAppTreeConfig;
    private List<String> multiAppTreeFirstLevel;
    /**
     * 是否导航查询
     */
    private boolean isNavQuery = false;
    /**
     * 导航视图bid
     */
    private String appViewConfigBid;
}
