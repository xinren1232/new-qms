package com.transcend.plm.datadriven.config.eventbus.posthandler.strategy.handler.file;

import com.transcend.plm.datadriven.api.feign.IntegrationFeignClient;
import com.transcend.plm.datadriven.api.model.ao.FileActionAo;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import com.transcend.plm.datadriven.config.eventbus.posthandler.strategy.handler.AbstractHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/4/30
 */
@Component
public class FilePostHandler extends AbstractHandler {


    private static final String FILE_OBJ_BID = "1229732314621886464";
    public static final String CHANGE = "change";

    @Resource
    private IntegrationFeignClient integrationFeignClient;

    @Override
    protected void doCurrentAction(NotifyPostEventBusDto postEventBusDto) {
        integrationFeignClient.filePostRecordAdd(
                new FileActionAo(
                        String.valueOf(postEventBusDto.getObject().get("fileId")),
                        postEventBusDto.getJobNumber(),
                        CHANGE
                )
        );
    }

    @Override
    public boolean isMatch(String currentObjBid) {
        return FILE_OBJ_BID.equals(currentObjBid);
    }


    @Override
    public int getOrder() {
        return 1;
    }
}
