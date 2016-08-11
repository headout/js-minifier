package com.headout.web.minification.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.headout.web.minification.common.CacheKey;
import com.headout.web.minification.common.CommonTagHandler;
import com.headout.web.minification.common.Compressor;
import com.headout.web.minification.settings.SettingsManager;

/**
 * This class replaces 'Inline JS code' with minified JS code using JSP customtag.
 *
 *
 * @author harshal
 *
 */
public class JSCompressTagHandler extends CommonTagHandler implements DynamicAttributes {
	// TODO: We need to have a Global level Exception Handler which will report out in the UI if an error occurred.

	// TODO: Review docs for all new IMPL.

	private static final long serialVersionUID = -9061910238981274919L;

	/**
	 * handles minification of JS code which lies inside JScompress tag.
	 */
	@Override
	public int doAfterBody() throws JspTagException {
		boolean writtenToJspWritter = false;

		// getPreviousOut() is used here . It pops previous out content, and so we have to write body-content to buffer
		// in any case(minified or non-minified).
		JspWriter out = getPreviousOut();
		BodyContent bc = getBodyContent();

		if (!("true".equals(pageContext.getRequest().getParameter(SettingsManager.JS_COMPRESSION_DISABLED))
				|| SettingsManager.isCompressionDisabled()
				|| SettingsManager.isJsCompressionDisabled())) {

			String absolutePath = Compressor.getAbsolutePath((HttpServletRequest) pageContext.getRequest(), null);
			Boolean isInline = true;

			// If working environment is non-production then, for each and every request inline JS code is compiled.
			// If working environment is production then, inline JS code is compiled only for first request. Also
			// cache the minified code till file doesn't get modified.
			if (!SettingsManager.isInlineContentCachingDisabled())
				compressor.minify(absolutePath, bc, getJspId(), isInline,
						Compressor.ContentType.JS);
			else
				compressor.minifyOnFly(bc,
						CacheKey.generateKey(absolutePath, getJspId(), isInline));

			// check if there are errors in compilation, before writing to buffer.

			throwExceptionIfErrors(out, null, isInline);
			writeToJspWriter(out, null, compressor.afterMinified);
			writtenToJspWritter = true;

		}
		if (!writtenToJspWritter) {
			writeBodyToJspWriter(out, null, bc);
		}
		return SKIP_BODY;
	}

}
