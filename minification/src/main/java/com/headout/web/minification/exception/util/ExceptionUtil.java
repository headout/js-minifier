package com.headout.web.minification.exception.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {

	public static String getStackTraceString(Throwable ex) {
		if (ex == null)
			return null;

		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			sw.close();
			return stackTrace;
		} catch (IOException e) {
			return "";
		}
	}
}
