package com.headout.web.minification.settings;

/**
 * This class is used for customizing library functionality accorrding to user provided settings.
 *
 * @author harshal
 *
 */
public class SettingsManager {
	// This setting disables compression completely.
	public static final String SETTING_COMPRESSION_DISABLED = "com.headout.web.minification.compressionDisabled";
	// This setting disables JS compresstion completely.
	public static final String SETTING_JS_COMPRESSION_DISABLED = "com.headout.web.minification.jsCompressionDisabled";
	// This setting disables CSS inline insertion completely.
	public static final String SETTING_CSS_INLINE_INSERTION_DISABLED = "com.headout.web.minification.cssInlineInsertionDisabled";
	// This saetting is used for disabling the caching of inline JS code.
	public static final String SETTING_INLINE_CONTENT_CACHING_DISABLED = "com.headout.web.minification.inlineContentCachingDisabled";
	// This setting is used for allowing stackstrace to be written to JSPWriter in case of error is occurs.
	public static final String SETTING_OUTPUT_STACK_TRACE = "com.headout.web.minification.outputStackTrace";

	// these are being used for disabling compression for request having params for disabling.
	public static final String JS_COMPRESSION_DISABLED = "minification.jsCompressionDisabled";
	public static final String CSS_INLINE_INSERTION_DISABLED = "minification.cssInlineInsertionDisabled";

	private static Boolean compressionDisabled = null;
	private static Boolean jsCompressionDisabled = null;
	private static Boolean cssInlineInsertionDisabled = null;

	private static Boolean inlineContentCachingDisabled = null;
	private static Boolean outputStacktrace = null;

	static {
		compressionDisabled = _getNullableBoolean(System.getProperty(SETTING_COMPRESSION_DISABLED));
		jsCompressionDisabled = _getNullableBoolean(System.getProperty(SETTING_JS_COMPRESSION_DISABLED));
		cssInlineInsertionDisabled = _getNullableBoolean(System.getProperty(SETTING_CSS_INLINE_INSERTION_DISABLED));
		inlineContentCachingDisabled = _getNullableBoolean(
				System.getProperty(SETTING_INLINE_CONTENT_CACHING_DISABLED));
		outputStacktrace = _getNullableBoolean(System.getProperty(SETTING_OUTPUT_STACK_TRACE));
	}

	private static Boolean _getNullableBoolean(String str) {
		if (str == null)
			return null;
		return Boolean.valueOf(str);
	}

	public static boolean isCompressionDisabled() {
		if (compressionDisabled != null)
			return compressionDisabled;

		// NOTE: Default value is FALSE.
		compressionDisabled = Boolean.valueOf(System.getProperty(SETTING_COMPRESSION_DISABLED));
		return compressionDisabled;
	}

	public static boolean isCssInlineInsertionDisabled() {
		if (cssInlineInsertionDisabled != null)
			return cssInlineInsertionDisabled;

		// NOTE: Default value is FALSE.
		cssInlineInsertionDisabled = Boolean.valueOf(System.getProperty(SETTING_CSS_INLINE_INSERTION_DISABLED));
		return cssInlineInsertionDisabled;
	}

	public static boolean isInlineContentCachingDisabled() {
		if (inlineContentCachingDisabled != null)
			return inlineContentCachingDisabled;

		// NOTE: Default value is FALSE.
		inlineContentCachingDisabled = Boolean.valueOf(System.getProperty(SETTING_INLINE_CONTENT_CACHING_DISABLED));
		return inlineContentCachingDisabled;
	}

	public static boolean isJsCompressionDisabled() {
		if (jsCompressionDisabled != null)
			return jsCompressionDisabled;

		// NOTE: Default value is FALSE.
		jsCompressionDisabled = Boolean.valueOf(System.getProperty(SETTING_JS_COMPRESSION_DISABLED));
		return jsCompressionDisabled;
	}

	public static boolean isOutputStacktrace() {
		if (outputStacktrace != null)
			return outputStacktrace;

		// NOTE: Default value is FALSE.
		outputStacktrace = Boolean.valueOf(System.getProperty(SETTING_OUTPUT_STACK_TRACE));
		return outputStacktrace;
	}

	public static void setCompressionDisabled(Boolean compressionDisabled) {
		if (SettingsManager.compressionDisabled == null)
			SettingsManager.compressionDisabled = compressionDisabled;
		else
			System.out.println("WARNING: compressionDisabled was already set to " + SettingsManager.compressionDisabled
					+ ". Not changing value.");
	}

	public static void setCssInlineInsertionDisabled(Boolean cssInlineInsertionDisabled) {
		if (SettingsManager.cssInlineInsertionDisabled == null)
			SettingsManager.cssInlineInsertionDisabled = cssInlineInsertionDisabled;
		else
			System.out.println("WARNING: cssInlineInsertionDisabled was already set to "
					+ SettingsManager.cssInlineInsertionDisabled
					+ ". Not changing value.");
	}

	public static void setInlineContentCachingDisabled(Boolean inlineContentCachingDisabled) {
		if (SettingsManager.inlineContentCachingDisabled == null)
			SettingsManager.inlineContentCachingDisabled = inlineContentCachingDisabled;
		else
			System.out.println(
					"WARNING: compressionDisabled was already set to " + SettingsManager.inlineContentCachingDisabled
							+ ". Not changing value.");
	}

	public static void setJsCompressionDisabled(Boolean jsCompressionDisabled) {
		if (SettingsManager.jsCompressionDisabled == null)
			SettingsManager.jsCompressionDisabled = jsCompressionDisabled;
		else
			System.out.println(
					"WARNING: jsCompressionDisabled was already set to " + SettingsManager.jsCompressionDisabled
							+ ". Not changing value.");
	}

	public static void setOutputStacktrace(Boolean outputStackStrace) {
		if (SettingsManager.outputStacktrace == null)
			SettingsManager.outputStacktrace = outputStackStrace;
		else
			System.out.println(
					"WARNING: compressionDisabled was already set to " + SettingsManager.inlineContentCachingDisabled
							+ ". Not changing value.");
	}

}
