package com.thoughtworks.qdox.attributes.dev;

import java.io.*;

import com.thoughtworks.qdox.attributes.impl.ReadClassFile;

/**
 * Read a class file and remove the bundles attribute from it.  Does not remove the attribute's
 * name from the constant pool, since it's too difficult to tell if somebody else might be using
 * it too by coincidence.  This class is used when compacting JARs.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class CompactClassFile extends ReadClassFile {
	
	private boolean bundlesRemoved;

	public CompactClassFile(InputStream in) throws IOException, ClassNotFoundException {
		super(in, false);
	}

	protected int readBundles(int index, int size) throws IOException, ClassNotFoundException {
		// remove bundles from class file
		System.arraycopy(b, index+size, b, index-6, len-(index+size));
		len -= size+6;
		bundlesRemoved = true;
		return index-6;
	}

	protected void afterAttributes(int indexNumAttributes, int numAttributes) {
		// adjust num attributes if necessary
		if (bundlesRemoved) writeUnsignedShort(indexNumAttributes, numAttributes-1);
	}
	
	public void save(OutputStream out) throws IOException {
		out.write(b, 0, len);
		// don't close!
	}

	private void writeUnsignedShort(int index, final int s) {
		byte[] b = this.b;
		b[index++] = (byte) (s >>> 8);
		b[index++] = (byte) s;
	}

}
