package com.thoughtworks.qdox.attributes.impl;

import java.io.*;
import java.util.Iterator;

import com.thoughtworks.qdox.attributes.*;

/**
 * A simple ordered bundle of attributes.  This implementation keeps the attributes
 * in an object array, and uses linear search to find entries.
 * Normally, there's so few attributes in a bundle that the running time of a linear
 * search should be comparable to more efficient methods (e.g. hash map).  The
 * array also allows duplicate keys, keeps the order of all the attributes, and should
 * save memory.
 */
public class SimpleBundle implements Bundle, Externalizable {
	
	private int size = 0;
	private Object[] attribs = SimpleBundle.EMPTY_OBJECT_ARRAY;
	private Creator[] creators = SimpleBundle.EMPTY_CREATOR_ARRAY;
	
	private static final int SIZE_INCREMENT = 3;	// arbitrary
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	private static final Creator[] EMPTY_CREATOR_ARRAY = new Creator[0];
	
	public SimpleBundle() {}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		creators = (Creator[]) in.readObject();
		if (creators == null) creators = EMPTY_CREATOR_ARRAY;
		size = in.readInt();
		attribs = new Object[size];
		for (int i=0; i<size; i++) {
			Object attribute = in.readObject();
			if (attribute == null) try {
				attribute = creators[i].create();
			} catch (Exception e) {
				IOException e2 = new InvalidObjectException("unable to recreate non-serializable attribute");
				e2.initCause(e);
				throw e2;
			}
			attribs[i] = attribute;
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(creators.length == 0 ? null : creators);
		out.writeInt(size);
		for (int i=0; i<size; i++) {
			Object a = attribs[i];
			if (creators.length > i && creators[i] != null) {
				out.writeObject(null);
			} else if (a instanceof Serializable) {
				out.writeObject(a);
			} else  {
				throw new NotSerializableException(a.getClass().getName());
			}
		}
	}

	public Object[] toArray() {
		Object[] a = new Object[size];
		System.arraycopy(attribs, 0, a, 0, size);
		return a;
	}

	public Iterator iterator() {
		return new Iterator() {
			private int index;
			public boolean hasNext() {
				return index < size;
			}
			public Object next() {
				return attribs[index++];
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public int size() {return size;}

	public void clear() {
		attribs = EMPTY_OBJECT_ARRAY;
		size = 0;
	}
	
	private void ensureSize(int desiredSize) {
		if (attribs.length < desiredSize) {
			Object[] a = new Object[Math.max(attribs.length + SIZE_INCREMENT, desiredSize)];
			System.arraycopy(attribs, 0, a, 0, size);
			attribs = a;
		}
		assert attribs.length >= desiredSize;
	}
	
	public void add(String key, String value) {
		ensureSize(size+1);
		attribs[size++] = new SimpleAttribute(key, value);
	}

	public void add(Object attribute) {
		ensureSize(size+1);
		attribs[size++] = attribute;
	}

	public void add(Object attribute, Creator creator) {
		ensureSize(size+1);
		attribs[size++] = attribute;
		if (creators.length < size) {
			Creator[] a = new Creator[Math.max(creators.length + SIZE_INCREMENT, size)];
			System.arraycopy(creators, 0, a, 0, creators.length);
			creators = a;
		}
		assert creators.length >= size;
		creators[size-1] = creator;
	}

	public Object get(Class klass) {
		Object r = null;
		for (int i = 0; i < size; i++) {
			Object a = attribs[i];
			if (klass.isAssignableFrom(a.getClass())) {
				if (r == null) r = a;
				else throw new MultipleValuesException();
			}
		}
		return r;
	}

	public Iterator iterator(final Class klass) {
		return new Iterator() {
			private int index;  {findNext();}
			private void findNext() {
				while(index < size) {
					if (klass.isAssignableFrom(attribs[index].getClass())) break;
					index++;
				}
			}
			public boolean hasNext() {
				return index < size;
			}
			public Object next() {
				Object r = attribs[index++];
				findNext();
				return r;
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public boolean has(Class klass) {
		for (int i = 0; i < size; i++) {
			if (klass.isAssignableFrom(attribs[i].getClass())) return true;
		}
		return false;
	}
	public String get(String key) {
		String r = null;
		for (int i = 0; i < size; i++) {
			if (attribs[i] instanceof SimpleAttribute) {
				SimpleAttribute a = (SimpleAttribute) attribs[i];
				if (key.equals(a.getKey())) {
					if (r == null) r = a.getValue();
					else throw new MultipleValuesException();
				}
			}
		}
		return r;
	}

	public Iterator iterator(final String key) {
		return new Iterator() {
			private int index;  {findNext();}
			private void findNext() {
				while(index < size) {
					if (attribs[index] instanceof SimpleAttribute && key.equals(((SimpleAttribute) attribs[index]).getKey())) break;
					index++;
				}
			}
			public boolean hasNext() {
				return index < size;
			}
			public Object next() {
				Object r = attribs[index++];
				findNext();
				return ((SimpleAttribute) r).getValue();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public boolean has(String key) {
		for (int i = 0; i < size; i++) {
			if (attribs[i] instanceof SimpleAttribute && key.equals(((SimpleAttribute) attribs[i]).getKey())) return true;
		}
		return false;
	}

	public Bundle includingInherited() {
		// TODO: implement
		throw new UnsupportedOperationException();
	}

}