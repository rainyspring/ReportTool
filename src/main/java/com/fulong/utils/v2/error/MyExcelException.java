package com.fulong.utils.v2.error;

public class MyExcelException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4495287645132253683L;

	public MyExcelException() {
		super();
	}

	public MyExcelException(String arg0) {
		super(arg0);
	}

	public MyExcelException(String msg, Throwable cause) {

		super(msg, cause);

	}

	public MyExcelException(Throwable cause) {

		super(cause);

	}
}
