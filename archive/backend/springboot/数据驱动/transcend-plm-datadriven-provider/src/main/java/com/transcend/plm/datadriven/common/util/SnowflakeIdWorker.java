package com.transcend.plm.datadriven.common.util;

/**
 * @author Jakeylove3
 * 2017/12/31
 */


import com.transsion.framework.common.InetUtil;
import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Twitter_Snowflake
 * <p>
 * <p>
 * SnowFlake的结构如下(每部分用-分开):
 * <p>
 * <p>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * <p>
 * <p>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * <p>
 * <p>
 * 41位时间戳(毫秒级)，注意，41位时间戳不是存储当前时间的时间戳，而是存储时间戳的差值（当前时间戳 - 开始时间戳)
 * <p>
 * <p>
 * 得到的值），这里的的开始时间戳，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下面程序SnowflakeIdWorker类的startTime属性）。41位的时间戳，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 * <p>
 * <p>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId
 * <p>
 * <p>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间戳)产生4096个ID序号
 * <p>
 * <p>
 * 加起来刚好64位，为一个Long型。
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Slf4j
public class SnowflakeIdWorker {

    /**
     *
     */
    private SnowflakeIdWorker() {
    }

    private static final String WORKER_ID_EVN = "snow_flake_worker_id";
    private static final String DATA_CENTER_ID_ENV = "snow_flake_center_id";

    /**
     * 开始时间戳 (2015-01-01)
     */
    private static final long TWEPOCH = 1420041600000L;

    /**
     * 机器id所占的位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据标识id所占的位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     private static  final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);*/

    /** 支持的最大数据标识id，结果是31
     private static  final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);*/

    /**
     * 序列在id中占的位数
     */
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);

    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);

    /**
     * 机器ID向左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳向左移22位(5+5+12)
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID(0~31)
     */
    private static long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private static long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private static long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private static long lastTimestamp = -1L;


    static {
        datacenterId = getDatacenterId(null);
        workerId = getMaxWorkerId(datacenterId);
    }

    // ==============================Methods==========================================

    /**
     * @param datacenterId
     * @return long
     */
    protected static long getMaxWorkerId(long datacenterId) {
        StringBuilder mpId = new StringBuilder();
        mpId.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (name != null && !name.isEmpty()) {
            // GET jvmPid
            mpId.append(name.split("@")[0]);
        }

        // MAC + PID 的 hashcode 获取16个低位
        return (mpId.toString().hashCode() & 0xffff) % (MAX_WORKER_ID + 1);
    }

    /**
     * @param inetAddress
     * @return long
     */
    protected static long getDatacenterId(InetAddress inetAddress) {
        long id = 0L;
        try {
            if (null == inetAddress) {
                inetAddress = InetUtil.getLocalAddress();
            }

            NetworkInterface network = NetworkInterface.getByInetAddress(inetAddress);

            byte[] mac=null;
            if (null==network) {
                mac = inetAddress.getAddress();
            }
            else {
                mac = network.getHardwareAddress();
            }
            if (null != mac) {
                id = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                id = id % (MAX_DATACENTER_ID + 1);
            }

        } catch (Exception e) {
            log.warn(" getDatacenterId: " + e.getMessage());
        }

        return id;
    }

    /**
     * @return {@link String }
     */
    public static String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public static synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            int tolerateMilliseconds = 2000;
            if(lastTimestamp - timestamp < tolerateMilliseconds){
                // 容忍2秒内的回拨，避免NTP校时造成的异常
                timestamp = lastTimestamp;
            } else{
                // 如果服务器时间有问题(时钟后退) 报错。
                throw new IllegalStateException(StringUtil.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
            }
        }
        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        //上次生成ID的时间戳
        lastTimestamp = timestamp;
        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT) | (datacenterId << DATACENTER_ID_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
    }

    /**
     * 循环等待下一个时间
     *
     * @param lastTimestamp 上次记录的时间
     * @return 下一个时间
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        // 循环直到操作系统时间戳变化
        while (timestamp == lastTimestamp) {
            timestamp = timeGen();
        }
        if (timestamp < lastTimestamp) {
            // 如果发现新的时间戳比上次记录的时间戳数值小，说明操作系统时间发生了倒退，报错
            throw new IllegalStateException(
                    StringUtil.format("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp));
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected static long timeGen() {
        return System.currentTimeMillis();
    }

    /** 测试
     public static void main(String[] args) {
     System.out.println("开始："+System.currentTimeMillis());
     //        SnowflakeIdWorker idWorker = new  static (0, 0);
     for (int i = 0; i < 50; i++) {
     long id = SnowflakeIdWorker.nextId();
     System.out.println(id);
     //            System.out.println(Long.toBinaryString(id));
     }
     System.out.println("结束："+System.currentTimeMillis());
     }
     */
}