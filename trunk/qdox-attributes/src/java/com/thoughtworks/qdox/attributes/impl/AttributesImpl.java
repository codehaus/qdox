package com.thoughtworks.qdox.attributes.impl;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.Attributes;
import com.thoughtworks.qdox.attributes.Bundle;

/**
 * A base class for attributes implementations.  Redirects queries on reflected elements
 * to the appropriate string-based equivalents.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class AttributesImpl extends Attributes {
	
	public static final String AGGREGATED_FILENAME = "META-INF/all.attribs";

	protected boolean canModifyAttributes = false;

	protected final Map packs = new HashMap();

	public void checkModifyAttribute() {
		if (!canModifyAttributes) super.checkModifyAttribute();
	}

	public Bundle get(Package pakage, ClassLoader classLoader) {
		return getPackage(pakage.getName(), classLoader);
	}

	public Bundle get(Class klass) {
		return getClass(klass.getName(), klass.getClassLoader());
	}

	public Bundle get(Constructor constructor) {
		StringBuffer buf = new StringBuffer();
		appendParamTypes(buf, constructor.getParameterTypes());
		return getConstructor(constructor.getDeclaringClass().getName(), buf.toString(), constructor.getDeclaringClass().getClassLoader());
	}

	public Bundle get(Constructor constructor, int parameterIndex) {
		StringBuffer buf = new StringBuffer();
		appendParamTypes(buf, constructor.getParameterTypes());
		return getConstructorParameter(constructor.getDeclaringClass().getName(), buf.toString(), parameterIndex, constructor.getDeclaringClass().getClassLoader());
	}

	public Bundle get(Method method) {
		StringBuffer buf = new StringBuffer();
		buf.append(method.getName());
		appendParamTypes(buf, method.getParameterTypes());
		return getMethod(method.getDeclaringClass().getName(), buf.toString(), method.getDeclaringClass().getClassLoader());
	}
	
	public Bundle get(Method method, int parameterIndex) {
		StringBuffer buf = new StringBuffer();
		buf.append(method.getName());
		appendParamTypes(buf, method.getParameterTypes());
		return getMethodParameter(method.getDeclaringClass().getName(), buf.toString(), parameterIndex, method.getDeclaringClass().getClassLoader());
	}

	public Bundle get(Field field) {
		return getField(field.getDeclaringClass().getName(), field.getName(), field.getDeclaringClass().getClassLoader());
	}

	public static void appendParamTypes(StringBuffer buf, Class[] paramTypes) {
		buf.append('(');
		for (int i = 0; i < paramTypes.length; i++) {
			if (i>0) buf.append(',');
			buf.append(paramTypes[i].getName());
		}
		buf.append(')');
	}

	public Bundle getPackage(String packageName, ClassLoader classLoader) {
		String extendedName = packageName + ".package";
		return getPack(classLoader).get(extendedName, "");
	}

	public Bundle getClass(String className, ClassLoader classLoader) {
		final String bundleName = className + ";inherited";
		ReadAttributesPack pack = getPack(classLoader);
		Bundle bundle = pack.getIfExists(bundleName);
		if (bundle == null) {
			InheritedBundleBuilder builder = new InheritedBundleBuilder(pack.get(className, ""));
			String[] types = pack.getSupertypes(className);
			for (int i=0; i<types.length; i++) builder.addSuper(getClass(types[i], classLoader));
			BundleBase bundleb = builder.getBundle();
			pack.put(bundleName, bundleb);
			bundle = bundleb;
		}
		return bundle;
	}

	public Bundle getConstructor(String className, String constructorSignature, ClassLoader classLoader) {
		if (constructorSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in constructor signature: " + constructorSignature);
		return getPack(classLoader).get(className, "#" + constructorSignature);
	}

	public Bundle getConstructorParameter(String className, String constructorSignature, int paramIndex, ClassLoader classLoader) {
		if (constructorSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in constructor signature: " + constructorSignature);
		if (paramIndex < 1) throw new IllegalArgumentException("parameter index below 1: " + paramIndex);
		return getPack(classLoader).get(className, "#" + constructorSignature + "!" + paramIndex);
	}

	public Bundle getMethod(String className, String methodSignature, ClassLoader classLoader) {
		if (methodSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in method signature: " + methodSignature);
		return getMethodHelper(className, methodSignature, true, classLoader);
	}
		
	protected Bundle getMethodHelper(String className, String methodSignature, boolean considerPrivate, ClassLoader classLoader) {
		ReadAttributesPack pack = getPack(classLoader);
		final String fullSignature = className + "#" + methodSignature;
		Bundle bundle;
		if (considerPrivate) {
			bundle = pack.getIfExists(fullSignature + ";private");
			if (bundle != null) return bundle;
		} 
		final String bundleName = fullSignature + ";inherited";
		bundle = pack.getIfExists(bundleName);
		if (bundle == null) {
			InheritedBundleBuilder builder = new InheritedBundleBuilder(pack.get(className, "#" + methodSignature));
			String[] types = pack.getSupertypes(className);
			for (int i=0; i<types.length; i++) {
				// avoid looking for private entries when walking inheritance chain
				builder.addSuper(getMethodHelper(types[i], methodSignature, false, classLoader));
			}
			BundleBase bundleb = builder.getBundle();
			pack.put(bundleName, bundleb);
			bundle = bundleb;
		}
		return bundle;
	}

	public Bundle getMethodParameter(String className, String methodSignature, int paramIndex, ClassLoader classLoader) {
		if (methodSignature.indexOf(' ') >= 0) throw new IllegalArgumentException("spaces in method signature: " + methodSignature);
		if (paramIndex < 0) throw new IllegalArgumentException("parameter index below 0: " + paramIndex);
		return getMethodHelper(className, methodSignature + "!" + paramIndex, true, classLoader);
	}

	public Bundle getField(String className, String fieldName, ClassLoader classLoader) {
		return getPack(classLoader).get(className, "#" + fieldName);
	}

	protected synchronized ReadAttributesPack getPack(ClassLoader classLoader) {
		if (classLoader == null) classLoader = Thread.currentThread().getContextClassLoader();
		ReadAttributesPack pack = (ReadAttributesPack) packs.get(classLoader);
		if (pack == null) {
			pack = new ReadAttributesPack(classLoader, true);
			pack.mergeAggregateFile(AGGREGATED_FILENAME);
			packs.put(classLoader, pack);
		}
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
