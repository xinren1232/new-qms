package com.transcend.plm.alm.file.event.handler;

import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.feign.IntegrationFeignClient;
import com.transcend.plm.datadriven.api.model.ao.FileActionAo;
import com.transcend.plm.datadriven.apm.event.entity.AddEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractAddEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author bin.yin
 * @description: 文件后置处理
 * 备注：由于新增文件走的底层接口，这里暂时注释掉
 * @version:
 * @date 2024/06/13 15:06
 */
@Slf4j
public class FileAddEventHandler extends AbstractAddEventHandler {
    @Resource
    private IntegrationFeignClient integrationFeignClient;

    private static final String FILE_OBJ_BID = "1229732314621886464";
    public static final String CHANGE = "change";

    @Override
    public MSpaceAppData postHandle(AddEventHandlerParam param, MSpaceAppData result) {
        integrationFeignClient.filePostRecordAdd(
                new FileActionAo(
                        String.valueOf(result.get("fileId")),
                        SsoHelper.getJobNumber(),
                        CHANGE
                )
        );
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(AddEventHandlerParam param) {
        CfgObjectVo objVo = param.getCfgObjectVo();
        String currentObjBid = objVo.getBid();
        return FILE_OBJ_BID.equals(currentObjBid);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
