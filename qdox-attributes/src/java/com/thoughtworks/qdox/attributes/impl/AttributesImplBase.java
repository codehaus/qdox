package com.thoughtworks.qdox.attributes.impl;

import java.lang.reflect.*;

import com.thoughtworks.qdox.attributes.Attributes;
import com.thoughtworks.qdox.attributes.Bundle;

/**
 * A base class for attributes implementations.  Redirects queries on reflected elements
 * to the appropriate string-based equivalents.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public abstract class AttributesImplBase extends Attributes {
	
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

	public abstract Bundle getPackage(String packageName, ClassLoader classLoader);
	public abstract Bundle getClass(String className, ClassLoader classLoader);
	public abstract Bundle getConstructor(String className, String constructorSignature, ClassLoader classLoader);
	public abstract Bundle getConstructorParameter(String className, String constructorSignature, int paramIndex, ClassLoader classLoader);
	public abstract Bundle getMethod(String className, String methodSignature, ClassLoader classLoader);
	public abstract Bundle getMethodParameter(String className, String methodSignature, int paramIndex, ClassLoader classLoader);
	public abstract Bundle getField(String className, String fieldName, ClassLoader classLoader);

}
