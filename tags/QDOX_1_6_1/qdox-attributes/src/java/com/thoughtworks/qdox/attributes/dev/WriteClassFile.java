package com.thoughtworks.qdox.attributes.dev;

import java.io.*;

import com.thoughtworks.qdox.attributes.impl.BundleBase;
import com.thoughtworks.qdox.attributes.impl.ClassFileBase;

/**
 * Bare-bones code to write an attributes attribute to a class file.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class WriteClassFile extends ClassFileBase {

	private int bundlesOffset;
	protected BundleBase[] bundles = EMPTY_BUNDLE_ARRAY;
	
	/**
	 * Read information from the given class file.  The instance created is in
	 * rewrite mode, ready to be written back to the given file.  Attributes
	 * are <em>not</em> read in.
	 * 
	 * @param in stream to read from
	 * @param closeStream whether to close the stream when done
	 * @throws IOException if there's a problem reading the file
	 */
	public WriteClassFile(InputStream in, boolean closeStream) throws IOException, ClassNotFoundException {
		super(in, closeStream);
	}
	
	protected int afterReadPool(int index, int numItems) {
		if (packNameItemNumber == 0) {
			// pack attribute name not found, insert it into the constant pool
			final int nameLength = PACK_NAME_BYTES.length;
			ensureSpace(len+3+nameLength);
			System.arraycopy(b, index, b, index+3+nameLength, len-index);
			len += 3 + nameLength;
			b[index++] = UTF8;
			writeUnsignedShort(index, nameLength);
			index += 2;
			System.arraycopy(PACK_NAME_BYTES, 0, b, index, nameLength);
			index += nameLength;
			packNameItemNumber = numItems;
			writeUnsignedShort(8, numItems+1);
		}
		return index;
	}

	protected int readBundles(int index, int size) throws IOException, ClassNotFoundException {
		// don't waste time reading bundles in rewrite mode
		// just take the chunk out of the byte array and record the offset
		bundlesOffset = index;
		System.arraycopy(b, index+size, b, index, len-(index+size));
		len -= size;
		return index;
	}
	
	protected void afterAttributes(int indexNumAttributes, int numAttributes) {
		if (bundlesOffset == 0) {
			//rewrite mode and no pack attribute, create one
			ensureSpace(len+6);
			writeUnsignedShort(indexNumAttributes, numAttributes+1);
			writeUnsignedShort(len, packNameItemNumber);
			bundlesOffset = len += 6;
		}
	}
	
	public void save(OutputStream out) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		WriteAttributesPack.writeBundles(new ObjectOutputStream(bout), bundles);
		bout.close();
		byte[] bundleBytes = bout.toByteArray();

		writeInt(bundlesOffset-4, bundleBytes.length);
		out.write(b, 0, bundlesOffset);
		out.write(bundleBytes);
		out.write(b, bundlesOffset, len-bundlesOffset);
		try {
		} finally {
			try {out.close();} catch (IOException e) { /* ignore */ }
		}
	}

	private void writeUnsignedShort(int index, final int s) {
		byte[] b = this.b;
		b[index++] = (byte) (s >>> 8);
		b[index++] = (byte) s;
	}

	private void writeInt(int index, final int i) {
		byte[] b = this.b;
		b[index++] = (byte) (i >>> 24);
		b[index++] = (byte) (i >>> 16);
		b[index++] = (byte) (i >>> 8);
		b[index++] = (byte) i;
	}

	public void setBundles(BundleBase[] bundles) {
		this.bundles = bundles;
	}

}
