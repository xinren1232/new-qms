//package com.transcend.plm.configcenter.a_springframework.config.sharding;
//
//
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
//
//import java.util.Collection;
//
//public class OrderShardingAlgorithm implements PreciseShardingAlgorithm<String> {
//
//    private static final String TABLE_NAME_PREFIX = "tb_order_";
//
//    /**
//     * @param collection
//     * @param preciseShardingValue
//     * @return
//     */
//    @Override
//    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
//        String targetTable = TABLE_NAME_PREFIX + preciseShardingValue.getValue();
//        if (collection.contains(targetTable)){
//            return targetTable;
//        }
//
//        throw new UnsupportedOperationException("无法判定的值: " + preciseShardingValue.getValue());
//    }
//}
