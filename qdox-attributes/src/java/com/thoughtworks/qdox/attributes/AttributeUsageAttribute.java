package com.thoughtworks.qdox.attributes;

import junit.framework.TestCase;

/**
 * An attribute used to specify usage characteristics of an attribute class.  You can specify the
 * permitted target element types that the attribute can be applied to, whether it can be
 * applied multiple times to one element, and whether it is inherited.  If not specified,
 * attributes can be applied to all element types, are allowed to repeat and are not inherited.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 * @AttributeUsage {CLASS} allowMultiple=false inherit=true final=true
 */
public class AttributeUsageAttribute implements java.io.Serializable {
	private short flags;
	
	public AttributeUsageAttribute(ElementType[] targets) {
		for (int i=0; i<targets.length; i++) {
			for (int j=i+1; j<targets.length; j++) {
				if (targets[i] == targets[j]) throw new IllegalArgumentException("duplicate target " + targets[i]);
			}
			flags |= 1<<targets[i].intValue();
		}
	}
	
	public boolean getAllowMultiple() {return (flags & 1<<12) == 0;}	// true by default
	public void setAllowMultiple(boolean allowMultiple) {
		Attributes.getInstance().checkModifyAttribute();
		if (getAllowMultiple() != allowMultiple) flags ^= 1<<12;
	}
	public boolean getInherit() {return (flags & 1<<13) != 0;}
	public void setInherit(boolean inherit) {
		Attributes.getInstance().checkModifyAttribute();
		if (getInherit() != inherit) flags ^= 1<<13;
	}
	public boolean getFinal() {return (flags & 1<<14) != 0;}
	public void setFinal(boolean fin) {
		Attributes.getInstance().checkModifyAttribute();
		if (getFinal() != fin) flags ^= 1<<14;
	}
	public boolean allowsTarget(ElementType target) {return (flags & 1<<target.intValue()) != 0;}
	
	public static final ElementType[] ALL = new ElementType[] {
		ElementType.CLASS, ElementType.INTERFACE, ElementType.FIELD, ElementType.METHOD,
		ElementType.CONSTRUCTOR, ElementType.PACKAGE, ElementType.PARAMETER,
		ElementType.RETURN
	};
	public static final ElementType[] TYPES = new ElementType[] {
		ElementType.CLASS, ElementType.INTERFACE
	};
	public static final ElementType[] MEMBERS = new ElementType[] {
		ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR
	};
	
	public static class Test extends TestCase {
		protected void setUp() {
			System.setProperty(Attributes.SIMPLE_IMPL_CLASS_NAME_PROPKEY, Attributes.DEFAULT_SIMPLE_IMPL_CLASS_NAME);
			Attributes.reset();
		}
		public void testCreate1() {
			AttributeUsageAttribute a = new AttributeUsageAttribute(new ElementType[]{ElementType.FIELD, ElementType.PACKAGE});
			assertTrue(a.allowsTarget(ElementType.FIELD));
			assertTrue(a.allowsTarget(ElementType.PACKAGE));
			assertFalse(a.allowsTarget(ElementType.CLASS));
			assertFalse(a.allowsTarget(ElementType.RETURN));
		}
		public void testCreate2() {
			AttributeUsageAttribute a = new AttributeUsageAttribute(new ElementType[]{});
			assertTrue(a.getAllowMultiple());
			assertFalse(a.getInherit());
		}
		public void testCreate3() {
			System.setProperty(Attributes.SIMPLE_IMPL_CLASS_NAME_PROPKEY, "com.thoughtworks.qdox.attributes.impl.CompileTimeAttributesImpl");
			Attributes.reset();
			AttributeUsageAttribute a = new AttributeUsageAttribute(new ElementType[]{});
			a.setAllowMultiple(false);
			a.setInherit(true);
			assertFalse(a.getAllowMultiple());
			assertTrue(a.getInherit());
		}
		public void testCreate4() {
			try {
				AttributeUsageAttribute a = new AttributeUsageAttribute(new ElementType[]{});
				a.setAllowMultiple(false);
				fail();
			} catch (IllegalStateException e) {
			}
		}
		public void testCreate5() {
			AttributeUsageAttribute a = new AttributeUsageAttribute(ALL);
			assertTrue(a.allowsTarget(ElementType.FIELD));
			assertTrue(a.allowsTarget(ElementType.PACKAGE));
			assertTrue(a.allowsTarget(ElementType.CLASS));
			assertTrue(a.allowsTarget(ElementType.RETURN));
		}
		public void testCreateDuplicate() {
			try {
				new AttributeUsageAttribute(new ElementType[]{ElementType.FIELD, ElementType.PACKAGE, ElementType.FIELD});
				fail();
			} catch (IllegalArgumentException e) {
			}
		}
	}

}
