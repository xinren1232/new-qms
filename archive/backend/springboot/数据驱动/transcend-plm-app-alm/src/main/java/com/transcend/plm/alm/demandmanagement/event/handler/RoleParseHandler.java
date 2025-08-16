package com.transcend.plm.alm.demandmanagement.event.handler;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.alm.demandmanagement.config.RoleParseProperties;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Describe 需求管理生成RR 领域后置事件
 * @Author yuanhu.huang
 * @Date 2024/7/10
 */
@Slf4j
@Component
public class RoleParseHandler extends AbstractAddEventHandler {
    @Resource
    private RoleParseProperties roleParseProperties;

    @Resource
    private IRuntimeService runtimeService;

    /**
     * RR处理前置事件
     * @param param 入参
     * @return
     */
    @Override
    public AddEventHandlerParam preHandle(AddEventHandlerParam param) {
        Map<String, Map<String, String>> modelCodeRoleMap = roleParseProperties.getModelCodeRoleMap();
        Map<String, String> roleMap = modelCodeRoleMap.get(param.getApmSpaceApp().getModelCode());
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        Map<String,List<String>> domainRoleMap = new HashMap<>(16);
        if(CollectionUtils.isNotEmpty(roleMap)){
            roleMap.forEach((attr,roleCode)->{
                List<String> users = getObjectList(mSpaceAppData.get(attr));
                if (CollectionUtils.isNotEmpty(users)){
                    domainRoleMap.put(roleCode,users);
                }
            });
        }
        if(CollectionUtils.isNotEmpty(domainRoleMap)){
            for(Map.Entry<String,List<String>> entry : domainRoleMap.entrySet()){
                runtimeService.updateFlowRoleUsers(mSpaceAppData.getBid(),entry.getValue(),param.getSpaceBid(),param.getApmSpaceApp().getBid(),entry.getKey());
            }
        }
        return super.preHandle(param);
    }

    @Override
    public MSpaceAppData postHandle(AddEventHandlerParam param, MSpaceAppData result) {
        return super.postHandle(param, result);
    }
    private List<String> getObjectList(Object object){
        List<String> list = new ArrayList<>();
        if(object == null){
            return list;
        }
        if(object instanceof List){
            list = JSON.parseArray(object.toString(), String.class);
        }else if(object instanceof String){
            String objectStr = (String) object;
            //将objectStr中"替换成空格
            objectStr = objectStr.replaceAll("\"", "");
            if(StringUtils.isNotEmpty(objectStr)){
                list.add(objectStr);
            }
        }else{
            if(object.toString().startsWith(CommonConstant.OPEN_BRACKET)){
                list = JSON.parseArray(object.toString(), String.class);
            }else{
                list.add(object.toString());
            }
        }
        return list;
    }

    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        return roleParseProperties.getModelCodeRoleMap().containsKey(param.getApmSpaceApp().getModelCode());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
