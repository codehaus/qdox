package com.thoughtworks.qdox.attributes.impl;

import java.util.*;

import com.thoughtworks.qdox.attributes.*;

/**
 * A simple ordered bundle of attributes.  This implementation keeps the attributes
 * in an object array, and uses linear search to find entries.
 * Normally, there's so few attributes in a bundle that the running time of a linear
 * search should be comparable to more efficient methods (e.g. hash map).  The
 * array also allows duplicate keys, keeps the order of all the attributes, and should
 * save memory.
 */
public class SimpleBundle extends SearchableBundleBase implements Bundle {
	
	transient private Map provenanceMap, constProvenanceMap;
	transient private boolean classNameExtracted;
	transient private String definingClassName;
	
	synchronized String getDefiningClassName() {
		if (!classNameExtracted) {
			int hashIndex = getKey().indexOf('#');
			definingClassName = hashIndex == -1 ? getKey() : getKey().substring(0, hashIndex);
			if (definingClassName.endsWith(".*")) definingClassName = null;
			classNameExtracted = true;
		}
		return definingClassName;
	}
	
	public synchronized Map getProvenanceMap() {
		if (provenanceMap == null) {
			provenanceMap = new LinkedHashMap();
			for (int i=0; i<size; i++) provenanceMap.put(objects[i], getDefiningClassName());
			constProvenanceMap = Collections.unmodifiableMap(provenanceMap);
		}
		return constProvenanceMap;
	}

}