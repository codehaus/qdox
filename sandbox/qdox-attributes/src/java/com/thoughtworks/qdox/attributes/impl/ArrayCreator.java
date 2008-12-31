package com.thoughtworks.qdox.attributes.impl;

import java.lang.reflect.Array;

import junit.framework.TestCase;


/**
 * Lazily creates arrays.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ArrayCreator implements Creator {
	private final Class type;
	private final IndirectObjectList contents;
	
	public ArrayCreator(Class type, IndirectObjectList contents) {
		if (!type.isArray()) throw new IllegalArgumentException("type is not an array");
		this.type = type;
		this.contents = contents;
	}

	public Object create() {
		Object[] a = (Object[]) Array.newInstance(type.getComponentType(), contents.size());
		contents.toArray(a);
		return a;
	}
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class Test extends TestCase {
		public void testCreate1() {
			IndirectObjectList c = new IndirectObjectList();
			c.add("hello"); 
			ArrayCreator ac = new ArrayCreator(String[].class, c);
			Object o = ac.create();
			assertSame(String[].class, o.getClass());
			String[] a = (String[]) o;
			assertEquals(1, a.length);
			assertEquals("hello", a[0]);
		}
		public void testCreate2() {
			IndirectObjectList c = new IndirectObjectList();
			c.add(new String[] {"hello"});
			c.add(new String[] {"bye"});
			ArrayCreator ac = new ArrayCreator(String[][].class, c);
			Object o = ac.create();
			assertSame(String[][].class, o.getClass());
			String[][] a = (String[][]) o;
			assertEquals(2, a.length);
			assertEquals(1, a[0].length);
			assertEquals(1, a[1].length);
			assertEquals("hello", a[0][0]);
			assertEquals("bye", a[1][0]);
		}
	}

}
