package com.estudy.entity.query;

import lombok.Data;

/**
 * 分页和排序参数
 */
@Data
public class BaseParam {
	private SimplePage simplePage;	// 分页
	private Integer pageNo;			// 当前页数
	private Integer pageSize;		// 每页记录数
	private String orderBy;			// 排序方式
}
