package com.thoughtworks.qdox.attributes.dev;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.thoughtworks.qdox.attributes.Attributes;
import com.thoughtworks.qdox.attributes.impl.*;

/**
 * A command-line attributes compiler.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class Compiler {
	
	private File[] sourceRoots, destinations;
	private String[] sources;
	private boolean force, cleanup = true, verbose;
	
	private Set matchedAttributeFiles = new HashSet();
	
	private AttributesBuilder builder = new AttributesBuilder(new MixedMode());
	
	public Compiler() {}
	
	public void setSourceRoots(File[] sourceRoots) {
		this.sourceRoots = sourceRoots;
	}

	public void setDestinations(File[] destinations) {
		this.destinations = destinations;
	}
	
	public void setSources(String[] sources) {
		this.sources = sources;
	}
		
	public void setForce(boolean force) {
		this.force = force;
	}
	
	public void setCleanup(boolean cleanup) {
		this.cleanup = cleanup;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public AttributesBuilder getBuilder() {
		return builder;
	}
	
	public void execute() {
		if (sourceRoots == null) sourceRoots = new File[] {new File(System.getProperty("user.dir"))};
		if (destinations == null) destinations = sourceRoots;
		
		if (verbose) {
			System.out.println("Effective source roots: " + Arrays.asList(sourceRoots));
			System.out.println("Effective destinations: " + Arrays.asList(destinations));
		}

		if (sources == null) scanSourceRoots(); else scanSources();
		if (cleanup) {
			if (sources == null) pruneSourceRoots(); else pruneSources();
		} 
	}
	
	private void pruneSources() {
		for (int i = 0; i < sources.length; i++) {
			String source = sources[i];
			if (source.endsWith(".java")) continue;
			for (int j = 0; j < destinations.length; j++) {
				File file = new File(destinations[j], source);
				if (file.exists() && file.isDirectory()) pruneRecursive(file);
			}
		}
	}
	
	private void pruneSourceRoots() {
		for (int i=0; i<destinations.length; i++) {
			pruneRecursive(destinations[i]);
		}
	}
	
	private void pruneRecursive(File dir) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				pruneRecursive(file);
			} else if (file.getName().endsWith(SimpleAttributesImpl.FILENAME_SUFFIX)) {
				if (!matchedAttributeFiles.contains(file)) file.delete();
			}			
		}
	}
	
	private void scanSources() {
		for (int i = 0; i < sources.length; i++) {
			String source = sources[i];
			for (int j = 0; j < sourceRoots.length; j++) {
				File file = new File(sourceRoots[j], source);
				if (!file.exists()) continue;
				if (file.isFile() && source.endsWith(".java")) {
					processSource(file, source.substring(0, source.length()-5));
				} else if (file.isDirectory()) {
					scanRecursive(file, source);
				} 
			}
		}
	}
	
	private void scanSourceRoots() {
		for (int i=0; i<sourceRoots.length; i++) {
			if (!sourceRoots[i].exists()) {
				System.err.println("source root does not exist: " + sourceRoots[i]);
			} else if (!sourceRoots[i].isDirectory()) {
				System.err.println("source root is not a directory: " + sourceRoots[i]);
			} else  {
				scanRecursive(sourceRoots[i], "");
			}
		}
	}
	
	private void scanRecursive(File dir, String packagePrefix) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String name = file.getName();
			if (file.isDirectory()) {
				String fullName = (packagePrefix.length() == 0 ? "" : packagePrefix + File.separatorChar) + name;
				scanRecursive(file, fullName);
			} else {
				int extIndex = name.lastIndexOf('.');
				if (extIndex == -1 || !name.substring(extIndex).equals(".java")) continue;
				name = name.substring(0, extIndex);
				if (name.length() == 0) continue;
				String fullName = (packagePrefix.length() == 0 ? "" : packagePrefix + File.separatorChar) + name;
				processSource(file, fullName);
			}			
		}
	}
	
	private void processSource(File file, String qualifiedName) {
		if (verbose) System.out.println("Processing " + file);
		
		// 1. find matching attribs file
		File destPath;
		FIND_CLASS: {
			for (int i=0; i<destinations.length; i++) {
				destPath = destinations[i];
				File classFile = new File(destPath, qualifiedName + ".class");
				if (classFile.exists()) break FIND_CLASS;
			}
			destPath = (File) destinations[0];
		}
		File attribsFile = new File(destPath, qualifiedName + SimpleAttributesImpl.FILENAME_SUFFIX);
		
		// 2. compare timestamps, skip if up-to-date
		if (!force && attribsFile.exists() && attribsFile.lastModified() >= file.lastModified()) {
			if (cleanup) matchedAttributeFiles.add(attribsFile);
			return;
		}
		if (attribsFile.exists()) attribsFile.delete();
		
		// 3. parse source file and write out attribs
		try {
			builder.parse(file);
			for (Iterator it = builder.getPacks().entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				AttributesPack pack = (AttributesPack) entry.getValue();
				if (pack.size() == 0) continue;
				File outFile = new File(destPath, ((String) entry.getKey()).replace('.', File.separatorChar) + SimpleAttributesImpl.FILENAME_SUFFIX);
				if (cleanup && !attribsFile.equals(outFile)) {
					System.err.println("Warning: file '" + file + "' contains class '" + entry.getKey() + "'; you must run the compiler in nocleanup mode.");
					System.err.println(">>> mismatch: " + attribsFile + " vs. " + outFile);
				}
				pack.save(outFile);
				if (cleanup) matchedAttributeFiles.add(outFile);
			}
		} catch (IOException e) {
			System.err.println("Error processing file '" + file + "': " + e);
		}
	}
	
	public static void main(String[] args) {
		System.setProperty(Attributes.SIMPLE_IMPL_CLASS_NAME_PROPKEY, CompileTimeAttributesImpl.class.getName());
		Compiler c = new Compiler();
		try {
			int k = 0;
			boolean ignoreSpecified = false;
			while (k < args.length) {
				String arg = args[k];
				if (!arg.startsWith("-")) break;
				k++;
				if ("-src".equals(arg)) {
					c.setSourceRoots(parseDirs(args[k++]));
				} else if ("-dst".equals(arg)) {
					c.setDestinations(parseDirs(args[k++]));
				} else if ("-mode".equals(arg)) {
					String kind = args[k++];
					if ("string".equals(kind)) {
						c.getBuilder().setMode(new StringMode());
					} else if ("object".equals(kind)) {
						c.getBuilder().setMode(new ObjectMode());
					} else if ("mixed".equals(kind)) {
						c.getBuilder().setMode(new MixedMode());
					} else {
						throw new IllegalArgumentException("unknown mode " + kind);
					}
				} else if ("-ignore".equals(arg)) {
					c.getBuilder().ignoreAll(parseTags(args[k++]));
					ignoreSpecified = true;
				} else if ("-force".equals(arg)) {
					c.setForce(true);
				} else if ("-nocleanup".equals(arg)) {
					c.setCleanup(false);
				} else if ("-verbose".equals(arg)) {
					c.setVerbose(true);
				} else if ("-help".equals(arg)) {
					printUsage();
					return;
				} else {
					throw new IllegalArgumentException("unknown option " + arg);
				}
			}
			if (!ignoreSpecified) c.getBuilder().ignoreAll(AttributesBuilder.JAVADOC_TAGS);

			// no more switches, all that's left is source filenames
			if (k < args.length) {
				String[] sources = new String[args.length - k];
				System.arraycopy(args, k, sources, 0, sources.length);
				c.setSources(sources);
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println();
			printUsage();
			return;
		}
		c.execute();
	}
	
	private static void printUsage() {
		System.out.println("Usage:  java com.ideanest.attributes.Compiler [options] <source files and directories>");
		System.out.println("Options are:");
		System.out.println("  -src <sourceRootPaths, separated as for classpath>");
		System.out.println("  -dst <destinationRootPaths, separated as for classpath>");
		System.out.println("  -mode <string|object|mixed>");
		System.out.println("  -ignore <tags to ignore, separated by commas> (if not specified, ignore all standard javadoc tags)");
		System.out.println("  -force");
		System.out.println("  -nocleanup");
		System.out.println("  -verbose");
		System.out.println("  -help");
	}
	
	private static File[] parseDirs(String str) {
		List dirs = new ArrayList();
		StringTokenizer st = new StringTokenizer(str, File.pathSeparator);
		while (st.hasMoreTokens()) {
			File dir = new File(st.nextToken());
			dirs.add(dir);
		}
		return (File[]) dirs.toArray(EMPTY_FILE_ARRAY);
	}
	
	private static Collection parseTags(String str) {
		List tags = new ArrayList();
		StringTokenizer st = new StringTokenizer(str, ",");
		while (st.hasMoreTokens()) {
			tags.add(st.nextToken());
		}
		return tags;
	}
	private static final File[] EMPTY_FILE_ARRAY = new File[0];
}
