package com.thoughtworks.qdox.model;

public class JavaParameter {

	private String name;
	private Type type;
	private int dimensions;

	public JavaParameter(Type type, String name, int dimensions) {
		this.name = name;
		this.type = type;
		this.dimensions = dimensions;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public int getDimensions() {
		return dimensions;
	}

	public boolean equals(Object obj) {
		JavaParameter p = (JavaParameter)obj;
		// name isn't used in equality check.
		return getType().equals(p.getType()) && getDimensions() == p.getDimensions();
	}
}
