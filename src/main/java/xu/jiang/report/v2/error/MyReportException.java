package xu.jiang.report.v2.error;

public class MyReportException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4495287645132253683L;

	public MyReportException() {
		super();
	}

	public MyReportException(String arg0) {
		super(arg0);
	}

	public MyReportException(String msg, Throwable cause) {

		super(msg, cause);

	}

	public MyReportException(Throwable cause) {

		super(cause);

	}
}
