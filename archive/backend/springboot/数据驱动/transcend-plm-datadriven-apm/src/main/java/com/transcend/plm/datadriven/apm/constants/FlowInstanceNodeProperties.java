package com.transcend.plm.datadriven.apm.constants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "transcend.instance.node")
public class FlowInstanceNodeProperties {


    /** RR状态流程退回确认节点 **/
    private String rrBack = "zcOs7_5Y5cNgQ5iL_";

    /** RR任务流程需求分析节点 **/
    private String rrAnalysis = "OBoXr_LI1YAOpMqX_";

    /** RR状态流程领域价值评估节点节点 **/
    private String rrAssessment = "kP_--C28QTqGo-mr_";

    /** IR状态流程价值决策节点 **/
    private String irValueDecision = "IDXnlTeag20DmAw5_";




}
