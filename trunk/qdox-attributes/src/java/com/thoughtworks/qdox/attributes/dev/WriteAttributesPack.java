package com.thoughtworks.qdox.attributes.dev;

import java.io.*;
import java.util.*;

import com.thoughtworks.qdox.attributes.Bundle;
import com.thoughtworks.qdox.attributes.impl.*;

/**
 * An attributes pack meant to be constructed for a single class, to write out its attributes
 * bundle.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class WriteAttributesPack extends AttributesPackBase {

	public WriteAttributesPack() {
	}
	
	public WriteAttributesPack(ReadAttributesPack original) {
		super(original);
	}

	public synchronized void save(File file) throws IOException {
		try {
			WriteClassFile wcf = new WriteClassFile(new FileInputStream(file), true);
			List nonEmptyBundles = new ArrayList();
			for (Iterator it = elements.values().iterator(); it.hasNext(); ) {
				BundleBase bundle = (BundleBase) it.next();
				if (bundle.size() > 0) nonEmptyBundles.add(bundle);
			}
			wcf.setBundles((BundleBase[]) nonEmptyBundles.toArray(ReadClassFile.EMPTY_BUNDLE_ARRAY));
			wcf.save(new FileOutputStream(file));
		} catch (ClassNotFoundException e) {
			assert false;
		}
	}

	public static void writeBundles(ObjectOutputStream out, Bundle[] bundles) throws IOException {
		out.writeInt(bundles.length);
		for (int i=0; i<bundles.length; i++) out.writeObject(bundles[i]);
	}

}
