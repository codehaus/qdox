package com.thoughtworks.qdox.attributes.dev;

import com.thoughtworks.qdox.attributes.SimpleAttribute;
import com.thoughtworks.qdox.attributes.impl.SimpleBundle;


public class StringMode implements AttributesBuilder.Mode {
	public void processTag(String tag, String text, SimpleBundle bundle) {
		bundle.add(new SimpleAttribute(tag, text));
	}
	public void setTypeResolver(TypeResolver resolver) {
	}
}