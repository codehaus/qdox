package com.thoughtworks.qdox.attributes.impl;

import java.util.*;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.Bundle;
import com.thoughtworks.qdox.attributes.InvalidBundleException;

/**
 * A bundle that holds all attributes that were inherited by an element.  It is never
 * persisted.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class InheritedBundle extends SearchableBundleBase {
	
	private final Map provenance;
	private final Map provenanceConst;

	protected InheritedBundle(Bundle base) {
		for (Iterator it = base.iterator(); it.hasNext();) add(it.next());
		provenance = new HashMap(base.getProvenanceMap());
		this.provenanceConst = Collections.unmodifiableMap(provenance);
	}
	
	protected void add(Object attribute, String declaringClassName) {
		add(attribute);
		provenance.put(attribute, declaringClassName);
	}

	public Map getProvenanceMap() {
		return provenanceConst;
	}

	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class Test extends TestCase {
		private SimpleBundle makeSampleBundle() {
			SimpleBundle base = new SimpleBundle();
			base.add("a");
			base.add("b");
			base.setKey("base");
			return base;
		}
		public void testBaseConstructor() {
			SimpleBundle base = makeSampleBundle();
			InheritedBundle bundle = new InheritedBundle(base);
			assertTrue(Arrays.equals(base.toArray(), bundle.toArray()));
			assertEquals("base", bundle.getProvenanceMap().get("a"));
		}
		public void testBaseConstructorError() {
			try {
				new InheritedBundle(new ErrorBundle());
				fail();
			} catch (InvalidBundleException e) {
			}
		}
		public void testAddAttribute() {
			InheritedBundle bundle = new InheritedBundle(makeSampleBundle());
			bundle.add("x", "inherited");
			assertEquals("base", bundle.getProvenanceMap().get("a"));
			assertEquals("inherited", bundle.getProvenanceMap().get("x"));			
		}
	}
}
