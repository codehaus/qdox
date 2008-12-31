package com.thoughtworks.qdox.attributes.impl;

import java.util.*;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.*;
import com.thoughtworks.qdox.attributes.Bundle;
import com.thoughtworks.qdox.attributes.InvalidBundleException;

/**
 * Builds inherited bundles, validating the attribute assignments in the process.
 * Doesn't throw exceptions, but rather collects errors (if any) and returns an error
 * bundle at the end instead of an inherited bundle.
 * <p>
 * Not thread-safe.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class InheritedBundleBuilder {
	
	private ErrorBundle errorBundle;
	private InheritedBundle inheritedBundle;

	/**
	 * Make a new builder to start constructing a new bundle.
	 * @param base the base bundle of the target element
	 */
	public InheritedBundleBuilder(Bundle base) {
		errorBundle = new ErrorBundle();
		try {
			inheritedBundle = new InheritedBundle(base);
		} catch (InvalidBundleException e) {
			errorBundle.addError(e);
			inheritedBundle = null;
		}
	}
	
	/**
	 * Add a parent (super) bundle and inherit the appropriate attributes into the
	 * bundle being constructed.
	 * @param superBundle the super bundle to inherit attributes from
	 */
	public void addSuper(Bundle superBundle) {
		if (inheritedBundle == null) return;
		for (Iterator it = superBundle.getProvenanceMap().entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry) it.next();
			Object attribute = entry.getKey();
			
			// if attribute was already inherited, don't inherit it again
			if (inheritedBundle.getProvenanceMap().get(attribute) != null) continue;
			
			String declaringClassName = (String) entry.getValue();
			AttributeUsageAttribute usage = AttributeUsageAttribute.of(attribute);
			
			if (usage.getInherit()) {
				boolean shouldInherit = true;
				if (!usage.getAllowMultiple() || usage.getFinal()) {
					// assume target and multiplicity have already been checked, only worry about inheritance
					String usageDefiningClassName = (String) Attributes.getInstance().get(attribute.getClass()).getProvenanceMap().get(usage);
					try {
						// if no existing attribute with matching class, inheriting is always OK
						if (inheritedBundle.iterator(attribute.getClass().getClassLoader().loadClass(usageDefiningClassName)).hasNext()) {
							if (usage.getFinal()) {
								errorBundle.addError("attempt to redefine final attribute " + attribute.getClass() + " originally assigned in " + declaringClassName);
								shouldInherit = false;
							} else if (!usage.getAllowMultiple()) {
								// already have a more specific assignment, ignore inherited one
								shouldInherit = false;
							}
						}
					} catch (ClassNotFoundException e) {
						throw new ChainedRuntimeException("unable to load attribute class ancestor type", e);
					}
				}
				if (shouldInherit) inheritedBundle.add(attribute, declaringClassName);
			}
		}
	}

	/**
	 * Return the constructed bundle.  If no errors occurred, this will be the inherited
	 * bundle, otherwise an error bundle.
	 * @return the constructed bundle
	 */
	public BundleBase getBundle() {
		if (errorBundle.size() > 0) {
			return errorBundle;
		} else {
			if (inheritedBundle == null) throw new RuntimeException("assertion failure: no bundle");
			return inheritedBundle;
		}
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
			InheritedBundleBuilder bb = new InheritedBundleBuilder(base);
			assertEquals(0, bb.errorBundle.size());
			assertTrue(Arrays.equals(base.toArray(), bb.inheritedBundle.toArray()));
			assertSame(bb.inheritedBundle, bb.getBundle());
		}
		public void testBaseConstructorError() {
			InheritedBundleBuilder bb = new InheritedBundleBuilder(new ErrorBundle());
			assertEquals(1, bb.errorBundle.size());
			assertNull(bb.inheritedBundle);
			assertSame(bb.errorBundle, bb.getBundle());
		}
	}
}
