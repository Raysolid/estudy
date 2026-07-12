package com.estudy.entity.enums;

import lombok.Getter;

/**
 * 每页显示数枚举
 */
@Getter
public enum PageSize {
	SIZE10(10), SIZE15(15), SIZE20(20), SIZE30(30), SIZE40(40), SIZE50(50);

	private final int size;

	PageSize(int size) {
		this.size = size;
	}
}
