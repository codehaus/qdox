package com.thoughtworks.qdox.attributes.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.Creator;

/**
 * A creator for attributes that cannot be serialized.  It is initialized with all the necessary
 * data, and recreates the original attribute when deserialized.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class AttributeCreator implements Creator {
	private final Class attributeClass;
	private final String[] constructorArgs;
	private final String[] propertyPairs;
	
	private final static Class[] ONE_STRING_PARAM = new Class[] {String.class};
	
	public AttributeCreator(Class attributeClass, String[] constructorArgs, String[] propertyPairs) {
		this.attributeClass = attributeClass;
		this.constructorArgs = constructorArgs == null || constructorArgs.length == 0 ? null : constructorArgs;
		this.propertyPairs = propertyPairs == null || propertyPairs.length == 0 ? null : propertyPairs;;
	}
	
	public Object create() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class[] paramTypes = new Class[constructorArgs == null ? 0 : constructorArgs.length];
		Arrays.fill(paramTypes, String.class);
		Object attr = attributeClass.getConstructor(paramTypes).newInstance(constructorArgs);
		if (propertyPairs != null) {
			for (int i=0; i<propertyPairs.length; i+=2) {
				attributeClass.getMethod(propertyPairs[i], ONE_STRING_PARAM).invoke(attr, new Object[]{propertyPairs[i+1]});
			}
		}
		return attr;
	}
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class Test extends TestCase {
		public void testMake() throws Exception {
			AttributeCreator c = new AttributeCreator(java.text.DecimalFormat.class, new String[]{"#,##0.0#;(#)"}, new String[]{"setNegativePrefix", "-", "setPositiveSuffix", "+"});
			assertSame(java.text.DecimalFormat.class, c.attributeClass);
			assertTrue(Arrays.equals(new String[]{"#,##0.0#;(#)"}, c.constructorArgs));
			assertTrue(Arrays.equals(new String[]{"setNegativePrefix", "-", "setPositiveSuffix", "+"}, c.propertyPairs));
		}
		public void testConstructorEmptyArrays() {
			AttributeCreator c = new AttributeCreator(java.text.DecimalFormat.class, new String[0], new String[0]);
			assertSame(java.text.DecimalFormat.class, c.attributeClass);
			assertNull(c.constructorArgs);
			assertNull(c.propertyPairs);
		}
		public void testCreateEmptyArrays() throws Exception {
			AttributeCreator c = new AttributeCreator(java.text.DecimalFormat.class, new String[0], new String[0]);
			Object o = c.create();
			assertNotNull(o);
			assertTrue(o instanceof java.text.DecimalFormat);			
		}
		public void testCreate() throws Exception {
			AttributeCreator c = new AttributeCreator(java.text.DecimalFormat.class, new String[]{"#,##0.0#;(#)"}, new String[]{"setNegativePrefix", "-", "setPositiveSuffix", "+"});
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
