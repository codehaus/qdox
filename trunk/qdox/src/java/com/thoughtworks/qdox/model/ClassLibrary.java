package com.thoughtworks.qdox.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ClassLibrary implements Serializable {

	private Set classes = new TreeSet();
	private boolean defaultLoaders = false;
	private transient List classLoaders = new ArrayList();
	private JavaClassCache cache;

	public ClassLibrary(JavaClassCache cache) {
		this.cache = cache;
	}

	public void add(String fullClassName) {
		classes.add(fullClassName);
	}

	public JavaClass getClassByName(String name) {
		return cache.getClassByName(name);
	}

	public boolean contains(String fullClassName) {
		if (classes.contains(fullClassName)) {
			return true;
		}
		for (Iterator iterator = classLoaders.iterator(); iterator.hasNext();) {
			ClassLoader classLoader = (ClassLoader)iterator.next();
			if (classLoader == null) {
				continue;
			}
			try {
				if (classLoader.loadClass(fullClassName) != null) {
					add(fullClassName);
					return true;
				}
			}
			catch (ClassNotFoundException e) {
				// continue
			}
		}
		return false;
	}

	public Collection all() {
		return Collections.unmodifiableCollection(classes);
	}

	public void addClassLoader(ClassLoader classLoader) {
		classLoaders.add(classLoader);
	}

	public void addDefaultLoader() {
		if (!defaultLoaders) {
			classLoaders.add(getClass().getClassLoader());
			classLoaders.add(Thread.currentThread().getContextClassLoader());
		}
		defaultLoaders = true;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		classLoaders = new ArrayList();
		if (defaultLoaders) {
			defaultLoaders = false;
			addDefaultLoader();
		}
	}

}
