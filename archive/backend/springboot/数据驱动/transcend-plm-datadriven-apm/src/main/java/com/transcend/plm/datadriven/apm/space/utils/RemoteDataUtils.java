package com.transcend.plm.datadriven.apm.space.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.transcend.plm.datadriven.apm.common.TranscendForgePublicLogin;
import com.transcend.plm.datadriven.apm.constants.RemoteDataConstant;
import com.transcend.plm.datadriven.apm.notice.PushCenterProperties;
import com.transcend.plm.datadriven.common.exception.PlmBizException;
import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transsion.framework.common.WebUtil;
import com.transsion.framework.http.util.HttpUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class RemoteDataUtils {


    public static Map<String, Object> getRemoteData(Map<String, Object> map){
        Map<String, String> headerMap = getHeaderMap();
        return getRemoteData(map, headerMap);
    }

    private static Map<String, String> getHeaderMap() {
        HttpServletRequest servletRequest = WebUtil.getServletRequest();
        Map<String, String> headers = WebUtil.getHeaderMap(servletRequest);
        Map<String, String> headerMap = new HashMap<>(CommonConstant.START_MAP_SIZE);
        headerMap.put("P-AppId", TranscendForgePublicLogin.getHeaderValue(headers,"P-AppId"));
        headerMap.put("P-Auth", TranscendForgePublicLogin.getHeaderValue(headers,"P-Auth"));
        headerMap.put("P-Rtoken", TranscendForgePublicLogin.getHeaderValue(headers,"P-Rtoken"));
        return headerMap;
    }

    public static Map<String, Object> getRemoteData(Map<String, Object> map, Map<String, String> headerMap) {
        String method = map.get(RemoteDataConstant.API_METHOD).toString();
        PushCenterProperties pushCenterProperties = PlmContextHolder.getBean(PushCenterProperties.class);
        String url = pushCenterProperties.getGateway() + map.get(RemoteDataConstant.API_URL).toString();
        Map<String, Object> param = new HashMap<>(CommonConstant.START_MAP_SIZE);
        Object params = map.get(RemoteDataConstant.API_PARAMS);
        if (params instanceof JSONArray) {
            params = JSON.parseArray(params.toString());
            for (Object o : (JSONArray) params) {
                JSONObject jsonObject = (JSONObject) o;
                param.put(jsonObject.getString("key"), jsonObject.get("value"));
            }
        } else if (params instanceof String) {
            param = JSON.parseObject(params.toString());
        }
        String label = map.get(RemoteDataConstant.API_LABEL).toString();
        String value = map.get(RemoteDataConstant.API_VALUE).toString();
        String dataPath = map.get(RemoteDataConstant.API_DATA_PATH).toString();
        String childKey = map.getOrDefault(RemoteDataConstant.API_CHILDREN, "").toString();
        String result = null;
        if ("get".equalsIgnoreCase(method)) {
            result = HttpUtil.get(url, headerMap, JSON.toJSONString(param));
        } else if ("post".equalsIgnoreCase(method)) {
            result = HttpUtil.post(url, headerMap, JSON.toJSONString(param));
        }
        Map<String, Object> dataMap = new HashMap<>(16);
        if (StringUtils.isNotBlank(result)) {
            try {
                JSONObject object = JSON.parseObject(result);
                if (!"200".equals(object.getString("code"))) {
                    throw new PlmBizException("500", object.toJSONString());
                }
                JSONArray data = null;
                String[] pathList = dataPath.split("\\.");
                for (int i = 0; i < pathList.length; i++) {
                    if (i != pathList.length - 1) {
                        object = object.getJSONObject(pathList[i]);
                    } else {
                        data = object.getJSONArray(pathList[i]);
                    }
                }
                assert data != null;
                for (Object datum : data) {
                    if (datum instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) datum;
                        if (ObjectUtils.isNotEmpty(jsonObject.get(label)) && ObjectUtils.isNotEmpty(jsonObject.get(value))) {
                            dataMap.put(jsonObject.getString(value), jsonObject.getString(label));
                        }
                        if (StringUtils.isNotBlank(childKey) && jsonObject.containsKey(childKey)) {
                            getChildData(jsonObject, childKey, dataMap, value, label);
                        }
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return dataMap;
    }

    private static void getChildData(JSONObject jsonObject, String childKey, Map<String, Object> dataMap, String value, String label) {
        if (ObjectUtils.isNotEmpty(jsonObject) && jsonObject.containsKey(childKey)) {
            JSONArray jsonArray = jsonObject.getJSONArray(childKey);
            if (ObjectUtils.isNotEmpty(jsonArray)) {
                for (Object datum : jsonArray) {
                    if (datum instanceof JSONObject) {
                        JSONObject datum1 = (JSONObject) datum;
                        if (ObjectUtils.isNotEmpty(datum1.get(label)) && ObjectUtils.isNotEmpty(datum1.get(value))) {
                            dataMap.put(datum1.getString(value), datum1.getString(label));
                        }
                        if (ObjectUtils.isNotEmpty(datum1.get(childKey))) {
                            getChildData(datum1, childKey, dataMap, value, label);
                        }
                    }
                }
            }
        }

    }
}
