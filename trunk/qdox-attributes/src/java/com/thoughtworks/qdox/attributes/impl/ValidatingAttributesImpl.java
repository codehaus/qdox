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
public class ValidatingAttributesImpl extends AttributesImplBase {
	
	public void checkModifyAttribute() {
		Attributes.getInstance(false).checkModifyAttribute();
	}

	// TODO: look into changing to WeakHashMap, to permit class unloading
	// what about strong classloader reference in AttributesPack?
	private final Map packs = new HashMap();
	
	private synchronized Map getPack(ClassLoader classLoader) {
		if (classLoader == null) classLoader = Thread.currentThread().getContextClassLoader();
		Map pack = (Map) packs.get(classLoader);
		if (pack == null) {
			pack = new HashMap();
			packs.put(classLoader, pack);
		}
		return pack;
	}
	
	private static Class[] getSupertypes(final Class klass) {
		Class[] types = klass.getInterfaces();
		if (klass.getSuperclass() != null) {
			Class[] a = new Class[types.length+1];
			System.arraycopy(types, 0, a, 1, types.length);
			a[0] = klass.getSuperclass();
			types = a;
		}
		return types;
	}
	
	public Bundle get(Package pakage, ClassLoader classLoader) {
		return Attributes.getInstance(false).get(pakage, classLoader);
	}

	public Bundle get(Class klass) {
		Map pack = getPack(klass.getClassLoader());
		synchronized(pack) {
			Bundle bundle = (Bundle) pack.get(klass);
			if (bundle == null) {
				InheritedBundleBuilder builder = new InheritedBundleBuilder(Attributes.getInstance(false).get(klass));
				Class[] types = getSupertypes(klass);
				for (int i=0; i<types.length; i++) builder.addSuper(get(types[i]));
				BundleBase bundleb = builder.getBundle();
				bundleb.setKey(klass.getName());
				pack.put(klass, bundleb);
				bundle = bundleb;
			}
			return bundle;
		}
	}

	public Bundle get(Method method) {
		Map pack = getPack(method.getDeclaringClass().getClassLoader());
		synchronized(pack) {
			Bundle bundle = (Bundle) pack.get(method);
			if (bundle == null) {
				if (Modifier.isPrivate(method.getModifiers())) {
					// private methods don't override (and hence, don't inherit) from superclasses
					bundle = Attributes.getInstance(false).get(method);
				} else {
					InheritedBundleBuilder builder = new InheritedBundleBuilder(Attributes.getInstance(false).get(method));
					Class[] types = getSupertypes(method.getDeclaringClass());
					for (int i=0; i<types.length; i++) {
						try {
							builder.addSuper(get(types[i].getDeclaredMethod(method.getName(), method.getParameterTypes())));
						} catch (NoSuchMethodException e) {
							// do nothing if the method doesn't exist in the supertype
						}
					}
					BundleBase bundleb = builder.getBundle();
					StringBuffer buf = new StringBuffer();
					buf.append(method.getName());
					appendParamTypes(buf, method.getParameterTypes());
					bundleb.setKey(method.getDeclaringClass().getName() + "#" + buf.toString());
					bundle = bundleb;
				}
				pack.put(method, bundle);
			}
			return bundle;
		}
	}

	public Bundle get(Method method, int parameterIndex) {
		// TODO: implement it later, after refactoring
		throw new UnsupportedOperationException();
	}

	public Bundle get(Constructor constructor) {
		return Attributes.getInstance(false).get(constructor);
	}

	public Bundle get(Constructor constructor, int parameterIndex) {
		return Attributes.getInstance(false).get(constructor, parameterIndex);
	}

	public Bundle get(Field field) {
		return Attributes.getInstance(false).get(field);
	}

	public Bundle getPackage(String packageName, ClassLoader classLoader) {
		return Attributes.getInstance(false).getPackage(packageName, classLoader);
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
		return Attributes.getInstance(false).getConstructor(className, constructorSignature, classLoader);
	}

	public Bundle getConstructorParameter(String className, String constructorSignature, int paramIndex, ClassLoader classLoader) {
		return Attributes.getInstance(false).getConstructorParameter(className, constructorSignature, paramIndex, classLoader);
	}

	public Bundle getField(String className, String fieldName, ClassLoader classLoader) {
		return Attributes.getInstance(false).getField(className, fieldName, classLoader);
	}

}
