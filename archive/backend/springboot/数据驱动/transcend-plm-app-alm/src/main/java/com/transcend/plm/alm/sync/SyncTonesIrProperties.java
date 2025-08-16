package com.transcend.plm.alm.sync;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "transcend.plm.apm.sync.tones")
@Getter
@Setter
@ToString
public class SyncTonesIrProperties {


    /**
     * 要导入的空间BID
     **/

    private String spaceBid ="1195029861299785728";

    /**
     * 要导入的RR应用bid
     **/

    private String irSpaceAppBid ="1253665116014346240";
    private String srSpaceAppBid ="1253654532919947264";
    private String arSpaceAppBid ="1268235106967724032";

    private String irSrRelationModelCode ="A5O";

    private String srArRelationModelCode ="A62";


    /**
     * 领域维护应用bid
     **/

    private String productSpaceBid ="1195030377085931520";

    /**
     * 业务模块应用bid
     **/

    private String moduleSpaceBid ="1197488637572153344";

    /**
     * RR状态流程启动参数
     **/

    private String irWorkItemType = "";
    private String srWorkItemType = "1253738516127444992";
    private String arWorkItemType = "";


    private String irOsVersion = "1251219000928628736";

    /**
     * RR生命周期模板Bid
     **/

    private String irLcTemplBid = "1255546076983750656";
    private String srLcTemplBid = "1287844423093022720";
    private String arLcTemplBid = "1254802664343826432";


    private String srTempNodeBid = "1287844341506609153";
    //private String srTempNodeBid = "1286375619114569729";
/**
     * RR生命周期模板版本
     **/

    private String irLcTemplVersion = "V51";
    private String srLcTemplVersion = "V2";
    private String arLcTemplVersion = "V13";

/**
     * 生产环境r_token(因为下载tones文件需要)
     **/

    private String prodRtoken = "r_NDI3MWVyMXIwZTJvMGl5dHhtZjZeNDQ1NjU1ODIxMjk4NjAzNjAyMTgxMjQ1";

/**
     * 生产环境u_token
     **/

    private String prodUtoken = "u_MjE3MWl1cTcwaWVwamlyaTlwaGFeNDQ1NjU1ODIxNDc1NTUyNjAyMTgxMjQ1";

}