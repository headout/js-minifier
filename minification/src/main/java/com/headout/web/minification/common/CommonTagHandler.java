package com.headout.web.minification.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.JspIdConsumer;

import com.headout.web.minification.exception.CommonRuntimeException;
import com.headout.web.minification.exception.JSCompileException;
import com.headout.web.minification.exception.util.ExceptionUtil;
import com.headout.web.minification.settings.SettingsManager;

public class CommonTagHandler extends BodyTagSupport implements JspIdConsumer, DynamicAttributes {

	private static final long serialVersionUID = -1022606939089990192L;
	protected final HashMap<String, Object> attrributeMap = new HashMap<String, Object>();
	private String jspId;
	protected Compressor compressor;

	public CommonTagHandler() {
		this.compressor = new Compressor();
	}

	/**
	 * This function generates output string format according to tag and bodyContent.
	 *
	 * @param tag enum specifies for which tag we are creating output string format
	 * @param bodyContent content to written inside tag
	 * @return output String is return
	 */
	protected String generateOutputString(Tag tag, String bodyContent) {

		StringBuilder output = new StringBuilder();
		output.append("<").append(tag.getName()).append(" ");
		// Add all relevent attributes in tag which were present in custom tag.
		// Attributes like :- src ,rel ,compress if present in custom tag, they are removed from map before
		// calling to this function.
		// Attibute 'type' is removed for CSS cutom tag.
		for (Entry<String, Object> entry : attrributeMap.entrySet()) {
			output.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\" ");
		}

		if (!tag.isEndTagSupported() && !tag.isSelfClosingTagSupported()) {
			new RuntimeException("Atleast one of the (emptyTag or nonEmptyTag) should be supported.");
		}

		if (bodyContent != null && !tag.isEndTagSupported()) {
			new RuntimeException("For non-empty body ,nonEmptyrag should be supported");
		}

		// if bodycontent is not null, there should be endtag.
		if (bodyContent != null || !tag.isSelfClosingTagSupported()) {
			output.append(">");
			if (bodyContent != null)
				output.append(bodyContent);
			output.append("</").append(tag.getName()).append(">");
		} else
			output.append("/>");

		return output.toString();
	}

	public String getJspId() {
		return jspId;
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
		attrributeMap.put(localName, value);
	}

	@Override
	public void setJspId(String jspId) {
		this.jspId = jspId;
	}

	/**
	 * If errors are there in Js compilation , show alert box and send errorMessage is JSP page as stacktrace (for
	 * non-production environment only),and throw runtime exception.
	 *
	 * @param out JspWriter ,where we have to write alert message if errors occurred.
	 * @param tag enum which tells what tag it is
	 * @param isInline compress inline or not
	 */
	protected void throwExceptionIfErrors(JspWriter out, Tag tag, boolean isInline) {
		if (compressor.errors != null && compressor.errors.length != 0) {
			JSCompileException ex = new JSCompileException(compressor.errors);
			if (SettingsManager.isOutputStacktrace()) {
				String alertMessage = "window.alert(\"An error has occurred in JS compilation.\")";
				String errormessage = "\n<div class=\"server-exception\" >\n<code>\n"
						+ ExceptionUtil.getStackTraceString(ex)
						+ "\n<code>\n</div>\n";
				if (isInline) {
					alertMessage += "</script>";
					errormessage += "</script>";
				}
				writeToJspWriter(out, tag, alertMessage);
				writeToJspWriter(out, tag, errormessage);
			}
			throw ex;
		}
	}

	/**
	 * The function is used to write body content
	 * @param out the JspWriter to be used
	 * @param tag enum which tells which tag it is
     * @param bc the body content to be written
     */
	protected void writeBodyToJspWriter(JspWriter out, Tag tag, BodyContent bc) {
		writeToJspWriter(out, tag, bc == null ? null : bc.getString());
	}

	/**
	 *
	 * @param out jspwriter.
	 * @param content string which is the content to be written in jspwriter.
	 */
	private void writer(JspWriter out, String content) {
		try {
			out.print(content);
		} catch (IOException e) {
			throw new CommonRuntimeException(e, "An Error has occurred while writing in JspWriter");
		}
	}

	/**
	 *
	 * @param out jspwriter.
	 * @param tag enum which tells which tag it is.
	 * @param bodyContent content to be written inside tag.
	 */
	protected void writeToJspWriter(JspWriter out, Tag tag, String bodyContent) {
		String output = null;
		if (tag != null) {
			output = generateOutputString(tag, bodyContent);
		} else
			output = bodyContent;
		writer(out, output);
	}

}
