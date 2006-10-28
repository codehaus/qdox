package com.thoughtworks.qdox.attributes.impl;

/**
 * A creator for attributes that cannot be serialized.  It is initialized with all the necessary
 * data, and recreates the original attribute on demand.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public interface Creator extends java.io.Serializable {

	Object create() throws Exception;

}
