package com.thoughtworks.qdox.attributes.impl;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.qdox.attributes.Bundle;

/**
 * A package of attributes for any number of program elements that is stored as a unit.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class AttributesPack {
	private static final Logger log = Logger.getLogger(AttributesPack.class.getName());
	
	private final ClassLoader classLoader;
	private final boolean doAfterLoad;
	private final Map elements = new HashMap();
	private final Set loadedURLs = new HashSet();
	
	public AttributesPack(ClassLoader classLoader, boolean doAfterLoad) {
		this.classLoader = classLoader;
		this.doAfterLoad = doAfterLoad;
	}
	
	public synchronized int size() {
		return elements.size();
	}
	
	public synchronized void save(File file) throws IOException {
		file.getParentFile().mkdirs();
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		try {
			save(out);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				log.log(Level.FINE, "failed to close file", e);
			}
		}
	}

	public void save(ObjectOutputStream out) throws IOException {
		// we filter out empty bundles, so must calculate number of non-empty ones manually
		int size = 0;
		for (Iterator it = elements.values().iterator(); it.hasNext(); ) {
			BundleBase bundle = (BundleBase) it.next();
			if (bundle.size() > 0) size++;
		}
		out.writeInt(size);
		for (Iterator it = elements.values().iterator(); it.hasNext();) {
			BundleBase bundle = (BundleBase) it.next();
			if (bundle.size() != 0) out.writeObject(bundle);
		}
	}
	
	public synchronized void merge(String filename) {
		Enumeration enum;
		try {
			if (classLoader == null) enum = ClassLoader.getSystemResources(filename);
			else enum = classLoader.getResources(filename);
		} catch (IOException e) {
			log.log(Level.SEVERE, "error while finding attribute resources called " + filename, e);
			return;
		}
		while(enum.hasMoreElements()) {
			URL url = (URL) enum.nextElement();
			if (loadedURLs.contains(url)) continue;
			try {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(url.openStream()));
				try {
					merge(in);
					loadedURLs.add(url);
				} catch (IOException e) {
					log.log(Level.SEVERE, "error while reading attributes from " + url, e);
				} catch (ClassNotFoundException e) {
					log.log(Level.SEVERE, "error while reading attributes from " + url, e);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						log.log(Level.FINE, "failed to close attribute file ", e);
					}
				}
			} catch (IOException e) {
				log.log(Level.SEVERE, "failed to open attribute file " + url, e);
			}
		}
	}

	public synchronized void merge(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int n = in.readInt();
		while(n-- > 0) {
			BundleBase bundle = (BundleBase) in.readObject();
			if (doAfterLoad) bundle.afterLoad();
			BundleBase old = (BundleBase) elements.put(bundle.getKey(), bundle);
			if (old != null) {
				// if old bundle is of size 0, then it must've been synthesized on request, the attribute got compiled later,
				// and now we're reading in its definition; emit a warning that things might not be up-to-date
				// TODO: track dependencies and recompile the appropriate bundles
				if (old.size() == 0) log.log(Level.WARNING, "attribute used before it was compiled " + bundle.getKey());
				else log.log(Level.WARNING, "multiple attribute bundles found for " + bundle.getKey());
			}
		}
	}
	
	public synchronized Bundle get(String key) {
		BundleBase bundle = (BundleBase) elements.get(key);
		if (bundle == null) put(key, bundle = new SimpleBundle());
		return bundle;
	}
	
	public synchronized void put(String key, BundleBase bundle) {
		assert !elements.containsKey(key);
		bundle.setKey(key);
		elements.put(key, bundle);
	}

}
