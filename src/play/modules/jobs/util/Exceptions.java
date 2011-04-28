package play.modules.jobs.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import play.Logger;

public abstract class Exceptions {

	/**
	 * Gets the stack trace.
	 * 
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(Exception exception) {
		return getStackTrace((Throwable) exception);
	}

	/**
	 * Gets the stack trace.
	 * 
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(Throwable exception) {
		return getStackTrace(null, exception);
	}

	/**
	 * Gets the stack trace.
	 * 
	 * @param title
	 *            the title
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(String title, Exception exception) {
		return getStackTrace(title, (Throwable) exception);
	}

	/**
	 * Gets the stack trace.
	 * 
	 * @param title
	 *            the title
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(String title, Throwable exception) {
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		if (title != null) {
			sb.append(title);
			sb.append("\n\n");
		}
		if (exception != null) {
			ByteArrayOutputStream ostr = new ByteArrayOutputStream();
			exception.printStackTrace(new PrintStream(ostr));
			sb.append(ostr);
		}
		return sb.toString();
	}

	/**
	 * Log stack trace.
	 * 
	 * @param t
	 *            the t
	 */
	public static void logError(Throwable t) {
		Logger.error(getStackTrace(t));
	}
}
