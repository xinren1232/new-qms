package com.transcend.plm.datadriven.apm.space.service.impl;

import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.apm.permission.repository.entity.ApmRoleIdentity;
import com.transcend.plm.datadriven.apm.permission.repository.service.ApmRoleIdentityService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmNotifyExecuteRecord;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmNotifyExecuteRecordService;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IApmNotifyService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import com.transcend.plm.datadriven.notify.nofifyEnum.NotifyEnum;
import com.transcend.plm.datadriven.notify.service.NotifyAppService;
import com.transcend.plm.datadriven.notify.vo.NotifyConfigVo;
import com.transcend.plm.datadriven.notify.vo.NotifyExecuteTimeVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
@Service
public class ApmNotifyServiceImpl implements IApmNotifyService {
    @Resource
    private NotifyAppService notifyAppService;
    @Resource
    private ApmRoleIdentityService apmRoleIdentityService;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ApmNotifyExecuteRecordService apmNotifyExecuteRecordService;

    @Override
    public void operateApmNotify(String spaceAppBid, String operateType, MSpaceAppData mSpaceAppData) {
        //先判断是否有配置
        NotifyConfigVo  notifyConfigVo = notifyAppService.getOperateConfig(spaceAppBid, NotifyEnum.BIZ_TYPE_APP.getCode(), operateType,"apm");
        if(notifyConfigVo != null){
            //有配置，执行通知
            //TODO 获取通知角色人员
            List<String> notifyRoleCodes = notifyConfigVo.getNotifyRoleCodes();
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(spaceAppBid);
            //先查实例，实例没有就查应用，应用没有就查空间
            List<ApmRoleIdentity> apmRoleIdentities = apmRoleIdentityService.listEmpByBizBidAndCodes(mSpaceAppData.getBid(),"instance",notifyRoleCodes);
            if(CollectionUtils.isEmpty(apmRoleIdentities)){
                apmRoleIdentities = apmRoleIdentityService.listEmpByBizBidAndCodes(spaceAppBid,"object",notifyRoleCodes);
            }
            if(CollectionUtils.isEmpty(apmRoleIdentities)){
                apmRoleIdentities = apmRoleIdentityService.listEmpByBizBidAndCodes(apmSpaceApp.getSpaceBid(),"space",notifyRoleCodes);
            }
            //TODO 算出通知内容
            String notifyContent = notifyConfigVo.getNotifyContent();
            if(StringUtils.isEmpty(notifyContent)){
                //没有配置通知内容，就用默认的
                if(NotifyEnum.OPERATE_CREATE.getCode().equals(operateType)){
                    notifyContent = SsoHelper.getName()+"新增了"+apmSpaceApp.getName()+"实例:"+mSpaceAppData.getName();
                }else if(NotifyEnum.OPERATE_UPDATE.getCode().equals(operateType)){
                    notifyContent = SsoHelper.getName()+"修改了"+apmSpaceApp.getName()+"实例:"+mSpaceAppData.getName();
                }
            }else{
                for(Map.Entry<String,Object> entry:mSpaceAppData.entrySet()){
                    if(notifyContent.contains("{"+entry.getKey()+"}")){
                        notifyContent = notifyContent.replaceAll("{"+entry.getKey()+"}",entry.getValue().toString());
                    }
                }
            }
            //TODO 计算出通知时间
            NotifyExecuteTimeVo notifyExecuteTimeVo = notifyAppService.getNotifyExecuteTime(notifyConfigVo.getNotifyTimeRuleVo(),mSpaceAppData);
            //保存到通知记录表
            ApmNotifyExecuteRecord apmNotifyExecuteRecord = new ApmNotifyExecuteRecord();
            apmNotifyExecuteRecord.setNotifyConfigBid(notifyConfigVo.getBid());
            apmNotifyExecuteRecord.setTenantCode("apm");
            apmNotifyExecuteRecord.setNotifyContent(notifyContent);
            apmNotifyExecuteRecord.setNofifyTime(notifyExecuteTimeVo.getExecuteTime());
            apmNotifyExecuteRecord.setNotifyWay(notifyConfigVo.getNotifyWay());
            apmNotifyExecuteRecord.setNofifyNow(notifyExecuteTimeVo.getIsNow());
        }
    }


}
