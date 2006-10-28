package com.thoughtworks.qdox.attributes;

/**
 * The enumeration of program elements to which attributes can be applied.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ElementType {
	public static final ElementType CLASS = new ElementType(0, "CLASS");
	public static final ElementType INTERFACE = new ElementType(1, "INTERFACE");
	public static final ElementType FIELD = new ElementType(2, "FIELD");
	public static final ElementType METHOD = new ElementType(3, "METHOD");
	public static final ElementType CONSTRUCTOR = new ElementType(4, "CONSTRUCTOR");
	public static final ElementType PACKAGE = new ElementType(5, "PACKAGE");
	public static final ElementType PARAMETER = new ElementType(6, "PARAMETER");
	public static final ElementType RETURN = new ElementType(7, "RETURN");
	
	private final int intValue;
	private final String name;
	private ElementType(int intValue, String name) {
		this.intValue = intValue;
		this.name = name;
	}
	public int intValue() {return intValue;}
	public String toString() {
		return "ElementType." + name;
	}
}