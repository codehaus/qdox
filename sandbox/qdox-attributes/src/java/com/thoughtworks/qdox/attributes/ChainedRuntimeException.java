package com.thoughtworks.qdox.attributes;

/**
 * A runtime exception that wraps another exception, which is the real cause of the
 * problem.  This is a backwards-compatible way of achieving the same effect as the
 * Throwable cause mechanism in JDK 1.4.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ChainedRuntimeException extends RuntimeException {
	private final Throwable cause;

	public ChainedRuntimeException() {
		this(null, null);
	}

	public ChainedRuntimeException(String s) {
		this(s, null);
	}

	public ChainedRuntimeException(Throwable cause) {
		super();
		this.cause = cause;
	}

	public ChainedRuntimeException(String s, Throwable cause) {
		super(s);
		this.cause = cause;
	}
	
	public Throwable getCause() {
		return cause;
	}
	
	// TODO: change printing methods to include cause?
}
