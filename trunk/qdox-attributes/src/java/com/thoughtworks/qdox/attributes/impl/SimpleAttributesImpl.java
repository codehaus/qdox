package com.thoughtworks.qdox.attributes.impl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.Bundle;

/**
 * A simple non-validating implementation of the Attributes toolkit facade.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class SimpleAttributesImpl extends AttributesImplBase {
	public static final String COALESCED_FILENAME = "META-INF/all.attribs";
	public static final String FILENAME_SUFFIX = ".attribs";

	boolean canModifyAttributes = false;

	private final Map packs = new HashMap();

	public void checkModifyAttribute() {
		if (!canModifyAttributes) super.checkModifyAttribute();
	}

	public Bundle getPackage(String packageName, ClassLoader classLoader) {
		String extendedName = packageName + ".*";
		return getPack(classLoader, extendedName).get(extendedName);
	}

	public Bundle getClass(String className, ClassLoader classLoader) {
		return getPack(classLoader, className).get(className);
	}

	public Bundle getConstructor(String className, String constructorSignature, ClassLoader classLoader) {
		if (constructorSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in constructor signature: " + constructorSignature);
		return getPack(classLoader, className).get(className + "#" + constructorSignature);
	}

	public Bundle getConstructorParameter(String className, String constructorSignature, int paramIndex, ClassLoader classLoader) {
		if (constructorSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in constructor signature: " + constructorSignature);
		if (paramIndex < 1) throw new IllegalArgumentException("parameter index below 1: " + paramIndex);
		return getPack(classLoader, className).get(className + "#" + constructorSignature + "!" + paramIndex);
	}

	public Bundle getMethod(String className, String methodSignature, ClassLoader classLoader) {
		if (methodSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in method signature: " + methodSignature);
		return getPack(classLoader, className).get(className + "#" + methodSignature);
	}

	public Bundle getMethodParameter(String className, String methodSignature, int paramIndex, ClassLoader classLoader) {
		if (methodSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in method signature: " + methodSignature);
		if (paramIndex < 0) throw new IllegalArgumentException("parameter index below 0: " + paramIndex);
		return getPack(classLoader, className).get(className + "#" + methodSignature + "!" + paramIndex);
	}

	public Bundle getField(String className, String fieldName, ClassLoader classLoader) {
		return getPack(classLoader, className).get(className + "#" + fieldName);
	}

	private synchronized AttributesPack getPack(ClassLoader classLoader, String className) {
		if (className == null || className.length() == 0) throw new IllegalArgumentException("no class name");
		if (classLoader == null) classLoader = Thread.currentThread().getContextClassLoader();
		AttributesPack pack = (AttributesPack) packs.get(classLoader);
		if (pack == null) {
			pack = new AttributesPack(classLoader, true);
			pack.merge(COALESCED_FILENAME);
			packs.put(classLoader, pack);
		}
		String attribsFilename = className.replace('.', '/');
		int k = attribsFilename.indexOf('$');
		if (k != -1) attribsFilename = attribsFilename.substring(0, k);
		attribsFilename += FILENAME_SUFFIX;
		pack.merge(attribsFilename);
		return pack;
	}

	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class Test extends TestCase {
		private AttributesImplBase a;
		public void setUp() {
			a = new SimpleAttributesImpl();
		}
		public void tearDown() {
			a = null;
		}
		public void testGetClassFullName() {
			a.getClass("com.ideanest.attributes.AttributesImpl", null);
		}
		public void testGetClassDefaultPackage() {
			a.getClass("DummyClass", null);
		}
		public void testGetFieldFullName() {
			a.getField("com.ideanest.attributes.AttributesImpl", "packs", null);
		}
		public void testGetFieldDefaultPackage() {
			a.getField("DummyClass", "field", null);
		}
		public void testGetMethodFullName() {
			a.getMethod("com.ideanest.attributes.AttributesImpl", "get(Field)", null);
		}
		public void testGetMethodFullNameNoParams() {
			a.getMethod("com.ideanest.attributes.Attributes", "getInstance()", null);
		}
		public void testGetMethodFullNameMultipleParams() {
			a.getMethod("com.ideanest.attributes.Attributes", "add(int,int)", null);
		}
		public void testGetMethodWithSpaces() {
			try {
				a.getMethod("com.ideanest.attributes.Attributes", "add(int, int)", null);
				fail();
			} catch (IllegalArgumentException e) {
			}
		}
		public void testGetMethodDefaultPackage() {
			a.getMethod("DummyClass", "dummyMethod()", null);
		}
	}

}
