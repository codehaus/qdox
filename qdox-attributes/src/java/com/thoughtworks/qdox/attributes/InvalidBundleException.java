package com.thoughtworks.qdox.attributes;

/**
 * Signals that the desired bundle is not valid, due to a compile-time or runtime validation error.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class InvalidBundleException extends RuntimeException {

	public InvalidBundleException(String message) {
		super(message);
	}
}
