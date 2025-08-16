package com.transcend.plm.datadriven.common.util;

import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * es工具类
 *
 * @author yinbin
 * @version:
 * @date 2023/10/07 14:32
 */
public class EsUtil {

    /**
     * @author yuanhu.huang
     * @date 2024/07/24
     */
    @Getter
    public enum EsType {
        /**
         * 操作日志
         */
        LOG("log", "操作日志"),
        /**
         * 角色标识日志
         */
        ROLE_IDENTITY("role_identity", "角色配置日志");

        /**
         * @param type
         * @param desc
         */
        EsType(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        /**
         * type
         */
        private final String type;
        /**
         * desc
         */
        private final String desc;

    }

    /**
     * 操作日志索引前缀
     **/
    public static final String LOG_INDEX_PREFIX = "transcend-plm-log";

    /**
     * 业务id
     **/
    public static final String BIZ_ID = "bizId";
    /**
     * 空间bid
     **/
    public static final String SPACE_BID = "spaceBid";
    /**
     * 类型
     */
    public static final String TYPE = "type";

    /**
     * @return {@link String }
     */
    public static String getOperationLogIndex() {
        String activeProfile = PlmContextHolder.getActiveProfile();
        return LOG_INDEX_PREFIX + StrUtil.DASHED + activeProfile;
    }

    /**
     * match查询语句(模糊) 分词查询
     *
     * @param fieldName  查询字段名称
     * @param fieldValue 查询字段值
     * @return query语句
     */
    public static Query initMatchQuery(String fieldName, String fieldValue) {
        return StringUtils.isNotBlank(fieldValue) ? MatchQuery.of(m -> m
                .field(fieldName)
                .query(fieldValue)
        )._toQuery() : null;
    }

    /**
     * range查询语句. 用于between查询
     *
     * @param fieldName 查询字段名称
     * @param begin     开始
     * @param end       结束
     * @return query语句
     */
    public static Query initRangeQuery(String fieldName, Object begin, Object end) {
        return RangeQuery.of(m -> m
                .field(fieldName)
                .gte((JsonData.of(begin)))
                .lte((JsonData.of(end)))
        )._toQuery();
    }

    /**
     * term查询语句.(精确) 用于eq查询
     *
     * @param fieldName  查询字段名称
     * @param fieldValue 查询字段值
     * @return Query
     */
    public static Query initTermQuery(String fieldName, String fieldValue) {
        return StringUtils.isNotBlank(fieldValue) ? TermQuery.of(m -> m
                .field(fieldName)
                .value(fieldValue)
        )._toQuery() : null;
    }

    /**
     * terms查询语句.(精确) 用于in查询
     *
     * @param fieldName 查询字段名称
     * @param valueList 查询字段值列表
     * @return Query
     */
    public static Query initTermsQuery(String fieldName, List<String> valueList) {
        if (CollectionUtils.isEmpty(valueList)) {
            return null;
        }
        List<FieldValue> fieldValues = Lists.newArrayList();
        for (String value : valueList) {
            fieldValues.add(FieldValue.of(JsonData.of(value)));
        }
        return TermsQuery.of(m -> m.field(fieldName).terms(TermsQueryField.of(t -> t.value(fieldValues))))._toQuery();
    }
}
