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
public class SimpleBundle extends IndirectObjectList implements Bundle, Externalizable {
	
	public SimpleBundle() {}
	
	public void add(String key, String value) {
		add(new SimpleAttribute(key, value));
	}

	public Object get(Class klass) {
		Object r = null;
		for (int i = 0; i < size; i++) {
			Object a = objects[i];
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
					if (klass.isAssignableFrom(objects[index].getClass())) break;
					index++;
				}
			}
			public boolean hasNext() {
				return index < size;
			}
			public Object next() {
				Object r = objects[index++];
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
			if (klass.isAssignableFrom(objects[i].getClass())) return true;
		}
		return false;
	}
	
	public String get(String key) {
		String r = null;
		for (int i = 0; i < size; i++) {
			if (objects[i] instanceof SimpleAttribute) {
				SimpleAttribute a = (SimpleAttribute) objects[i];
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
					if (objects[index] instanceof SimpleAttribute && key.equals(((SimpleAttribute) objects[index]).getKey())) break;
					index++;
				}
			}
			public boolean hasNext() {
				return index < size;
			}
			public Object next() {
				Object r = objects[index++];
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
			if (objects[i] instanceof SimpleAttribute && key.equals(((SimpleAttribute) objects[i]).getKey())) return true;
		}
		return false;
	}

	public Bundle includingInherited() {
		// TODO: implement
		throw new UnsupportedOperationException();
	}

}