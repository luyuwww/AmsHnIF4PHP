package cn.lams.util;

import cn.lams.pojo.SGroup;
import cn.lams.pojo.SUser;
import cn.lams.service.BaseService;

public class ExceptionThrows extends Exception {
	private static final long serialVersionUID = 1L;
	String errorMessage;

	public ExceptionThrows(String errorMessage)

	{

		this.errorMessage = errorMessage;

	}

	public String toString()

	{

		return errorMessage;

	}

	public String getMessage()

	{

		return errorMessage;

	}

}

