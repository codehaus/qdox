package com.thoughtworks.qdox.attributes.impl;

/**
 * An non-validating attributes implementation that always allows attribute modification.  Used
 * at compile-time, and for attribute unit testing.  To set it, do something like:
 * <pre>System.setProperty(Attributes.SIMPLE_IMPL_CLASS_NAME_PROPKEY, "com.thoughtworks.qdox.attributes.impl.CompileTimeAttributesImpl");
 * Attributes.reset();</pre>
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class CompileTimeAttributesImpl extends AttributesImpl {
	public void checkModifyAttribute() {return;}
}
