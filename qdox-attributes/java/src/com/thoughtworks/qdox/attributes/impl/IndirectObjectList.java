package com.thoughtworks.qdox.attributes.impl;

import java.io.*;
import java.util.Iterator;

import com.thoughtworks.qdox.attributes.Creator;

/**
 * An array of objects, some of which may have matching creators.  The creators
 * are used to serialize an object and reconstruct it later, if the object itself is not
 * serializable.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class IndirectObjectList implements Externalizable {
	
	protected int size;
	protected Object[] objects;
	protected Creator[] creators;
	
	private static final int SIZE_INCREMENT = 3;	// arbitrary
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final Creator[] EMPTY_CREATOR_ARRAY = new Creator[0];
	
	public IndirectObjectList() {
		clear();
	}
		
	public void clear() {
		objects = EMPTY_OBJECT_ARRAY;
		creators = EMPTY_CREATOR_ARRAY;
		size = 0;
	}
	
	void clearCreators() {
		creators = EMPTY_CREATOR_ARRAY;
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		creators = (Creator[]) in.readObject();
		if (creators == null) creators = EMPTY_CREATOR_ARRAY;
		size = in.readInt();
		objects = new Object[size];
		for (int i=0; i<size; i++) {
			Object attribute = in.readObject();
			if (attribute == null) try {
				attribute = creators[i].create();
			} catch (Exception e) {
				IOException e2 = new InvalidObjectException("unable to recreate non-serializable object");
				e2.initCause(e);
				throw e2;
			}
			objects[i] = attribute;
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(creators.length == 0 ? null : creators);
		out.writeInt(size);
		for (int i=0; i<size; i++) {
			out.writeObject(creators.length > i && creators[i] != null ? null : objects[i]);
		}
	}

	public Object[] toArray() {
		Object[] a = new Object[size];
		System.arraycopy(objects, 0, a, 0, size);
		return a;
	}
	
	public void toArray(Object[] a) {
		System.arraycopy(objects, 0, a, 0, size);
	}

	public Iterator iterator() {
		return new Iterator() {
			private int index;
			public boolean hasNext() {
				return index < size;
			}
			public Object next() {
				return objects[index++];
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public int size() {return size;}

	private void ensureCapacity(int desiredCapacity) {
		if (objects.length < desiredCapacity) {
			Object[] a = new Object[Math.max(objects.length + SIZE_INCREMENT, desiredCapacity)];
			System.arraycopy(objects, 0, a, 0, size);
			objects = a;
		}
		assert objects.length >= desiredCapacity;
	}
	
	public void add(Object o) {
		ensureCapacity(size+1);
		objects[size++] = o;
	}

	public void add(Object o, Creator creator) {
		add(o);
		if (creators.length < size) {
			Creator[] a = new Creator[Math.max(creators.length + SIZE_INCREMENT, size)];
			System.arraycopy(creators, 0, a, 0, creators.length);
			creators = a;
		}
		assert creators.length >= size;
		creators[size-1] = creator;
	}

}
