package com.thoughtworks.qdox.attributes.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import com.thoughtworks.qdox.attributes.*;

/**
 * Stands in for an invalid bundle, holding the relevant errors so they don't get lost or ignored.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ErrorBundle extends BundleBase {
	
	public ErrorBundle() {
		this("");
	}
	
	public ErrorBundle(String kind) {
		this.kind = kind;
	}
	
	void afterLoad() {
		clearCreators();
		Logger.getLogger(Attributes.class.getName()).warning(getErrorMessage());
	}
	
	private final String kind;
	private transient String errorMessage;
	public String getErrorMessage() {
		if (errorMessage == null) {
			StringBuffer buf = new StringBuffer();
			buf.append("The " + kind + " bundle for ").append(getKey()).append(" is invalid due to the following errors:\n");
			for (Iterator it = super.iterator(); it.hasNext(); ) {
				// TODO: indent nested messages
				buf.append(it.next()).append("\n");
			}
			errorMessage = buf.toString();
		}
		return errorMessage;
	}
	
	public void addError(Exception e) {
		super.add(e.getMessage());
	}
	
	public int getErrorCount() {
		return super.size();
	}
	
	public Iterator iterator() {throw new InvalidBundleException(getErrorMessage());}
	public Object[] toArray() {throw new InvalidBundleException(getErrorMessage());}
	public void toArray(Object[] a) {throw new InvalidBundleException(getErrorMessage());}
	public Map getProvenanceMap() {throw new InvalidBundleException(getErrorMessage());}
	public int size() {throw new InvalidBundleException(getErrorMessage());}
	public Object get(Class klass) {throw new InvalidBundleException(getErrorMessage());}
	public Iterator iterator(Class klass) {throw new InvalidBundleException(getErrorMessage());}
	public boolean has(Class klass) {throw new InvalidBundleException(getErrorMessage());}
	public String get(String key) {throw new InvalidBundleException(getErrorMessage());}
	public Iterator iterator(String key) {throw new InvalidBundleException(getErrorMessage());}
	public boolean has(String key) {throw new InvalidBundleException(getErrorMessage());}
	public void add(Object o) {throw new InvalidBundleException(getErrorMessage());}
	public void add(Object o, Creator creator) {throw new InvalidBundleException(getErrorMessage());}

}
