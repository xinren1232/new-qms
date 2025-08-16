package com.transcend.plm.datadriven.apm.flow.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.apm.constants.TodoCenterConstant;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.IToDoCenterService;
import com.transcend.plm.datadriven.apm.notice.PushSendResult;
import com.transcend.plm.datadriven.apm.permission.pojo.bo.ApmUser;
import com.transcend.plm.datadriven.apm.permission.service.impl.PlatformUserWrapper;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.constant.CommonConst;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transsion.framework.dto.BaseResponse;
import com.transsion.framework.uac.model.dto.DepartmentDTO;
import com.transsion.framework.unifiedapproval.dto.mode2.*;
import com.transsion.framework.unifiedapproval.service.sdk.IUaSdkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Slf4j
@Service
public class ToDoCenterServiceImpl implements IToDoCenterService {

    @Resource
    private PlatformUserWrapper platformUserWrapper;

    @Resource
    private IUaSdkService uaSdkService;

    @Value("${transcend.todo.center.appId:bpm}")
    private String todoCenterAppId;

    public static final String SYS_CODE = "TRANSCEND";

    public static final String DAN_JU_HAO = "danjuhao";

    @Value("${transcend.todo.center.switch:off}")
    private String sendTodoSwitch;

    public static final String FLOW_TEMPLATE_ID = "TRANSCEND_2024";

    public static final String FLOW_TEMPLATE_NAME = "TRANSCEND";

    public static final String REGION = "sz";

