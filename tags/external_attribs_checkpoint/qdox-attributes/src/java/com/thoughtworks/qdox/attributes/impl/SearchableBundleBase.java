package com.thoughtworks.qdox.attributes.impl;

import java.util.Iterator;

import com.thoughtworks.qdox.attributes.MultipleValuesException;
import com.thoughtworks.qdox.attributes.SimpleAttribute;

/**
 * A bundle that can be searched and iterated.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public abstract class SearchableBundleBase extends BundleBase {

	protected void afterLoad() {
		clearCreators();
	}

	public synchronized Object get(Class klass) {
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

	public synchronized Iterator iterator(final Class klass) {
		return new Iterator() {
			private int index; {findNext();}
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

	public synchronized boolean has(Class klass) {
		for (int i = 0; i < size; i++) {
			if (klass.isAssignableFrom(objects[i].getClass())) return true;
		}
		return false;
	}

	public synchronized String get(String key) {
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

	public synchronized Iterator iterator(final String key) {
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

	public synchronized boolean has(String key) {
		for (int i = 0; i < size; i++) {
			if (objects[i] instanceof SimpleAttribute && key.equals(((SimpleAttribute) objects[i]).getKey())) return true;
		}
		return false;
	}

}
