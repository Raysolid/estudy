package com.estudy.entity.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PaginationResult<T> {
	private Integer totalCount;	// 总记录数
	private Integer pageSize;	// 每页记录数
	private Integer pageNo;		// 当前页码
	private Integer pageTotal;	// 总页数
	private List<T> list = new ArrayList<T>();	// 当前页的数据列表

	public PaginationResult(Integer totalCount, Integer pageSize, Integer pageNo, List<T> list) {
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.pageNo = pageNo;
		this.list = list;
	}

    public PaginationResult(Integer totalCount, Integer pageSize, Integer pageNo, Integer pageTotal, List<T> list) {
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageNo = pageNo == 0 ? 1 : pageNo;
        this.pageTotal = pageTotal;
        this.list = list;
    }

	public PaginationResult(List<T> list) {
		this.list = list;
	}

	public PaginationResult() {
	}
}