    @Async
    @Override
    public void pushTodoTaskData(ApmFlowInstanceNode apmFlowInstanceNode, MSpaceAppData mSpaceAppData, List<String> empNos, String content, String appLink) {
        FlowStartDTO flowStart = new FlowStartDTO();
        // 系统参数
        flowStart.setSysCode(SYS_CODE);
        // flowStart.setAppId(SYS_CODE); 暂时使用bpm，代办中心最近在改造
        flowStart.setAppId(todoCenterAppId);

        // 流程实例唯一标识
        String instanceBid = apmFlowInstanceNode.getInstanceBid();
        flowStart.setBusinessId(instanceBid);

        ApmUser applicantUserInfo = platformUserWrapper.getUserBOByEmpNO(mSpaceAppData.getCreatedBy());
        String applicantDeptId = applicantUserInfo.getDepartmentList().get(0);
        DepartmentDTO applicantDeptInfo = platformUserWrapper.getDepartmentByDepartmentId(applicantDeptId);
        String version = Long.toString(System.currentTimeMillis());
        String createdTime = DateUtil.formatDateTime(DateUtil.offsetHour(DateUtil.parseDateTime(DateUtil.formatLocalDateTime(mSpaceAppData.getCreatedTime())), -8));
        // 流程申请人参数
        flowStart.setCreatedBy(applicantUserInfo.getEmpNo());
        flowStart.setCreatedByName(applicantUserInfo.getName());
        flowStart.setCreatedOn(createdTime);
        flowStart.setFromOneworks(false);
        flowStart.setDeptId(applicantDeptInfo.getDeptNo());
        flowStart.setDeptName(applicantDeptInfo.getName());
        flowStart.setVersion(version);

        // 模板参数
        String flowNo = String.valueOf(mSpaceAppData.get(DAN_JU_HAO) == null ? apmFlowInstanceNode.getBid() : mSpaceAppData.get(DAN_JU_HAO));
        flowStart.setFlowTemplateID(FLOW_TEMPLATE_ID);
        flowStart.setFlowTemplateName(FLOW_TEMPLATE_NAME);
        flowStart.setFlowNo(flowNo);

        // 详情参数：流程审核人信息
        ArrayList<TodoDTO> todoList = new ArrayList<>();
        List<ContextDTO> contextList = Collections.singletonList(new ContextDTO());
        empNos.forEach(empNo -> {
            TodoDTO todo = new TodoDTO();
            // 任务唯一标识
            ApmUser approvalUserInfo = platformUserWrapper.getUserBOByEmpNO(empNo);
            String approvalDeptId = approvalUserInfo.getDepartmentList().get(0);
            DepartmentDTO approvalDeptInfo = platformUserWrapper.getDepartmentByDepartmentId(approvalDeptId);
            String now = DateUtil.formatDateTime(DateUtil.offsetHour(DateUtil.date(), -8));
            todo.setTaskID(apmFlowInstanceNode.getBid() + approvalUserInfo.getEmpNo());
            todo.setTaskCreatedOn(now);
            todo.setApprover(approvalUserInfo.getEmpNo());
            todo.setApprovalName(approvalUserInfo.getName());
            todo.setDeptId(approvalDeptInfo.getDeptNo());
            todo.setDeptName(approvalDeptInfo.getName());

            todo.setIsRead("0");
            todo.setVersion(version);

            // 审批节点内容
            todo.setOperate(TodoCenterConstant.AGREE);
            // 节点id
            todo.setNodeId(apmFlowInstanceNode.getTemplateNodeBid());
            todo.setNodeName(apmFlowInstanceNode.getNodeName());
            // 移动端跳转地址, 去除零宽空格，否则跳转有问题
            todo.setUrl(appLink.replaceAll("\\p{Cf}", ""));
            // PC端跳转地址，去除零宽空格，否则跳转有问题
            todo.setPcUrl(appLink.replaceAll("\\p{Cf}", ""));
            todo.setState(TodoCenterConstant.APPROVAL_ING);

            // 冗余字段
            todo.setEdit(true);
            todo.setFlowNo(flowNo);
            todo.setSysCode(SYS_CODE);
            todo.setBusinessId(instanceBid);
            todo.setRegion(REGION);

            // 审批详情
            DetailDTO detail = new DetailDTO();
            // 设置主题
            TopicalDTO topical = new TopicalDTO();
            topical.setAc(approvalUserInfo.getEmpNo());
            topical.setUm(approvalUserInfo.getName());
            topical.setDp(approvalDeptInfo.getName());
            topical.setTm(now);
            topical.setTp(mSpaceAppData.getName() + "-" + apmFlowInstanceNode.getNodeName());
            detail.setTopical(topical);
            // 设置摘要
            AbstractDTO abstracts = new AbstractDTO();
            abstracts.setTemplate(TodoCenterConstant.LAYOUT_TYPE_02);
            abstracts.setDetails(content);
            detail.setStract(abstracts);
            // 设置模板
            String formData = String.format("{\"config\":{\"publish\":false,\"view\":\"all\"}," +
                            "\"metaData\":{\"i18n\":{\"zh\":{\"baseInfo\":{\"propertys\":{\"title\":{\"name\":\"标题\"}," +
                            "\"flowNo\":{\"name\":\"流程编码\"},\"flowProposer\":{\"name\":\"申请人\"}," +
                            "\"flowDept\":{\"name\":\"申请部门\"},\"flowStartDate\":{\"name\":\"申请日期\"}}}}}}," +
                            "\"bizData\":{\"i18n\":{\"zh\":{\"baseInfo\":{\"title\":\"%s\",\"flowNo\":\"%s\"," +
                            "\"flowProposer\":\"%s\",\"flowDept\":\"%s\",\"flowStartDate\":\"%s\"}}}}}",
                    mSpaceAppData.getName(), flowNo, applicantUserInfo.getName(), approvalDeptInfo.getName(), createdTime);
            List<Map<String, Object>> dataTemplateList = Lists.newArrayList();
            dataTemplateList.add(JSON.parseObject(formData));
            detail.setContexts_new(dataTemplateList);
            detail.setContexts(contextList);
            todo.setDetail(detail);

            todoList.add(todo);
        });
        flowStart.setFlows(todoList);
        BaseResponse<Boolean> start = uaSdkService.start(flowStart);
        if (!start.isSuccess()) {
            log.error("推送待办失败：{}", JSON.toJSONString(start));
        }

    }

