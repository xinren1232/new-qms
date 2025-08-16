package com.transcend.plm.datadriven.apm.constants;

/**
 * @author shu.zhang
 * @version 1.0
 * @className TodoStateConstant
 * @description desc
 * @date 2024/7/18 16:03
 */
public class TodoCenterConstant {

    private TodoCenterConstant() {
    }

    /**
     * 操作类型：00：发起 01：同意 02：拒绝，03：转交，04:撤回，05:退回，06：转发 07：分案 08：强制归档 09：删除 10：自动审批通过 11：沟通
     */
    public static final String START = "00";
    public static final String AGREE = "01";
    public static final String REFUSE = "02";
    public static final String DELIVER = "03";
    public static final String REVOCATE = "04";
    public static final String BACK = "05";
    public static final String TRANSMIT = "06";
    public static final String DIVISION = "07";
    public static final String MANDATORY_FILING = "08";
    public static final String DELETE = "09";
    public static final String AUTO_APPROVAL_COMPLETE = "10";
    public static final String COMMUNICATE = "11";


    /**
     * 待办状态：10-代办的待审批状态, 11-待办的已锁定状态，15-待办的已取消状态, 19-代办的审批待确认状态，20-代办的已审核状态,30-代办的抄送状态，31-代办的抄送需提交状态, 40-代办的已读状态，41-待办的已阅读并提交状态，
     */
    public static final String APPROVAL_ING = "10";
    public static final String LOCKED = "11";
    public static final String CANCEL = "15";
    public static final String APPROVAL_CONFIRM = "19";
    public static final String APPROVAL_COMPLETE = "20";
    public static final String COPY = "30";
    public static final String COPY_SUBMIT = "31";
    public static final String READ = "40";
    public static final String READ_SUBMIT = "41";

    /**
     * 布局模板类型 01: 三行两列，最大五个。 02: 三行， 最大三个   03: 纯文本
     */
    public static final String LAYOUT_TYPE_01 = "01";
    public static final String LAYOUT_TYPE_02 = "02";
    public static final String LAYOUT_TYPE_03 = "03";


}
