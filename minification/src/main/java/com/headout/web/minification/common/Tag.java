package com.headout.web.minification.common;

public enum Tag {
	SCRIPT("script", false, true),
	LINK("link", true, false),
	STYLE("style", false, true);

	private final String name;
	private final boolean selfClosingTagSupported;
	private final boolean endTagSupported;

	/**
	 *
	 * @param name name of tag
	 * @param emptyTagSupported boolean value, which tells noEndTag is supported or not.
	 */
	Tag(String name, boolean selfClosingTagSupported, boolean endTagSupported) {
		this.name = name;
		this.selfClosingTagSupported = selfClosingTagSupported;
		this.endTagSupported = endTagSupported;
	}

	public String getName() {
		return name;
	}

	public boolean isEndTagSupported() {
		return endTagSupported;
	}

	public boolean isSelfClosingTagSupported() {
		return selfClosingTagSupported;
	}
}
