package com.thoughtworks.qdox.xml;

public class XmlHandlerException extends RuntimeException {

    private Throwable cause;

    public XmlHandlerException(String message) {
        super(message);
    }

    public XmlHandlerException(Throwable cause) {
        // super(cause);
        super();
        this.cause = cause;
    }

    public XmlHandlerException(String message, Throwable cause) {
        // super(message, cause);
        super(message);
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }
    
}
