package com.thoughtworks.qdox.attributes.dev;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.*;
import com.thoughtworks.qdox.attributes.impl.*;
import com.thoughtworks.qdox.parser.Builder;
import com.thoughtworks.qdox.parser.impl.JFlexLexer;
import com.thoughtworks.qdox.parser.impl.Parser;
import com.thoughtworks.qdox.parser.structs.*;

/**
 * QDox Builder implementation for creating Properties containing attributes.
 *<p>
 * An AttributesBuilder can only be used to parse one file at a time.  Call the <code>reset()</code>
 * method before reusing an instance.
 * <p>
 * Based on a similar class written by Joe Walnes.
 *
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class AttributesBuilder implements Builder {
	
	private static final Set JAVADOC_TAGS_SRC = new HashSet(Arrays.asList(new String[] {
		"author", "deprecated", "exception", "param", "return", "see",
		"serial", "serialData", "serialField", "since", "throws", "version"
	}));
	public static final Set JAVADOC_TAGS = Collections.unmodifiableSet(JAVADOC_TAGS_SRC);

	private Mode mode;
	private TypeResolver typeResolver = new TypeResolver();
	private Set ignoredTags = new HashSet();
	
	private SimpleBundle currentBundle;
	private ErrorBundle errorBundle;
	private WriteAttributesPack pack;
	private JFlexLexer lexer;
	private File currentFile;
	
	private Map packs = new HashMap();

	public AttributesBuilder(Mode mode) {
		setMode(mode);
	}
	
	public void parse(File file) throws IOException {
		currentBundle = new SimpleBundle();
		errorBundle = new ErrorBundle();
		pack = null;
		typeResolver.reset();
		packs.clear();
		
		currentFile = file;
		InputStream input = null;
		try {
			input = new BufferedInputStream(new FileInputStream(file));
			lexer = new JFlexLexer(input);
			new Parser(lexer, this).parse();
		} finally {
			if (input != null) try {
				input.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	public Map getPacks() {
		return packs;
	}
	
	public void ignore(String tag) {
		ignoredTags.add(tag);
	}

	public void ignoreAll(Collection tags) {
		ignoredTags.addAll(tags);	
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
		mode.setTypeResolver(typeResolver);
	}

	public void addPackage(String packageName) {
		typeResolver.addPackage(packageName);
	}

	public void addJavaDoc(String text) {
	}

	public void endClass() {
		String className = typeResolver.endClass();
		if (typeResolver.isTopContext()) {
			// top-level class, save attributes pack in map and setup for another class
			packs.put(className, pack);
			pack = null;
			// just in case, reset current bundle to get rid of any unassigned javadoc comments
			currentBundle = new SimpleBundle();
		}
	}
	
	private void recordBundle(ElementType target, String name) {
		assert currentBundle.size() > 0;
		boolean errors = false;
		for (Iterator it = currentBundle.iterator(); it.hasNext(); ) {
			Object attribute = it.next();
			try {
				verifyAttribute(attribute, target);
			} catch (Exception e) {
				errorBundle.addError(e);
				errors = true;
			}
		}
		if (errors) {
			pack.put(name, errorBundle);
			System.err.print(errorBundle.getErrorMessage());
			System.err.println("(in file " + currentFile + ")");
			errorBundle = new ErrorBundle();
		} else if (currentBundle.size() > 0) {
			pack.put(name, currentBundle);
		}
		if (currentBundle.size() > 0) currentBundle = new SimpleBundle();
	}
	
	private void verifyAttribute(Object attribute, ElementType target) {
		AttributeUsageAttribute usage = AttributeUsageAttribute.of(attribute);
		if (!usage.allowsTarget(target)) throw new IllegalArgumentException("attribute type " + attribute.getClass().getName() + " cannot be applied to " + target);
		if (!usage.getAllowMultiple()) {
			String usageDefiningClassName = (String) Attributes.getInstance().get(attribute.getClass()).getProvenanceMap().get(usage);
			assert usageDefiningClassName != null : "attribute usage attribute applied to a package";
			try {
				Class usageRestrictedClass = typeResolver.resolve(usageDefiningClassName);
				Iterator it = currentBundle.iterator(usageRestrictedClass);
				assert it.hasNext() : "current bundle has no attribute of given class, should be at least one";
				it.next();
				if (it.hasNext()) throw new IllegalArgumentException("multiple values of attribute class " + usageDefiningClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("unable to load ancestor class", e);
			}
		}
	}

	public void beginClass(ClassDef def) {
		if (typeResolver.isTopContext()) pack = new WriteAttributesPack();
		String className = typeResolver.beginClass(def);
		if (currentBundle.size() > 0) recordBundle(def.isInterface ? ElementType.INTERFACE : ElementType.CLASS, className);
	}
	
	public void addMethod(MethodDef def) {
		if (currentBundle.size() == 0) return;
		
		StringBuffer buf = new StringBuffer();
		if (!def.constructor) buf.append(def.name);
		buf.append('(');
		for (Iterator it = def.params.iterator(); it.hasNext();) {
			FieldDef param = (FieldDef) it.next();
			try {
				buf.append(typeResolver.resolve(param.type, param.dimensions).getName());
			} catch (ClassNotFoundException e) {
				reportError("error resolving method formal parameter type " + param.type, e);
				return;
			}
			if (it.hasNext()) buf.append(',');
		}
		buf.append(')');
		
		recordBundle(
			def.constructor ? ElementType.CONSTRUCTOR : ElementType.METHOD,
			typeResolver.getCurrentClassName() + "#" + buf.toString() + (def.modifiers.contains("private") ? ";private" : "")
		);
	}

	private void reportError(String string, Exception e) {
		System.err.println(string + " in " + currentFile + " around line " + lexer.getLine());
		System.err.println(e);
		System.err.println();
	}

	public void addField(FieldDef def) {
		if (currentBundle.size() > 0) recordBundle(ElementType.FIELD, typeResolver.getCurrentClassName() + "#" + def.name);
	}

	public void addJavaDocTag(String tag, String text, int lineNumber) {
		// TODO: store line number for error reporting
		if (ignoredTags.contains(tag) || tag.indexOf('{') != -1) return;
		try {
			mode.processTag(tag, text, currentBundle);
		} catch (Exception e) {
			reportError("error processing tag " + tag, e);
		}
	}

	public void addImport(String importName) {
		typeResolver.addImport(importName);
	}

	/**
	 * Defines the mode strategy interface for the attributes builder.
	 * 
	 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
	 * @version $Revision$ ($Date$)
	 */
	public interface Mode {
		/**
		 * This method will be called for each tag encountered in the source input.
		 * @param tag the tag name, stripped of the leading '@'
		 * @param text everything following the tag, stripped of leading and lagging whitespace
		 * @param bundle the bundle that the attribute described by this tag should be added to
		 * @throws Exception if the tag to attribute conversion fails in any way
		 */
		void processTag(String tag, String text, SimpleBundle bundle) throws Exception;
		
		/**
		 * This method will be called once to make a type resolver available to the mode
		 * strategy.  It will be called before the mode is asked to process any tags.
		 * 
		 * @param resolver an appropriate type resolver
		 */
		void setTypeResolver(TypeResolver resolver);
	}

	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class TestVerifyAttribute extends TestCase {
		private AttributesBuilder builder;
		protected void setUp() {
			builder = new AttributesBuilder(new ObjectMode());
			builder.currentBundle = new SimpleBundle();
		}
		protected void tearDown() {
			builder = null;
		}
		public void testRightTarget() {
			Object attr = new AttributeUsageAttribute(AttributeUsageAttribute.MEMBERS);
			builder.currentBundle.add(attr);
			builder.verifyAttribute(attr, ElementType.CLASS);
		}
		public void testWrongTarget() {
			try {
				Object attr = new AttributeUsageAttribute(AttributeUsageAttribute.MEMBERS);
				builder.currentBundle.add(attr);
				builder.verifyAttribute(attr, ElementType.METHOD);
				fail();
			} catch (IllegalArgumentException e) {
			}
		}
		public void testBadMultiple() {
			try {
				Object attr = new AttributeUsageAttribute(AttributeUsageAttribute.MEMBERS);
				builder.currentBundle.add(attr);
				builder.currentBundle.add(attr);
				builder.verifyAttribute(attr, ElementType.CLASS);
				fail();
			} catch (IllegalArgumentException e) {
			}
		}
	}
}