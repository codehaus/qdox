package com.thoughtworks.qdox.attributes;

import java.util.Iterator;
import java.util.Map;

/**
 * A bundle of attributes for some program element.  Each attribute is an object.  Simple
 * attributes are string pairs, the key being the tag used to introduce the attribute and
 * the value being the tag's argument.  Complex attributes can be instances of any class.
 * <p>
 * The original order in which the attributes were specified is maintained by the iterators
 * and the <code>toArray</code> converter.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public interface Bundle {
	
	/**
	 * A read-only iterator over all the attributes in this bundle, in declaration order.
	 * @return a new iterator over all attributes in this bundle
	 */
	Iterator iterator();
	
	/**
	 * Return a new array with all the attributes from this bundle.
	 * @return an array of the attributes in this bundle; changing it will not change the bundle's contents
	 */
	Object[] toArray();
	
	/**
	 * Return an unmodifiable map from all the attributes in this bundle to the program elements on which
	 * they were originally defined.  The keys will be the same attributes returned by {@link #toArray},
	 * in the same order.  The value associated to each key will be the fully qualified name of the class in which
	 * that attribute was originally declared.  For package attributes, the value will be <code>null</code>.
	 * @return an unmodifiable provenance map of the attributes in this bundle
	 */
	Map getProvenanceMap();
	
	/**
	 * Return the number of attributes in this bundle.
	 * @return the number of attributes in this bundle
	 */
	int size();
	
	
	/**
	 * Get an object attribute whose type is assignable to the given type.
	 * @param klass a supertype of the desired attribute type
	 * @return the attribute value, or <code>null</code> if none
	 * @throws MultipleValuesException if there is more than one attribute assignable to the given type
	 */
	Object get(Class klass);
	
	/**
	 * Get an iterator over all object attributes whose type is assignable to the given type.
	 * @param klass a supertype of the desired attribute type
	 * @return the array of attribute values, or an empty array if none
	 */
	Iterator iterator(Class klass);

	/**
	 * Return whether this bundle has any object attributes whose type is assignable to the
	 * given type.
	 * @param klass a supertype of the desired attribute type
	 * @return true if there is at least one object attribute whose type is assignable to the given type, false otherwise
	 */
	boolean has(Class klass);


	/**
	 * Get the string value matching the given key.  If the key was defined with no value,
	 * return the empty string.
	 * @param key the desired attribute's key
	 * @return the value for the given key, or <code>null</code> if the key is not present in this bundle
	 * @throws MultipleValuesException if there is more than one value for the given key
	 */
	String get(String key);
	
	/**
	 * Get an iterator over the values of all the simple attributes with the given key.
	 * @param key the desired attributes' key
	 * @return the array of attribute values, or an empty array if none
	 */
	Iterator iterator(String key);
	
	/**
	 * Return whether this bundle has any string attributes for the given key.
	 * @param key the desired attribute key
	 * @return true if there is at least one string attribute for the given key, false otherwise
	 */
	boolean has(String key);

}