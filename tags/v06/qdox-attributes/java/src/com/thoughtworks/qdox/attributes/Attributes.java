package com.thoughtworks.qdox.attributes;

import java.lang.reflect.*;

/**
 * The singleton access point to attributes set on various program elements.
 * <p>
 * Note that there are two matching sets of accessor methods: one takes reflected objects,
 * the other strings.  The two sets are roughly equivalent, however it is best to use the reflected
 * versions of the accessors whenever possible, since it is impossible to pass an illegal
 * argument to them.  The string set is provided in case it is necessary to retrieve attributes
 * for elements without forcing the containing class to be loaded.  Accessors from this set will
 * not check whether the specified element actually exists, since that would require loading it.
 * They may also be unable to resolve attribute inheritance.
 * <p>
 * All implementations must be thread-safe.
 *
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */

public abstract class Attributes {
	public static final String IMPL_CLASS_NAME_PROPKEY = "com.ideanest.attributes.implementation";
	public static final String DEFAULT_IMPL_CLASS_NAME = "com.thoughtworks.qdox.attributes.impl.AttributesImpl";
	
	private static Attributes instance;
	/**
	 * Get the singleton instance of the <code>Attributes</code> class.  The first time this
	 * method is called, the class specified by the value of the system property keyed with
	 * {@link #IMPL_CLASS_NAME_PROPKEY} will be instantiated.  If not such property is
	 * defined, the {@link #DEFAULT_IMPL_CLASS_NAME default class} will be instantiated. 
	 * @return the singleton instance of <code>Attributes</code>
	 * @throws InstantiationException if unable to instantiate the implementation class
	 * @throws IllegalAccessException if unable to access the implementation class
	 * @throws ClassNotFoundException if unable to find the implementation class
	 */
	public synchronized static Attributes getInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (instance == null) {
			String implClassName = System.getProperty(IMPL_CLASS_NAME_PROPKEY);
			if (implClassName == null) implClassName = DEFAULT_IMPL_CLASS_NAME;
			instance = (Attributes) Thread.currentThread().getContextClassLoader().loadClass(implClassName).newInstance();
		}
		return instance;
	}
	
	/**
	 * Get the attributes bundle for the given class.
	 * @param klass the class for which attributes are needed
	 * @return the attributes for the desired class, possibly an empty bundle if none were found
	 */
	public abstract Bundle get(Class klass);
	
	/**
	 * Get the attributes bundle for the given method.
	 * @param method the method for which attributes are needed
	 * @return the attributes for the desired method, possibly an empty bundle if none were found
	 */
	public abstract Bundle get(Method method);

	/**
	 * Get the attributes bundle for the given constructor.
	 * @param constructor the constructor for which attributes are needed
	 * @return the attributes for the desired constructor, possibly an empty bundle if none were found
	 */
	public abstract Bundle get(Constructor constructor);

	/**
	 * Get the attributes bundle for the given field.
	 * @param field the field for which attributes are needed
	 * @return the attributes for the desired field, possibly an empty bundle if none were found
	 */
	public abstract Bundle get(Field field);
	
	
	/**
	 * Get the bundle of attributes for the given class.  The class name must be fully qualified
	 * with the class' package.  For nested classes, use '$' to separate the parent and nested class
	 * names.  
	 * @param className the fully qualified name of the class
	 * @param classLoader the class loader to use when fetching attribute descriptors, if <code>null</code> use the thread's context class loader
	 * @return the bundle of attributes for the class matching the given name
	 */
	public abstract Bundle getClass(String className, ClassLoader classLoader);
	
	/**
	 * Get the bundle of attributes for the given method.  The name of the class to which
	 * the method belongs must be provided as for @link{#getClass}.  The method's signature
	 * consists of the method name and the parameter type list in parentheses.  Each
	 * parameter type is a fully qualified type name encoded according to the rules of
	 * {@link java.lang.Class#getName() Class.getName()}.  The parameter types are
	 * separated by commas, with no spaces.  The return type is not listed.  Methods with
	 * no parameters must include an empty parameter list, '()'.
	 * @param className the fully qualified name of the method's class
	 * @param methodSignature the name and parameter types of the desired method
	 * @param classLoader the class loader to use when fetching attribute descriptors, if <code>null</code> use the thread's context class loader
	 * @return the bundle of attributes for the method matching the given name
	 * @see #getClass(String,ClassLoader)
	 */
	public abstract Bundle getMethod(String className, String methodSignature, ClassLoader classLoader);
	
	/**
	 * Get the bundle of attributes for the given constructor. The name of the class to which
	 * the constructor belongs must be provided as for @link{#getClass}.  The parameter type
	 * list must be provided as for @link{#getMethod}, but with the method name omitted.
	 * For example, <code>"java.lang.Thread.&lt;init&gt;(java.lang.Runnable,java.lang.String)"</code>.
	 * @param className the fully qualified name of the constructor's class
	 * @param constructorSignature the parameter types of the desired constructor
	 * @param classLoader the class loader to use when fetching attribute descriptors, if <code>null</code> use the thread's context class loader
	 * @return the bundle of attributes for the constructor matching the given name
	 */
	public abstract Bundle getConstructor(String className, String constructorSignature, ClassLoader classLoader);
	
	/**
	 * The the bundle of attributes for the given field.
	 * @param className the fully qualified name of the field's class
	 * @param fieldName the name of the desired field
	 * @param classLoader the class loader to use when fetching attribute descriptors, if <code>null</code> use the thread's context class loader
	 * @return the bundle of attributes for the field matching the given name
	 */
	public abstract Bundle getField(String className, String fieldName, ClassLoader classLoader);
	
}