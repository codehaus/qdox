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
	private final boolean clearCreators;
	private final Map elements = new HashMap();
	private final Set loadedURLs = new HashSet();
	
	public AttributesPack(ClassLoader classLoader, boolean clearCreators) {
		this.classLoader = classLoader;
		this.clearCreators = clearCreators;
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
		for (Iterator it = elements.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry pair = (Map.Entry) it.next();
			if (((SimpleBundle) pair.getValue()).size() > 0) size++;
		}
		out.writeInt(size);
		for (Iterator it = elements.entrySet().iterator(); it.hasNext();) {
			Map.Entry pair = (Map.Entry) it.next();
			if (((SimpleBundle) pair.getValue()).size() == 0) continue;
			out.writeUTF((String) pair.getKey());
			out.writeObject(pair.getValue());
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
			String key = in.readUTF();
			SimpleBundle bundle = (SimpleBundle) in.readObject();
			if (clearCreators) bundle.clearCreators();
			Object old = elements.put(key, bundle);
			if (old != null) log.log(Level.WARNING, "multiple attribute bundles found for " + key);
		}
	}
	
	public synchronized Bundle get(String key) {
		SimpleBundle bundle = (SimpleBundle) elements.get(key);
		if (bundle == null) {
			bundle = new SimpleBundle();
			elements.put(key, bundle);
		}
		return bundle;
	}
	
	public synchronized void put(String key, SimpleBundle bundle) {
		assert !elements.containsKey(key);
		elements.put(key, bundle);
	}

}
