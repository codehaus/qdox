package com.thoughtworks.qdox.attributes.dev;

import java.io.*;
import java.util.zip.*;

import com.thoughtworks.qdox.attributes.impl.AttributesPack;
import com.thoughtworks.qdox.attributes.impl.SimpleAttributesImpl;

/**
 * Compacts attribute files within JAR files into one combined attribute file.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class JarCompacter {

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
		AttributesPack pack = new AttributesPack(null, false);
		ZipInputStream zin = null;
		ZipOutputStream zout = null;
			
		try {
			File originalFile = new File(fileName);
			File compactFile = File.createTempFile("cja", null, originalFile.getParentFile());
			compactFile.deleteOnExit();
			
			zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(originalFile)));
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(compactFile)));
			
			while(true) {
				ZipEntry entry = zin.getNextEntry();
				if (entry == null) break;
				if (entry.getName().endsWith(SimpleAttributesImpl.FILENAME_SUFFIX)) {
					// merge in attributes file
					pack.merge(new ObjectInputStream(zin));
				} else {
					// copy other file straight through to zip output
					zout.putNextEntry(entry);
					copyStreams(zin, zout);
					zout.closeEntry();
				}
				zin.closeEntry();
			}
			
			zin.close();
			ZipEntry entry = new ZipEntry(SimpleAttributesImpl.COALESCED_FILENAME);
			zout.setLevel(9);
			zout.putNextEntry(entry);
			ObjectOutputStream oout = new ObjectOutputStream(zout);
			pack.save(oout);
			zout.closeEntry();
			oout.close();
			
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
