package com.thoughtworks.qdox.attributes.dev;

import java.io.*;
import java.util.*;

import com.thoughtworks.qdox.attributes.Attributes;

/**
 * A command-line attributes compiler.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class Compiler {
	
	private File[] sourceRoots, destinations;
	private String[] sources;
	private boolean force, verbose;
	
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
	
	private void processSource(File file, String slashedName) {
		if (verbose) System.out.println("Processing " + file);
		
		try {
			if (!force) {
				// find matching class file and see if it already has bundles 
				// if so, then it must be up-to-date, since a javac compile would've overwritten them
				if (new CheckClassFile(new FileInputStream(findClassFile(slashedName))).hasBundles()) return;
			}
			
			// otherwise, parse source file and write out attribs
			builder.parse(file);
			for (Iterator it = builder.getPacks().entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				final String className = (String) entry.getKey();
				WriteAttributesPack pack = (WriteAttributesPack) entry.getValue();
				if (pack.size() == 0) continue;
				pack.save(findClassFile(className.replace('.', File.separatorChar)));
			}
		} catch (IOException e) {
			System.err.println("Error processing file '" + file + "': " + e);
		} catch (ClassNotFoundException e) {
			assert false;
		}
	}
	
	private File findClassFile(String slashedName) throws IOException {
		slashedName += ".class";
		for (int i=0; i<destinations.length; i++) {
			File classFile = new File(destinations[i], slashedName);
			if (classFile.exists()) return classFile;
		}
		throw new IOException("no matching class file found");
	}
	
	public static void main(String[] args) {
		System.setProperty(Attributes.IMPL_CLASS_NAME_PROPKEY, CompileTimeAttributesImpl.class.getName());
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
