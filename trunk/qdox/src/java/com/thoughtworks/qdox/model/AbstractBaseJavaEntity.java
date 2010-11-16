package com.thoughtworks.qdox.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AbstractBaseJavaEntity implements Serializable {

	protected String name;
	private List<Annotation> annotations = Collections.emptyList();
	private int lineNumber = -1;
	protected JavaClassParent parent;

	public AbstractBaseJavaEntity() {
		super();
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getName() {
	    return name;
	}

	public List<Annotation> getAnnotations() {
	    return annotations;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public void setAnnotations(List<Annotation> annotations) {
	    this.annotations = annotations;
	}

	public void setLineNumber(int lineNumber) {
	    this.lineNumber = lineNumber;
	}

	/**
	 *  
	 * @return
	 * @deprecated
	 */
	public JavaClassParent getParent() { 
	    return parent; 
	}

	/**
	 * 
	 * @param parent
	 * @deprecated
	 */
	public void setParent(JavaClassParent parent) { 
	    this.parent = parent;
	}
	
	/**
	 * Not every entity has a parentClass, but AnnotationFieldRef requires access to it.
	 * When used with JavaClass, don't confuse this with getSuperClass()
	 * 
	 * @return the surrounding class
	 */
	public JavaClass getParentClass() { return null; }

}