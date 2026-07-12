package com.estudy.entity.query;

import com.estudy.entity.enums.PageSize;
import lombok.Data;

/**
 * 分页信息
 */
@Data
public class SimplePage {
	private int pageNo;		// 当前页码
	private int countTotal;	// 总记录数
	private int pageSize;	// 每页显示数
	private int pageTotal;	// 总页数
	private int start;		// 起始索引
	private int end;		// 本页显示数

	public SimplePage() {
	}

	/**
	 * @param pageNo 当前页码
	 * @param countTotal 总记录数
	 * @param pageSize 每页显示数
	 */
	public SimplePage(Integer pageNo, int countTotal, int pageSize) {
		this.pageNo = pageNo == null ? 0 : pageNo;
		this.countTotal = countTotal;
		this.pageSize = pageSize;
		action();
	}

	/**
	 * limit(start, end)
	 * @param start 起始索引
	 * @param end 本页显示数
	 */
	public SimplePage(int start, int end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * 计算和设置相关的分页属性
	 */
	public void action() {
		// 默认每页显示20条数据
		if (this.pageSize <= 0) {
			this.pageSize = PageSize.SIZE20.getSize();
		}
		// 根据总记录数计算总页数
		if (this.countTotal > 0) {
			this.pageTotal = this.countTotal % this.pageSize == 0 ?
					this.countTotal / this.pageSize :
					this.countTotal / this.pageSize + 1;
		} else {
			pageTotal = 1;
		}
		// 校验当前页码
		if (pageNo <= 1) {
			pageNo = 1;
		}
		if (pageNo > pageTotal) {
			pageNo = pageTotal;
		}
		// 计算分页查询的 起始位置 和 本页显示数
		this.start = (pageNo - 1) * pageSize;
		this.end = this.pageSize;
	}

	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
		this.action();
	}
}
