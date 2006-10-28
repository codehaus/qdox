package com.thoughtworks.qdox.attributes.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base for attributes packs.  Basically a synchronized hash map of elements.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public abstract class AttributesPackBase {

	protected final Map elements;

	public AttributesPackBase() {
		elements = new HashMap();
	}

	public AttributesPackBase(AttributesPackBase original) {
		elements = new HashMap(original.elements);
	}

	public synchronized void put(String key, BundleBase bundle) {
		if (elements.containsKey(key)) throw new RuntimeException("assertion failure: key already present");
		bundle.setKey(key);
		elements.put(key, bundle);
	}

	public synchronized int size() {
		return elements.size();
	}

}
