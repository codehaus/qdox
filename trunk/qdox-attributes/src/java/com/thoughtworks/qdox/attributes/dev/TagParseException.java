package com.thoughtworks.qdox.attributes.dev;

/**
 * Indicates a syntax error in a parsed tag value.
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class TagParseException extends Exception {
	String value;
	int column;

	public TagParseException(String message) {
		super(message);
	}
	
	public String getValue() {return value;}
	public int getColumn() {return column;}
	
	public String getMessage() {
		if (value != null || column > 0) {
			return super.getMessage() + " ("
				+ (value != null ? "in \"" + value + "\"" : "")
				+ (value != null && column != 0 ? " " : "")
				+ (column != 0 ? "at column " + column : "")
				+ ")";
		} else {
			return super.getMessage();
		}
	}

}
