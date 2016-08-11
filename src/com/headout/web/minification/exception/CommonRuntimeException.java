package com.headout.web.minification.exception;

public class CommonRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CommonRuntimeException(Throwable ex, String message) {
		super(message, ex);
	}
}
