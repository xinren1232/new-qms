package com.transcend.plm.datadriven.apm.log;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.fastjson.JSON;
import com.transcend.framework.common.util.SnowflakeIdWorker;
import com.transcend.framework.sso.tool.SsoHelper;
import com.transcend.plm.datadriven.api.model.BaseDataEnum;
import com.transcend.plm.datadriven.apm.log.model.dto.BaseEsData;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transsion.framework.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Es 基础服务
 * @author yinbin
 * @version:
 * @date 2023/09/28 09:19
 */
@Service
@Slf4j
public class BaseEsService {
    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private ElasticsearchAsyncClient elasticsearchAsyncClient;

    /**
     * 保存单条es数据
     *
     * @param index      index 索引名
     * @param baseEsData baseEsData 数据
     * @version: 1.0
     * @date: 2023/10/7 9:29
     * @author: bin.yin
     */
    public void save(String index, BaseEsData baseEsData) {
        log.info("save es data index: {}, data: {}", index, JSON.toJSONString(baseEsData));
        try {
            if (StringUtil.isBlank(baseEsData.getCreatedBy())) {
                baseEsData.setCreatedBy(SsoHelper.getJobNumber());
            }
            if (StringUtil.isBlank(baseEsData.getCreatedByName())) {
                baseEsData.setCreatedByName(SsoHelper.getName());
            }
            baseEsData.setCreatedTime(new Date());
            IndexResponse indexResponse = elasticsearchClient.index(i -> i
                    .index(index)
                    .id(SnowflakeIdWorker.nextIdStr())
                    .document(baseEsData));
            log.info("save es data id: {}, result:{}", indexResponse.id(), indexResponse.result().jsonValue());
        } catch (IOException e) {
            log.error("save es data error", e);
            throw new PlmBizException("save es data error");
        }
    }

    /**
     * 批量保存es数据
     *
     * @param index    index 索引名
     * @param dataList dataList  数据集合
     * @version: 1.0
     * @date: 2023/10/7 9:52
     * @author: bin.yin
     */
    public void bulkSave(String index, List<? extends BaseEsData> dataList) {
        log.info("save es data index: {}, data: {}", index, JSON.toJSONString(dataList));
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (BaseEsData baseEsData : dataList) {
            if (StringUtil.isBlank(baseEsData.getCreatedBy())) {
                baseEsData.setCreatedBy(SsoHelper.getJobNumber());
            }
            if (StringUtil.isBlank(baseEsData.getCreatedByName())) {
                baseEsData.setCreatedByName(SsoHelper.getName());
            }
            baseEsData.setCreatedTime(new Date());
            br.operations(op -> op
                    .index(idx -> idx
                            .index(index)
                            .id(SnowflakeIdWorker.nextIdStr())
                            .document(baseEsData)
                    )
            );
        }
        try {
            BulkResponse bulkResponse = elasticsearchClient.bulk(br.build());
            if (bulkResponse.errors()) {
                log.error("Bulk had errors");
                for (BulkResponseItem item : bulkResponse.items()) {
                    if (item.error() != null) {
                        log.error(item.error().reason());
                    }
                }
            }
        } catch (IOException e) {
            log.error("bulk save es data error", e);
            throw new PlmBizException("bulk save es data error");
        }
    }

    /**
     * 通过采集器 批量保存es数据
     *
     * @param index        index 索引名
     * @param dataList     dataList 数据集合
     * @param bulkIngester bulkIngester 采集器
     * @version: 1.0
     * @date: 2023/10/7 10:24
     * @author: bin.yin
     */
    public <T> void bulkIngesterSave(String index, List<? extends BaseEsData> dataList, BulkIngester<T> bulkIngester) {
        try {
            for (BaseEsData baseEsData : dataList) {
                if (StringUtil.isBlank(baseEsData.getCreatedBy())) {
                    baseEsData.setCreatedBy(SsoHelper.getJobNumber());
                }
                if (StringUtil.isBlank(baseEsData.getCreatedByName())) {
                    baseEsData.setCreatedByName(SsoHelper.getName());
                }
                baseEsData.setCreatedTime(new Date());
                bulkIngester.add(op -> op
                        .index(idx -> idx
                                .index(index)
                                .id(SnowflakeIdWorker.nextIdStr())
                                .document(baseEsData)
                        )
                );
            }
        } finally {
            bulkIngester.close();
        }
    }

    /**
     * filter查询(不统计score)
     * @param index 索引名
     * @param clazz 返回参数类型
     * @param queryList must查询条件
     * @return List<T>
     * @param <T>
     */
    public <T extends BaseEsData> List<T> queryList(String index, Class<T> clazz, List<Query> queryList) {
        List<T> result = Lists.newArrayList();
        try {
            SearchResponse<T> response = elasticsearchClient.search(s -> s
                            .index(index)
                            .query(q -> q
                                    .bool(b -> b.filter(queryList))
                            )
                            .sort(sort -> sort.field(f -> f.field(BaseDataEnum.CREATED_TIME.getCode()).order(SortOrder.Desc)))
                            .size(10000),
                    clazz
            );
            List<Hit<T>> hits = response.hits().hits();
            for (Hit<T> hit : hits) {
                T source = hit.source();
                result.add(source);
            }
            return result;
        } catch (IOException e) {
            log.error("query es data error", e);
            throw new PlmBizException("query es data error");
        }
    }

    /**
     * 异步保存单条es数据
     * @param index      index
     * @param baseEsData baseEsData
     * @return CompletableFuture<IndexResponse>
     * @version: 1.0
     * @date: 2023/9/28 17:20
     * @author: bin.yin
     */
    public CompletableFuture<IndexResponse> asyncSave(String index, BaseEsData baseEsData) {
        return elasticsearchAsyncClient.index(i -> i.index(index).id(SnowflakeIdWorker.nextIdStr()).document(baseEsData));
    }
}
