package com.transcend.plm.datadriven.apm.consumer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.rabbitmq.client.Channel;
import com.transcend.plm.datadriven.apm.api.service.UserDemandService;
import com.transcend.plm.datadriven.apm.common.TranscendForgePublicLogin;
import com.transcend.plm.datadriven.apm.consumer.entity.UserDemandBody;
import com.transcend.plm.datadriven.apm.consumer.entity.UserDemandChange;
import com.transcend.plm.datadriven.apm.notice.PushCenterFeishuBuilder;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transsion.framework.auth.IUser;
import com.transsion.framework.auth.IUserContext;
import com.transsion.framework.auth.UserContextDto;
import com.transsion.framework.auth.dto.UserLoginDto;
import com.transsion.framework.context.holder.UserContextHolder;
import com.transsion.framework.core.context.ServletRequestContextHolder;
import com.transsion.framework.uac.model.dto.LoginUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * tones用户需求更新 mq listener
 *
 * @author yinbin
 * @version:
 * @date 2023/11/01 17:53
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.second.enabled", matchIfMissing = true, havingValue = "true")
public class UserDemandConsumer {
    @Resource
    private UserDemandService userDemandService;
    @Value("#{'${transcend.plm.user-demand.mq.failed.send.emp:}'.split(',')}")
    private List<String> sendEmpNo;

    @RabbitListener(queues = {"send_pi_issue_info"}, containerFactory = "secondFactory")
    public void userDemandListener(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("userDemandListener receive tag: {}, message: {}", deliveryTag, message);
        UserDemandBody userDemandBody = JSON.parseObject(message, UserDemandBody.class);
        Map<String, UserDemandChange> changes = JSON.parseObject(userDemandBody.getChanges(), new TypeReference<Map<String, UserDemandChange>>() {
        });
        try {
            Map<String, Object> params = Maps.newHashMap();
            changes.forEach((key, change) -> params.put(StrUtil.toCamelCase(key), change.getNewValue()));
            params.put(TranscendModelBaseFields.BID, userDemandBody.getDataBid());
            login(userDemandBody.getModifier());
            userDemandService.update(params);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("userDemandListener receive error", e);
            PushCenterFeishuBuilder.builder().title("用户需求同步失败通知")
                    .content(userDemandBody.getModifier() + "\n修改了用户需求,同步transcend失败\n同步消息为:\n" + message)
                    .receivers(sendEmpNo)
                    .send();
        } finally {
            ServletRequestContextHolder.removeContext();
            UserContextHolder.removeUser();
        }
    }

    private void login(String modifier) throws Exception {
        LoginUserDTO loginUser = TranscendForgePublicLogin.getInstance().login();
        TranscendForgePublicLogin.HeaderPassRequest request = TranscendForgePublicLogin.getInstance().forgeRequest(loginUser);
        ServletRequestContextHolder.setContext(request);
        IUserContext<IUser> userContext = getUserContext(loginUser.getUtoken(), modifier);
        UserContextHolder.setUser(userContext);
    }

    @NotNull
    private IUserContext<IUser> getUserContext(String uToken, String modifier) {
        UserLoginDto emp = new UserLoginDto();
        String name = StringUtils.substringBetween(modifier, "", "(");
        String employeeNo = StringUtils.substringBetween(modifier, "(", ")");
        emp.setRealName(name);
        emp.setEmployeeNo(employeeNo);
        return new UserContextDto<>(uToken, emp);
    }


}