    @Override
    public void pushTodoTaskState(ApmFlowInstanceNode apmFlowInstanceNode, MSpaceAppData mSpaceAppData, List<String> empNos, String content, String appLink, String operate, String state) {
        if (!CommonConst.SWITH_ON.equalsIgnoreCase(sendTodoSwitch)) {
            log.info("推送待办开关未开启");
            PushSendResult.builder().success(false).message("推送待办开关未开启");
        }

        if(apmFlowInstanceNode == null || CollectionUtils.isEmpty(empNos)){
            return;
        }
        FlowStartDTO flowStart = new FlowStartDTO();
        // 系统参数
        flowStart.setSysCode(SYS_CODE);
        flowStart.setAppId(todoCenterAppId);

        // 流程实例唯一标识
        String instanceBid = apmFlowInstanceNode.getInstanceBid();
        flowStart.setBusinessId(instanceBid);

        // 流程的申请人信息
        ApmUser applicantUserInfo = platformUserWrapper.getUserBOByEmpNO(mSpaceAppData.getCreatedBy());
        String applicantDeptId = applicantUserInfo.getDepartmentList().get(0);
        DepartmentDTO applicantDeptInfo = platformUserWrapper.getDepartmentByDepartmentId(applicantDeptId);
        String version = Long.toString(System.currentTimeMillis());
        String createdTime = DateUtil.formatDateTime(DateUtil.offsetHour(DateUtil.parseDateTime(DateUtil.formatLocalDateTime(mSpaceAppData.getCreatedTime())), -8));
        flowStart.setCreatedBy(applicantUserInfo.getEmpNo());
        flowStart.setCreatedByName(applicantUserInfo.getName());
        flowStart.setCreatedOn(createdTime);
        flowStart.setFromOneworks(false);
        flowStart.setDeptId(applicantDeptInfo.getDeptNo());
        flowStart.setDeptName(applicantDeptInfo.getName());
        flowStart.setVersion(version);

        // 模板参数
        String flowNo = String.valueOf(mSpaceAppData.get(DAN_JU_HAO) == null ? apmFlowInstanceNode.getBid() : mSpaceAppData.get(DAN_JU_HAO));
        flowStart.setFlowTemplateID(FLOW_TEMPLATE_ID);
        flowStart.setFlowTemplateName(FLOW_TEMPLATE_NAME);
        flowStart.setFlowNo(flowNo);

        List<TodoDTO> todoList = new ArrayList<>();
        List<ContextDTO> contextList = Collections.singletonList(new ContextDTO());
        empNos.forEach(empNo -> {
            // 待办信息
            TodoDTO todo = new TodoDTO();
            ApmUser approvalUserInfo = platformUserWrapper.getUserBOByEmpNO(empNo);
            if (approvalUserInfo != null) {
                String approvalDeptId = approvalUserInfo.getDepartmentList().get(0);
                DepartmentDTO approvalDeptInfo = platformUserWrapper.getDepartmentByDepartmentId(approvalDeptId);
                String now = DateUtil.formatDateTime(DateUtil.offsetHour(DateUtil.date(), -8));
                todo.setTaskID(apmFlowInstanceNode.getBid() + approvalUserInfo.getEmpNo());
                todo.setNodeId(apmFlowInstanceNode.getTemplateNodeBid());
                todo.setNodeName(apmFlowInstanceNode.getNodeName());
                todo.setApprover(approvalUserInfo.getEmpNo());
                todo.setApprovalName(approvalUserInfo.getName());
                todo.setApprovalTime(now);
                todo.setDeptId(approvalDeptInfo.getDeptNo());
                todo.setDeptName(approvalDeptInfo.getName());

                todo.setIsRead("1");
                todo.setVersion(version);
                todo.setOperate(operate);
                todo.setState(state);
                todo.setEdit(true);
                todo.setUrl(appLink.replaceAll("\\p{Cf}", ""));
                todo.setPcUrl(appLink.replaceAll("\\p{Cf}", ""));
                todo.setFlowNo(flowNo);
                todo.setSysCode(SYS_CODE);
                todo.setBusinessId(instanceBid);
                todo.setRegion(REGION);

                // 审批详情
                DetailDTO detail = new DetailDTO();
                // 设置主题
                TopicalDTO topical = new TopicalDTO();
                topical.setAc(approvalUserInfo.getEmpNo());
                topical.setUm(approvalUserInfo.getName());
                topical.setDp(approvalDeptInfo.getName());
                topical.setTm(now);
                topical.setTp(mSpaceAppData.getName() + "-" + apmFlowInstanceNode.getNodeName());
                detail.setTopical(topical);
                // 设置摘要
                AbstractDTO abstracts = new AbstractDTO();
                abstracts.setTemplate(TodoCenterConstant.LAYOUT_TYPE_02);
                abstracts.setDetails(content);
                detail.setStract(abstracts);
                detail.setContexts(contextList);
                todo.setDetail(detail);
                todoList.add(todo);
            }
        });
        flowStart.setFlows(todoList);
        BaseResponse<Boolean> todoState = uaSdkService.todoState(flowStart);
        if (!todoState.isSuccess()) {
            log.error("修改代办任务状态失败：{}", JSON.toJSONString(todoState));
        }
    }

}
