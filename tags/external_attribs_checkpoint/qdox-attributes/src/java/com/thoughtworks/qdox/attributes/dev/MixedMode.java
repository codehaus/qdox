package com.thoughtworks.qdox.attributes.dev;

import com.thoughtworks.qdox.attributes.SimpleAttribute;
import com.thoughtworks.qdox.attributes.impl.SimpleBundle;


public class MixedMode extends ObjectMode {
	protected void tagClassNotFound(String tag, String text, SimpleBundle bundle) {
		bundle.add(new SimpleAttribute(tag, text));
	}
}