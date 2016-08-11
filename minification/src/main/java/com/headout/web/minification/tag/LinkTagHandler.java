package com.headout.web.minification.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.headout.web.minification.common.CommonTagHandler;
import com.headout.web.minification.common.Compressor;
import com.headout.web.minification.common.Tag;
import com.headout.web.minification.settings.SettingsManager;

/**
 * This class is used for inlining external CSS code(already minified css code) which is referenced through link using
 * JSP customtag
 *
 * @author harshal
 *
 */
public class LinkTagHandler extends CommonTagHandler implements DynamicAttributes {
	// TODO: We need to have a Global level Exception Handler which will report out in the UI if an error occurred.

	private static final long serialVersionUID = -7090071750120401939L;
	private static final String REL = "rel";
	private static final String TYPE = "type";
	private static final String HREF = "href";
	private String type;
	private String rel;
	private boolean writtenToJspWriter = false;

	/**
	 * This method inlines already minified external CSS code using custom JSP tag.
	 *
	 */
	@Override
	public int doEndTag() {
		JspWriter out = pageContext.getOut();
		if (!("true".equals(pageContext.getRequest().getParameter(SettingsManager.CSS_INLINE_INSERTION_DISABLED))
				|| SettingsManager.isCssInlineInsertionDisabled() || SettingsManager.isCompressionDisabled())) {

			rel = (String) attrributeMap.get(REL);
			type = (String) attrributeMap.get(TYPE);

			// If link is of css file reference then proceed for getting external CSS code.
			if ("text/css".equals(type) || ("stylesheet".equals(rel) && type == null)) {
				if (attrributeMap.containsKey(HREF)) {
					String cssFilePath = (String) attrributeMap.get(HREF);
					String absolutePath = Compressor.getAbsolutePath((HttpServletRequest) pageContext.getRequest(),
							cssFilePath);
					String afterMinified = null;
					if (!Compressor.isWebAddress(cssFilePath)) {
						compressor.minify(absolutePath, getBodyContent(), getJspId(), false,
								Compressor.ContentType.CSS);
						afterMinified = compressor.afterMinified;

						// Before writing to JspWriter remove attributes which are not required to be passed.
						attrributeMap.remove(HREF);
						if (rel != null)
							attrributeMap.remove(REL);
						if (type != null)
							attrributeMap.remove(TYPE);
						// write to JspWriter
						writeToJspWriter(out, Tag.STYLE, afterMinified);
						writtenToJspWriter = true;
					}

				}
			}

		}
		if (!writtenToJspWriter) {
			writeToJspWriter(out, Tag.LINK, null);
		}
		return EVAL_PAGE;
	}
}
