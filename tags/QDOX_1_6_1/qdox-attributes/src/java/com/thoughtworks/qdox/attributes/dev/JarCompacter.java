package com.thoughtworks.qdox.attributes.dev;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.thoughtworks.qdox.attributes.Bundle;
import com.thoughtworks.qdox.attributes.impl.AttributesImpl;

/**
 * Compacts attribute files within JAR files into one combined attribute file.
 * Note that this will probably <em>increase</em> the size of the JAR file,
 * but may improve runtime load performance, since only one file needs to
 * be deserialized, and class files will generally not need to be parsed.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class JarCompacter {

	public static final Bundle[] EMPTY_BUNDLE_ARRAY = new Bundle[0];

	public static void main(String[] args) {
		if (args.length == 0) {
			printUsage();
			return;
		}
		for (int i = 0; i < args.length; i++) {
			compactJar(args[i]);
		}
	}
	
	public static void compactJar(String fileName) {
		Map supertypes = new HashMap();
		List bundles = new ArrayList();
		
		ZipInputStream zin = null;
		ZipOutputStream zout = null;
			
		try {
			File originalFile = new File(fileName);
			File compactFile = File.createTempFile("cja", null, originalFile.getParentFile());
			compactFile.deleteOnExit();
			
			zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(originalFile)));
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(compactFile)));
			zout.setLevel(9);
			
			while(true) {
				ZipEntry entry = zin.getNextEntry();
				if (entry == null) break;
				if (entry.getName().endsWith(".class")) {
					// merge in attributes file
					CompactClassFile ccf = new CompactClassFile(zin);
					supertypes.put(ccf.getClassName(), ccf.getSupertypes());
					bundles.addAll(Arrays.asList(ccf.getBundles()));
					zout.putNextEntry(new ZipEntry(entry.getName()));
					ccf.save(zout);	// save class file with bundles removed
					zout.closeEntry();
				} else {
					// copy other file straight through to zip output
					zout.putNextEntry(entry);
					copyStreams(zin, zout);
					zout.closeEntry();
				}
				zin.closeEntry();
			}
			
			zin.close();
			zout.putNextEntry(new ZipEntry(AttributesImpl.AGGREGATED_FILENAME));
			ObjectOutputStream oout = new ObjectOutputStream(zout);
			oout.writeObject(supertypes);
			WriteAttributesPack.writeBundles(oout, (Bundle[]) bundles.toArray(EMPTY_BUNDLE_ARRAY));
			oout.close();
			// don't close entry, since closing oout automatically closes zout, and hence the entry
			// zout.closeEntry();
			
			if (!originalFile.delete()) {
				// try to clean up
				compactFile.delete();
				throw new IOException("Failed to delete original JAR.");
			}
			if (!compactFile.renameTo(originalFile)) {
				throw new IOException("Failed to rename compact JAR to original name; unfortunately, the original has already been deleted.");
			}
		} catch (Exception e) {
			System.err.println("Error processing " + fileName + ":");
			System.err.println(e);
			System.err.println();
		} finally {
			if (zin != null) try {
				zin.close();
			} catch (IOException e) {}
			if (zout != null) try {
				zout.close();
			} catch (IOException e) {}
		}
	}
	
	private static byte[] buffer = new byte[1024];
	private static void copyStreams(InputStream in, OutputStream out) throws IOException {
		while(true) {
			int len = in.read(buffer);
			if (len == -1) return;
			out.write(buffer, 0, len);
		}
	}

	public static void printUsage() {
		System.out.println("Usage:");
		System.out.println("  java com.thoughtworks.qdox.attributes.dev.JarCompacter <file.jar> <file2.jar> ...");
	}
}
