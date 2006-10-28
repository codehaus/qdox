package com.thoughtworks.qdox.attributes.impl;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.thoughtworks.qdox.attributes.Bundle;

/**
 * A package of attributes for any number of program elements that is stored as a unit.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ReadAttributesPack extends AttributesPackBase {
	private final ClassLoader classLoader;
	private final boolean doAfterLoad;
	private final Map supertypes = new HashMap();
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	public ReadAttributesPack(ClassLoader classLoader, boolean doAfterLoad) {
		this.classLoader = classLoader;
		this.doAfterLoad = doAfterLoad;
	}
	
	public synchronized Bundle get(String className, String suffix) {
		if (ensureClassLoaded(className)) {
			final String key = className + suffix;
			BundleBase bundle = (BundleBase) elements.get(key);
			if (bundle == null) put(key, bundle = new SimpleBundle());	// not static since key will get set
			return bundle;
		} else {
			ErrorBundle bundle = new ErrorBundle();
			bundle.setKey(className);
			bundle.addError("no class with that name found");
			return bundle;
		}
	}

	public synchronized Bundle getIfExists(String key) {
		return (Bundle) elements.get(key);
	}

	public synchronized String[] getSupertypes(String className) {
		return ensureClassLoaded(className) ?(String[]) supertypes.get(className) : EMPTY_STRING_ARRAY;
	}
	
	public synchronized boolean ensureClassLoaded(String className) {
		if (!supertypes.containsKey(className)) mergeClass(className);
		return supertypes.containsKey(className);
	}
	
	public synchronized void mergeClass(String className) {
		String filename = className.replace('.','/') + ".class";
		Enumeration enumVar = getResources(filename);
		while(enumVar.hasMoreElements()) {
			URL url = (URL) enumVar.nextElement();
			try {
				ReadClassFile rcf = new ReadClassFile(url.openStream(), true);
				supertypes.put(className, rcf.getSupertypes());
				mergeBundles(rcf.getBundles());
			} catch (IOException e) {
				System.err.println("error while reading attributes from " + url);
				e.printStackTrace(System.err);
			} catch (ClassNotFoundException e) {
				System.err.println("error while reading attributes from " + url);
				e.printStackTrace(System.err);
			}
		}
	}
	
	public synchronized void mergeAggregateFile(String filename) {
		Enumeration enumVar = getResources(filename);
		while(enumVar.hasMoreElements()) {
			URL url = (URL) enumVar.nextElement();
			try {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(url.openStream()));
				supertypes.putAll((Map) in.readObject());
				mergeBundles(readBundles(in));
			} catch (IOException e) {
				System.err.println("error while reading attributes from " + url);
				e.printStackTrace(System.err);
			} catch (ClassNotFoundException e) {
				System.err.println("error while reading attributes from " + url);
				e.printStackTrace(System.err);
			}
		}
	}
	
	protected synchronized Enumeration getResources(String filename) {
		Enumeration enumVar;
		try {
			if (classLoader == null) enumVar = ClassLoader.getSystemResources(filename);
			else enumVar = classLoader.getResources(filename);
		} catch (IOException e) {
			System.err.println("error while finding attribute resources called " + filename);
			e.printStackTrace(System.err);
			enumVar = Collections.enumeration(Collections.EMPTY_LIST);
		}
		return enumVar;
	}

	private synchronized void mergeBundles(BundleBase[] bundles) {	
		for (int i = 0; i < bundles.length; i++) {
			BundleBase bundle = bundles[i];
			if (doAfterLoad) bundle.afterLoad();
			BundleBase old = (BundleBase) elements.put(bundle.getKey(), bundle);
			if (old != null) {
				// if old bundle is of size 0, then it must've been synthesized on request, the attribute got compiled later,
				// and now we're reading in its definition; emit a warning that things might not be up-to-date
				// TODO: track dependencies and recompile the appropriate bundles
				if (old.size() == 0) System.err.println("attribute used before it was compiled " + bundle.getKey());
				else System.err.println("multiple attribute bundles found for " + bundle.getKey());
			}						
		}
	}

	public static BundleBase[] readBundles(ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			int n = in.readInt();
			BundleBase[] bundles = new BundleBase[n];
			for(int i=0; i < n; i++) bundles[i] = (BundleBase) in.readObject();
			return bundles;
		} finally {
			try {
				in.close();	// this had better not throw an exception!
			} catch (IOException e) {
			}
		}
	}

}
