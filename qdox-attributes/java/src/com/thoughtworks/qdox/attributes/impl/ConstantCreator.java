package com.thoughtworks.qdox.attributes.impl;

import com.thoughtworks.qdox.attributes.Creator;

/**
 * Retrieves the value of a constant field whose value cannot be serialized.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ConstantCreator implements Creator {
	
	private final Class klass;
	private final String fieldName;
	
	public ConstantCreator(Class klass, String fieldName) {
		this.klass = klass;
		this.fieldName = fieldName;
	}

	public Object create() throws Exception {
		return klass.getField(fieldName).get(null);
	}

}
