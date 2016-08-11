package com.headout.web.minification.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.headout.web.minification.common.CacheKey;
import com.headout.web.minification.common.CommonTagHandler;
import com.headout.web.minification.common.Compressor;
import com.headout.web.minification.common.Tag;
import com.headout.web.minification.settings.SettingsManager;

/**
 * This class replaces 'Inline JS code' or 'JS code which is referenced through External file' with minified JS code
 * using JSP customtag.
 *
 * @author harshal
 *
 */
public class ScriptTagHandler extends CommonTagHandler implements DynamicAttributes {
	// TODO: We need to have a Global level Exception Handler which will report out in the UI if an error occurred.

	private static final long serialVersionUID = -9061910238981274919L;
	private static final String TYPE = "type";
	private static final String SRC = "src";
	private static final String COMPRESS = "compress";

	private String type;
	private String src;

	private String compress;

	private boolean writtenToJspWritter = false;

	/**
	 * This method is invoked by container for those tags which has non-empty body-content. Which means Inline JS code
	 * minification is handled by this method
	 */
	@Override
	public int doAfterBody() throws JspTagException {
		// getPreviousOut() is used here . It pops previous out content, and so we have to write body-content to buffer
		// in any case(minified or non-minified).
		JspWriter out = getPreviousOut();
		BodyContent bc = getBodyContent();

		if (!("true".equals(pageContext.getRequest().getParameter(SettingsManager.JS_COMPRESSION_DISABLED))
				|| SettingsManager.isCompressionDisabled() || SettingsManager.isJsCompressionDisabled())) {
			compress = (String) attrributeMap.get(COMPRESS);
			type = (String) attrributeMap.get(TYPE);
			src = (String) attrributeMap.get(SRC);
			if ((type == null || "text/javascript".equals(type) || "application/javascript".equals(type))) {

				// If compress flag is false then don't minify ,and write original unminified body-content to JspWriter
				if ("false".equals(compress)) {
					attrributeMap.remove("compress");
					writeToJspWriter(out, Tag.SCRIPT, bc.getString());
					writtenToJspWritter = true;
					return SKIP_BODY;
				}
				String absolutePath = Compressor.getAbsolutePath((HttpServletRequest) pageContext.getRequest(), src);
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

				// check if there are errors in compilation,before writting to buffer.
				throwExceptionIfErrors(out, Tag.SCRIPT, isInline);

				writeToJspWriter(out, Tag.SCRIPT, compressor.afterMinified);
				writtenToJspWritter = true;
			}
		}
		return SKIP_BODY;
	}

	/**
	 * This method handles minification of tags which has empty body-content. This method minifies body-content only if
	 * compress is enabled. When compress is enable and type is javascript, then this method gets minified code from
	 * Compressor for corresponding JS code and writes minified code in JspWriter. In all other cases, the original
	 * body-content is written to JspWriter.
	 */
	@Override
	public int doEndTag() {
		BodyContent bc = getBodyContent();
		JspWriter out = pageContext.getOut();
		type = (String) attrributeMap.get(TYPE);
		src = (String) attrributeMap.get(SRC);

		// If the tag does not have a body then this is probably a tag which links to a source.
		// Fetch the data of all such source files (if they are not external web files), minify them and include
		// them inline.
		if (!("true".equals(pageContext.getRequest().getParameter(SettingsManager.JS_COMPRESSION_DISABLED))
				|| SettingsManager.isCompressionDisabled() || SettingsManager.isJsCompressionDisabled()) && bc == null
				&& src != null) {
			if (type == null || "text/javascript".equals(type) || "application/javascript".equals(type)) {

				// If src is webAddress then we don't need to do any minification.
				// write original bodycontent to Jspwriter.
				String afterMinified = null;
				if (!Compressor.isWebAddress(src)) {
					// src is JS file ,then get minified content using Compressor.
					// check if there are errors in compilation,before writing to buffer.
					String absolutePath = Compressor.getAbsolutePath((HttpServletRequest) pageContext.getRequest(),
							src);
					compressor.minify(absolutePath, getBodyContent(), getJspId(), false, Compressor.ContentType.JS);

					// As we are inlining External referenced JS File, we should remove 'src' attribute.
					attrributeMap.remove(SRC);

					throwExceptionIfErrors(out, Tag.SCRIPT, false);
					afterMinified = compressor.afterMinified;
				}

				writeToJspWriter(out, Tag.SCRIPT, afterMinified);
				writtenToJspWritter = true;
			}
		}
		// If we have not written any-content to JspWriter, write original content to JspWritter.
		if (!writtenToJspWritter) {
			writeBodyToJspWriter(out, Tag.SCRIPT, bc);
		}
		return EVAL_PAGE;
	}

}
