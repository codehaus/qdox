package com.thoughtworks.qdox.attributes.dev;

import java.io.IOException;
import java.io.InputStream;

import com.thoughtworks.qdox.attributes.impl.ClassFileBase;

/**
 * Reads a class file to find out if it has an attributes attribute.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class CheckClassFile extends ClassFileBase {

	private boolean hasBundles;

	public CheckClassFile(InputStream in) throws IOException, ClassNotFoundException {
		super(in, true);
	}

	public boolean hasBundles() {
		return hasBundles;
	}
	
	protected int readBundles(int index, int size) throws IOException, ClassNotFoundException {
		hasBundles = true;
		return super.readBundles(index, size);
	}
}
