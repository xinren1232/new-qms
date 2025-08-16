package com.transcend.plm.datadriven.apm.notice;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @Program transsion-ipm
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2022-08-24 14:03
 **/
@Configuration
@ConfigurationProperties(prefix = "transsion.push")
@Data
@Slf4j
public class PushCenterProperties {
    private String applinkurlTemplate = "https://applink.feishu.cn/client/web_app/open?appId={appId}&path=/feishu/feishu/redirect&arId={arId}&redirectUrl={redirectUrl}";
    private String ssourlTemplate ="http://pfgatewayuat.transsion.com:9099/pt-feishu-portal/feishu/redirect?arId={arId}&redirectUrl={redirectUrl}";
    private String gateway = "http://pfgatewayidct.transsion.com:9088";
    private String appKey = "220620001";
    private String appSecret = "e19c5deba725d715dc8ca4919485830da13933bbbdfd33914a452a648ea067fa";
    private String pdcConclusionTemplateCode = "T0000026";
    private String feishuAppId = "562481922736537600";
    private String arid = "5c201dd193689249bfc22c508475f9b1";
    private String urlModelUserTask = "http://ipm-sit.transsion.com/#/panel?type=%s&taskBid=%s";
    private String projectMemberChangeApproval="http://ipm-sit.transsion.com/#/project-manage/details/%s/team";
    private String androidProjectMemberChangeApproval = "https://ipm-uat.transsion.com/app/mobile/#/pages/tabbar/team-role-approve/team-role-approve?dataId=%s";
    private String projectQuestionEdit = "http://ipm-sit.transsion.com/#/project-manage/details/%s/cockpit?sid=%s&pqbid=%s";
    private String sendMailSwitch = "off";

    public String buildUrl(String redirectUri){
        return buildUrl(feishuAppId,arid,false,redirectUri);
    }
    /**
     * 构建飞书免密登录链接<br>
     *
     * @param appId appId（找白霄获取）
     * @param arId arId （找白霄获取）
     * @param openBench openBench true-应用内打开，false-浏览器打开
     * @param redirectUri 业务系统跳转url
     * @return 免密登录链接
     */
    public String buildUrl(String appId, String arId, boolean openBench, String redirectUri) {
        try {
            // 双次url编码
            redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name());
            redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return StringPool.EMPTY;
        }

        if (openBench) {
            // 应用内打开
            return applinkurlTemplate
                    .replace("{appId}", appId)
                    .replace("{arId}", arId)
                    .replace("{redirectUrl}", redirectUri);

        } else {
            // 浏览器打开
            return ssourlTemplate
                    .replace("{arId}", arId)
                    .replace("{redirectUrl}", redirectUri);
        }
    }
}
