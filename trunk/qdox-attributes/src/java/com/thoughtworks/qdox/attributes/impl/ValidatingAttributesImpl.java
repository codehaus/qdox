package com.thoughtworks.qdox.attributes.impl;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.qdox.attributes.Attributes;
import com.thoughtworks.qdox.attributes.Bundle;

/**
 * A validating implementation of the Attributes toolkit facade.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ValidatingAttributesImpl extends Attributes {
	
	public void checkModifyAttribute() {
		Attributes.getInstance(false).checkModifyAttribute();
	}

	// TODO: look into changing to WeakHashMap, to permit class unloading
	// what about strong classloader reference in AttributesPack?
	private final Map packs = new HashMap();
	
	public Bundle get(Package pakage, ClassLoader classLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle get(Class klass) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle get(Method method) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle get(Method method, int parameterIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle get(Constructor constructor) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle get(Constructor constructor, int parameterIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle get(Field field) {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle getPackage(String packageName, ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

	public Bundle getClass(String className, ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

	public Bundle getMethod(String className, String methodSignature, ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

	public Bundle getMethodParameter(String className, String methodSignature, int paramIndex, ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

	public Bundle getConstructor(String className, String constructorSignature, ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

	public Bundle getConstructorParameter(String className, String constructorSignature, int paramIndex, ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

	public Bundle getField(String className, String fieldName, ClassLoader classLoader) {
		throw new UnsupportedOperationException();
	}

}
