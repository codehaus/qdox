package com.thoughtworks.qdox.attributes;

/**
 * A simple string key/value pair attribute.  It can be applied to all program elements,
 * can be used multiple times on a single element, and is not inherited.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 * @AttributeUsage ALL allowMultiple=true inherit=false final=false
 */
public class SimpleAttribute implements java.io.Serializable {
	
	private final String key, value;
	public SimpleAttribute(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {return key;}
	public String getValue() {return value;}

	public String toString() {
		return "@" + getKey() + " " + getValue();
	}
}
