package com.thoughtworks.qdox.attributes.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.Attributes;

/**
 * A creator for attributes that cannot be serialized.  It is initialized with all the necessary
 * data, and recreates the original attribute when deserialized.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class AttributeCreator implements Creator {
	private final Class attributeClass;
	private final Class[] constructorArgTypes;
	private final IndirectObjectList constructorArgs;
	private final IndirectObjectList propertyTriples;
	
	private final static Class[] EMPTY_PARAM = new Class[0];
	private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	
	public AttributeCreator(Class attributeClass, Class[] constructorArgTypes, IndirectObjectList constructorArgs, IndirectObjectList propertyPairs) {
		if ((constructorArgTypes == null || constructorArgs == null) && (constructorArgTypes != null || constructorArgs != null) || constructorArgTypes.length != constructorArgs.size()) throw new IllegalArgumentException("constructor arg types don't match arg list");
		this.attributeClass = attributeClass;
		// optimize to nulls to lower stored size
		this.constructorArgTypes = constructorArgTypes == null || constructorArgTypes.length == 0 ? null : constructorArgTypes;
		this.constructorArgs = constructorArgs == null || constructorArgs.size() == 0 ? null : constructorArgs;
		this.propertyTriples = propertyPairs == null || propertyPairs.size() == 0 ? null : propertyPairs;;
	}
	
	public Object create() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		((SimpleAttributesImpl) Attributes.getInstance(false)).canModifyAttributes = true;
		try {
			Class[] paramTypes = constructorArgTypes == null ? EMPTY_PARAM : constructorArgTypes;
			Object[] args = constructorArgs == null ? EMPTY_OBJECT_ARRAY : constructorArgs.toArray();
			Object attr = attributeClass.getConstructor(paramTypes).newInstance(args);
			if (propertyTriples != null) {
				for (Iterator it = propertyTriples.iterator(); it.hasNext(); ) {
					attributeClass.getMethod((String) it.next(), new Class[]{(Class) it.next()}).invoke(attr, new Object[]{it.next()});
				}
			}
			return attr;
		} finally {
			((SimpleAttributesImpl) Attributes.getInstance(false)).canModifyAttributes = false;
		}
	}
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class Test extends TestCase {
		private AttributeCreator createSampleInstance() {
			IndirectObjectList args = new IndirectObjectList(), propTriples = new IndirectObjectList();
			args.add("#,##0.0#;(#)");
			propTriples.add("setNegativePrefix");
			propTriples.add(String.class);
			propTriples.add("-");
			propTriples.add("setPositiveSuffix");
			propTriples.add(String.class);
			propTriples.add("+");
			return new AttributeCreator(java.text.DecimalFormat.class, new Class[] {String.class}, args, propTriples);
		}
		public void testMake() throws Exception {
			AttributeCreator c = createSampleInstance();
			assertSame(java.text.DecimalFormat.class, c.attributeClass);
			assertTrue(Arrays.equals(new Class[] {String.class}, c.constructorArgTypes));
			assertTrue(Arrays.equals(new Object[]{"#,##0.0#;(#)"}, c.constructorArgs.toArray()));
			assertTrue(Arrays.equals(new Object[]{"setNegativePrefix", String.class, "-", "setPositiveSuffix", String.class, "+"}, c.propertyTriples.toArray()));
		}
		public void testConstructorEmptyArrays() {
			AttributeCreator c = new AttributeCreator(java.text.DecimalFormat.class, new Class[0], new IndirectObjectList(), new IndirectObjectList());
			assertSame(java.text.DecimalFormat.class, c.attributeClass);
			assertNull(c.constructorArgs);
			assertNull(c.propertyTriples);
		}
		public void testCreateEmptyArrays() throws Exception {
			AttributeCreator c = new AttributeCreator(java.text.DecimalFormat.class, new Class[0], new IndirectObjectList(), new IndirectObjectList());
			Object o = c.create();
			assertNotNull(o);
			assertTrue(o instanceof java.text.DecimalFormat);			
		}
		public void testCreate() throws Exception {
			AttributeCreator c = createSampleInstance();
			Object o = c.create();
			assertNotNull(o);
			assertTrue(o instanceof java.text.DecimalFormat);
			java.text.DecimalFormat df = (java.text.DecimalFormat) o;
			assertEquals("-", df.getNegativePrefix());
			assertEquals("+", df.getPositiveSuffix());
			assertEquals("#,##0.0#+;'-'#,##0.0#", df.toPattern());
		}
	}
}
