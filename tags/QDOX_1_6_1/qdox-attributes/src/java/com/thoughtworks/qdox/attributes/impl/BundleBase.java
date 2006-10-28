package com.thoughtworks.qdox.attributes.impl;

import java.io.*;

import com.thoughtworks.qdox.attributes.Bundle;

/**
 * A base bundle class that allows the {@link AttributesPackBase} to deal uniformly with simple and error bundles.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public abstract class BundleBase extends IndirectObjectList implements Bundle {

	private String key;
	void setKey(String key) {this.key = key;}
	String getKey() {return key;}
	
	public synchronized void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		key = in.readUTF();
		super.readExternal(in);
	}
	
	public synchronized void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(key);
		super.writeExternal(out);
	}
	
	abstract void afterLoad();
	
}
