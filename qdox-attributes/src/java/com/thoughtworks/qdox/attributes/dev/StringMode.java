package com.thoughtworks.qdox.attributes.dev;

import com.thoughtworks.qdox.attributes.SimpleAttribute;
import com.thoughtworks.qdox.attributes.impl.SimpleBundle;

/**
 * String mode for the attributes builder, considers every tag as a simple
 * key/value pair with no validation.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class StringMode implements AttributesBuilder.Mode {
	public void processTag(String tag, String text, SimpleBundle bundle) {
		bundle.add(new SimpleAttribute(tag, text));
	}
	public void setTypeResolver(TypeResolver resolver) {
	}
}