package com.thoughtworks.qdox.attributes.impl;

/**
 * Indicates a syntax error in a parsed tag value.
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class TagParseException extends Exception {

	public TagParseException(String message) {
		super(message);
	}

}
