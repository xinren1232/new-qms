package com.transcend.plm.configcenter.api.model.object.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zhihui.yu
 * @date 2022年9月23日 下午5:17:35
 * @description
 */
@Data
public class DeepObject<T> {
	String name;
	List<T> list;
}
