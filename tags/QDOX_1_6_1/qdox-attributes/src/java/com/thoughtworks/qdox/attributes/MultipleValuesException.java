package com.thoughtworks.qdox.attributes;

/**
 * This exception signals that there are multiple attribute values where only one was expected.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class MultipleValuesException extends ChainedRuntimeException {

	public MultipleValuesException() {
		super();
	}

	public MultipleValuesException(String message) {
		super(message);
	}

	public MultipleValuesException(Throwable cause) {
		super(cause);
	}

	public MultipleValuesException(String message, Throwable cause) {
		super(message, cause);
	}

}
