package com.thoughtworks.qdox.attributes.impl;

import java.lang.reflect.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.qdox.attributes.Attributes;
import com.thoughtworks.qdox.attributes.Bundle;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class AttributesImpl extends Attributes {
	
	public static final String COALESCED_FILENAME = "META-INF/all.attribs";
	public static final String FILENAME_SUFFIX = ".attribs";

	// TODO: look into changing to WeakHashMap, to permit class unloading
	// what about strong classloader reference in AttributesPack?
	private final Map packs = new HashMap();
	
	public Bundle get(Class klass) {
		return getClass(klass.getName(), klass.getClassLoader());
	}

	public Bundle get(Constructor constructor) {
		StringBuffer buf = new StringBuffer();
		appendParamTypes(buf, constructor.getParameterTypes());
		return getConstructor(constructor.getDeclaringClass().getName(), buf.toString(), constructor.getDeclaringClass().getClassLoader());
	}

	public Bundle get(Method method) {
		StringBuffer buf = new StringBuffer();
		buf.append(method.getName());
		appendParamTypes(buf, method.getParameterTypes());
		return getMethod(method.getDeclaringClass().getName(), buf.toString(), method.getDeclaringClass().getClassLoader());
	}
	
	public static void appendParamTypes(StringBuffer buf, Class[] paramTypes) {
		buf.append('(');
		for (int i = 0; i < paramTypes.length; i++) {
			if (i>0) buf.append(',');
			buf.append(paramTypes[i].getName());
		}
		buf.append(')');
	}

	public Bundle get(Field field) {
		return getField(field.getDeclaringClass().getName(), field.getName(), field.getDeclaringClass().getClassLoader());
	}

	public Bundle getClass(String className, ClassLoader classLoader) {
		return getPack(classLoader, className).get(className);
	}

	public Bundle getConstructor(String className, String constructorSignature, ClassLoader classLoader) {
		if (constructorSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in constructor signature: " + constructorSignature);
		return getPack(classLoader, className).get(className + "#" + constructorSignature);
	}

	public Bundle getMethod(String className, String methodSignature, ClassLoader classLoader) {
		if (methodSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in method signature: " + methodSignature);
		return getPack(classLoader, className).get(className + "#" + methodSignature);
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
		private AttributesImpl a;
		public void setUp() {
			a = new AttributesImpl();
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
