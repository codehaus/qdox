package com.thoughtworks.qdox.attributes.impl;

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract base for class file parsers.  Implements the base parsing flow, with strategically
 * placed calls to optional methods (template pattern).
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public abstract class ClassFileBase {

	public static final BundleBase[] EMPTY_BUNDLE_ARRAY = new BundleBase[0];
	protected static final byte[] PACK_NAME_BYTES = {0x51, 0x44, 0x6F, 0x78, 0x41, 0x74, 0x74, 0x72, 0x69, 0x62, 0x75, 0x74, 0x65, 0x73};

	protected byte[] b;
	protected int len;
	protected int packNameItemNumber;

	/**
	 * Read information from the given stream, and close it if so requested.
	 * 
	 * @param in the stream to read the class file from
	 * @param closeStream whether the stream should be closed when parsing is complete
	 * @throws IOException if there's a problem reading the file
	 */
	public ClassFileBase(InputStream in, boolean closeStream) throws IOException, ClassNotFoundException {
		try {
			readClass(in);
			parse();
		} finally {
			if (closeStream) try {in.close();} catch (IOException e) {/* ignore exceptions on close */}
		}
	}
	
	protected void ensureSpace(int desiredLength) {
		if (b.length < desiredLength) {
			int newLength = b.length+64;
			if (newLength < desiredLength) newLength = desiredLength;
			byte[] c = new byte[newLength];
			System.arraycopy(b, 0, c, 0, len);
			b = c;
		}
		if (! (b.length >= desiredLength) ) throw new RuntimeException("assertion failure");
	}

	private void readClass(final InputStream is) throws IOException {
		b = new byte[is.available()];
		len = 0;
		while (true) {
			int n = is.read(b, len, b.length - len);
			if (n == -1) return;
			else if ( (len += n) == b.length) ensureSpace(b.length+1024);
		}
	}

	protected int afterReadPool(int index, int numItems) {return index;}
	protected int readBundles(int index, int size) throws IOException, ClassNotFoundException {
		return index + size;
	}
	protected void afterAttributes(int indexNumAttributes, int numAttributes) {}
	protected void initPool(int numItems) {}
	protected void savePoolItem(int itemNumber, int index) {}
	protected void processPoolUTF8(int byteLength) {}
	protected int processHeader(int index) {
		return index + 8 + 2 * readUnsignedShort(index + 6);
	}

	private void parse() throws IOException, ClassNotFoundException {
		int i, j;
		
		// parse the constant pool
		int numItems = readUnsignedShort(8);
		initPool(numItems);
		int index = 10;
		for (i = 1; i < numItems; ++i) {
			int size;
			savePoolItem(i, index);
			switch (b[index]) {
				case FIELD: case METH: case IMETH: case INT: case FLOAT: case NAME_TYPE:
					size = 5;
					break;
				case LONG: case DOUBLE:
					size = 9;
					i++;
					break;
				case UTF8:
					int byteLength = readUnsignedShort(index + 1);
					size = 3 + byteLength;
					processPoolUTF8(byteLength);
					CHECK: if (packNameItemNumber == 0 && byteLength == PACK_NAME_BYTES.length) {
						for (j=0; j < byteLength; j++) if (b[index+3+j] != PACK_NAME_BYTES[j]) break CHECK;
						packNameItemNumber = i;
					}
					break;
				//case CLASS:
				//case STR:
				default:
					size = 3;
					break;
			}
			index += size;
		}
		
		index = afterReadPool(index, numItems);
		
		// read the header
		index = processHeader(index);
		
		// skip fields and methods
		i = readUnsignedShort(index);
		index += 2;
		for (; i > 0; --i) {
			j = readUnsignedShort(index + 6);
			index += 8;
			for (; j > 0; --j) index += 6 + readInt(index + 2);
		}
		i = readUnsignedShort(index);
		index += 2;
		for (; i > 0; --i) {
			j = readUnsignedShort(index + 6);
			index += 8;
			for (; j > 0; --j) index += 6 + readInt(index + 2);
		}
		
		// scan the class's attributes, reading only the bundle pack if it's there
		final int indexNumAttributes = index;
		int numAttributes = i = readUnsignedShort(index); index += 2;
		for ( ; i > 0; i--) {
			int size = readInt(index + 2);
			if (packNameItemNumber == readUnsignedShort(index)) {
				index = readBundles(index+6, size);
			} else {
		  		index += 6 + size;
			}
		}
		
		afterAttributes(indexNumAttributes, numAttributes);
		
	}

	protected int readUnsignedShort(final int index) {
		byte[] b = this.b;
		return ((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF);
	}

	protected int readInt(final int index) {
		byte[] b = this.b;
		return ((b[index] & 0xFF) << 24)
			| ((b[index + 1] & 0xFF) << 16)
			| ((b[index + 2] & 0xFF) << 8)
			| (b[index + 3] & 0xFF);
	}

	protected static final int FIELD = 9;
	protected static final int METH = 10;
	protected static final int IMETH = 11;
	protected static final int INT = 3;
	protected static final int FLOAT = 4;
	protected static final int LONG = 5;
	protected static final int DOUBLE = 6;
	protected static final int NAME_TYPE = 12;
	protected static final int UTF8 = 1;

}
