package com.thoughtworks.qdox.attributes.impl;

import java.io.*;

/**
 * Bare-bones functionality to read an attributes attribute from a class file.  Code adapted from ASM.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ReadClassFile extends ClassFileBase {
	
	private BundleBase[] bundles;
	private int[] items;
	private int maxStringLength;
	private char[] buf;
	private String className;
	private String[] supertypes;
	
	/**
	 * Read information from the given stream, then close the stream.
	 * The instance created is in read-only mode.
	 * 
	 * @param in the stream to read the class file from
	 * @param closeStream whether the stream should be closed when parsing is complete
	 * @throws IOException if there's a problem reading the file
	 */
	public ReadClassFile(InputStream in, boolean closeStream) throws IOException, ClassNotFoundException {
		super(in, closeStream);
		if (bundles == null) bundles = EMPTY_BUNDLE_ARRAY;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String[] getSupertypes() {
		return supertypes;
	}
	
	protected void initPool(int numItems) {
		items = new int[numItems];
	}
	
	protected void savePoolItem(int itemNumber, int index) {
		items[itemNumber] = index+1;
	}
	
	protected void processPoolUTF8(int byteLength) {
		maxStringLength = byteLength > maxStringLength ? byteLength : maxStringLength;
	}
	
	protected int afterReadPool(int index, int numItems) {
		buf = new char[maxStringLength];  // buffer used to read strings
		return index;
	}
	
	protected int processHeader(int index) {
		className = readUTF8(items[readUnsignedShort(index+2)]);
		int v = items[readUnsignedShort(index + 4)];
		supertypes = new String[readUnsignedShort(index + 6) + (v == 0 ? 0 : 1)];
		if (v != 0) {
			supertypes[0] = readUTF8(v);
			v = 1;
		}
		index += 8;
		for (; v < supertypes.length; v++) {
			supertypes[v] = readUTF8(items[readUnsignedShort(index)]);
			index += 2;
		}
		return index;
	}
	
	protected int readBundles(int index, int size) throws IOException, ClassNotFoundException {
		if (bundles != null) throw new IOException("multiple bundle packs in class file");
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b, index, size));
		bundles = ReadAttributesPack.readBundles(in);
		return super.readBundles(index, size);
	}
	
	public BundleBase[] getBundles() {
		return bundles;
	}
	
	private String readUTF8(int index) {
		// computes the start index of the CONSTANT_Utf8 item in b
		index = items[readUnsignedShort(index)];
		// reads the length of the string (in bytes, not characters)
		int utfLen = readUnsignedShort(index);
		index += 2;
		// parses the string bytes
		int endIndex = index + utfLen;
		byte[] b = this.b;
		int strLen = 0;
		int c, d, e;
		while (index < endIndex) {
			c = b[index++] & 0xFF;
			switch (c >> 4) {
				case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
					// 0xxxxxxx
					buf[strLen++] = (char) c;
					break;
				case 12: case 13:
					// 110x xxxx   10xx xxxx
					d = b[index++];
					buf[strLen++] = (char) (((c & 0x1F) << 6) | (d & 0x3F));
					break;
				default:
					// 1110 xxxx  10xx xxxx  10xx xxxx
					d = b[index++];
					e = b[index++];
					buf[strLen++] = (char) (((c & 0x0F) << 12) | ((d & 0x3F) << 6) | (e & 0x3F));
					break;
			}
		}
		return new String(buf, 0, strLen);
	}

}
