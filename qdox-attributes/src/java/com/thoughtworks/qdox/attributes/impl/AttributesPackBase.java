package com.thoughtworks.qdox.attributes.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
		assert !elements.containsKey(key);
		bundle.setKey(key);
		elements.put(key, bundle);
	}

	public synchronized int size() {
		return elements.size();
	}

	protected static final Logger log = Logger.getLogger(ReadAttributesPack.class.getName());

}
