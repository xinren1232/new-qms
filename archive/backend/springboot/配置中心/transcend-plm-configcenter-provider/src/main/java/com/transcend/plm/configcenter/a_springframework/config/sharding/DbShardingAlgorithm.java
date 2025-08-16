//package com.transcend.plm.configcenter.a_springframework.config.sharding;
//
//
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
//import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
//
//import java.util.Collection;
//
//public class DbShardingAlgorithm implements PreciseShardingAlgorithm<String> {
//
//    private static final String DB_NAME_PREFIX = "test_";
//
//
//    /**
//     * @param collection
//     * @param preciseShardingValue
//     * @return
//     */
//    @Override
//    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
//        String targetTable = DB_NAME_PREFIX + "ali";
//        return targetTable;
////        if (availableTargetNames.contains(targetTable)){
////            return targetTable;
////        }
////
////        throw new UnsupportedOperationException("无法判定的值: " + shardingValue.getValue());
//    }
//}
