package com.headout.web.minification.exception;

import com.google.javascript.jscomp.JSError;

public class JSCompileException extends RuntimeException {
	private static final long serialVersionUID = 8188673809802803624L;

	private static String convertToErrorString(JSError[] errors) {
		StringBuilder errorBuilder = new StringBuilder();
		for (JSError jsError : errors) {
			if (errorBuilder.length() > 0)
				errorBuilder.append('\n');
			errorBuilder.append(jsError.toString());
		}
		return errorBuilder.toString();
	}

	private final JSError[] errors;

	public JSCompileException(JSError[] errors) {
		super("Js compilation failed due to error in JS file :\n" + JSCompileException.convertToErrorString(errors));
		this.errors = errors;
	}

	public JSError[] getErrors() {
		return errors;
	}
}
