package com.thoughtworks.qdox.attributes.impl;

import java.io.*;
import java.util.Iterator;
import java.util.List;


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
	protected Object[] objects = EMPTY_OBJECT_ARRAY;
	protected Creator[] creators = EMPTY_CREATOR_ARRAY;
	
	private static final int SIZE_INCREMENT = 3;	// arbitrary
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final Creator[] EMPTY_CREATOR_ARRAY = new Creator[0];
	
	/**
	 * Copy the contents of the original list into this list.
	 * @param original the list from which to copy
	 */
	public void copy(IndirectObjectList original) {
		this.size = original.size;
		this.objects = original.toArray();
		if (original.creators == EMPTY_CREATOR_ARRAY) {
			this.creators = EMPTY_CREATOR_ARRAY;
		} else {
			this.creators = new Creator[original.creators.length];
			System.arraycopy(original.creators, 0, creators, 0, creators.length);
		}
	}
	
	synchronized void clearCreators() {
		creators = EMPTY_CREATOR_ARRAY;
	}
	
	public synchronized void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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

	public synchronized void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(creators.length == 0 ? null : creators);
		out.writeInt(size);
		for (int i=0; i<size; i++) {
			out.writeObject(creators.length > i && creators[i] != null ? null : objects[i]);
		}
	}

	public synchronized Object[] toArray() {
		Object[] a = new Object[size];
		System.arraycopy(objects, 0, a, 0, size);
		return a;
	}
	
	public synchronized void toArray(Object[] a) {
		System.arraycopy(objects, 0, a, 0, size);
	}

	public synchronized Iterator iterator() {
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

	public synchronized int size() {return size;}
	
	private synchronized void ensureCapacity(int desiredCapacity) {
		if (objects.length < desiredCapacity) {
			Object[] a = new Object[Math.max(objects.length + SIZE_INCREMENT, desiredCapacity)];
			System.arraycopy(objects, 0, a, 0, size);
			objects = a;
		}
		assert objects.length >= desiredCapacity;
	}
	
	public synchronized void add(Object o) {
		ensureCapacity(size+1);
		objects[size++] = o;
	}

	public synchronized void add(Object o, Creator creator) {
		add(o);
		if (creators.length < size) {
			Creator[] a = new Creator[Math.max(creators.length + SIZE_INCREMENT, size)];
			System.arraycopy(creators, 0, a, 0, creators.length);
			creators = a;
		}
		assert creators.length >= size;
		creators[size-1] = creator;
	}
	
	public synchronized void addAll(List list) {
		ensureCapacity(size+list.size());
		for (Iterator it = list.iterator(); it.hasNext();) {
			objects[size++] = it.next();
		}
	}

}
