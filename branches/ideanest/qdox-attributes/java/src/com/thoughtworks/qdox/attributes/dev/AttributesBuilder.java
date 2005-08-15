package com.thoughtworks.qdox.attributes.dev;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import junit.framework.TestCase;

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
	private AttributesPack pack;
	private JFlexLexer lexer;
	private File currentFile;
	
	private Map packs = new HashMap();

	public AttributesBuilder(Mode mode) {
		this.mode = mode;
	}
	
	public void parse(File file) throws IOException {
		currentBundle = new SimpleBundle();
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
	
	private void recordBundle(String name) {
		assert currentBundle.size() > 0;
		pack.put(name, currentBundle);
		currentBundle = new SimpleBundle();
	}

	public void beginClass(ClassDef def) {
		if (typeResolver.isTopContext()) pack = new AttributesPack(null);
		String className = typeResolver.beginClass(def);
		if (currentBundle.size() > 0) recordBundle(className);
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
		
		recordBundle(typeResolver.getCurrentClassName() + "#" + buf.toString());
	}

	private void reportError(String string, Exception e) {
		System.err.println(string + " in " + currentFile + ":" + lexer.getLine());
		System.err.println(e);
		System.err.println();
	}

	public void addField(FieldDef def) {
		if (currentBundle.size() > 0) recordBundle(typeResolver.getCurrentClassName() + "#" + def.name);
	}

	public void addJavaDocTag(String tag, String text) {
		if (ignoredTags.contains(tag)) return;
		try {
			mode.processTag(tag, text, currentBundle, typeResolver);
		} catch (Exception e) {
			reportError("error processing tag " + tag, e);
		}
	}

	public void addImport(String importName) {
		typeResolver.addImport(importName);
	}

	public interface Mode {
		void processTag(String tag, String text, SimpleBundle bundle, TypeResolver typeResolver) throws Exception;
	}

	public static class StringMode implements Mode {
		public void processTag(String tag, String text, SimpleBundle bundle, TypeResolver typeResolver) {
			bundle.add(tag, text);
		}
	}
	
	public static class ObjectMode implements Mode {

		public void processTag(String tag, String text, SimpleBundle bundle, TypeResolver typeResolver) throws ClassNotFoundException, TagParseException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Class klass;
			try {
				if (tag.endsWith("Attribute")) {
					klass = typeResolver.resolve(tag);
				} else {
					try {
						klass = typeResolver.resolve(tag + "Attribute");
					} catch (ClassNotFoundException e) {
						klass = typeResolver.resolve(tag);
					}
				}
				AttributeCreator creator = AttributeCreatorFactory.make(klass, text);
				Object attribute = creator.create();
				if (attribute instanceof java.io.Serializable) {
					bundle.add(attribute);
				} else {
					bundle.add(attribute, creator);
				}
			} catch (ClassNotFoundException e) {
				tagClassNotFound(tag, text, bundle);
			}
		}
		
		protected void tagClassNotFound(String tag, String text, SimpleBundle bundle) throws TagParseException {
			throw new TagParseException("no class found for tag " + tag);
		}
		
		/**
		 * @deprecated Test class that should not be javadoc'ed.
		 */
		public static class Test extends TestCase {
			private ObjectMode mode;
			public void setUp() {
				mode = new ObjectMode();
			}
			public void tearDown() {
				mode = null;
			}
			public void testTagAttributeSuffix() throws Exception {
				SimpleBundle bundle = new SimpleBundle();
				mode.processTag("com.thoughtworks.qdox.attributes.Simple", "hello bye", bundle, new TypeResolver());
				assertEquals("bye", bundle.get("hello"));
			}
			public void testTagAttributeExplicitSuffix() throws Exception {
				SimpleBundle bundle = new SimpleBundle();
				mode.processTag("com.thoughtworks.qdox.attributes.SimpleAttribute", "hello bye", bundle, new TypeResolver());
				assertEquals("bye", bundle.get("hello"));
			}
		}
	}
	
	public static class MixedMode extends ObjectMode {
		protected void tagClassNotFound(String tag, String text, SimpleBundle bundle) {
			bundle.add(tag, text);
		}
	}


}