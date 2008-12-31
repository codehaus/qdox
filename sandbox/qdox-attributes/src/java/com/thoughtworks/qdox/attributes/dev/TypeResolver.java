package com.thoughtworks.qdox.attributes.dev;

import java.util.*;

import junit.framework.TestCase;

import com.thoughtworks.qdox.parser.structs.ClassDef;

/**
 * Resolves type names found in Java source files, according to Java type resolution rules.
 * Normally this algorithm is implemented by a Java compiler, but sometimes we want to
 * know what fully qualified typename a typename mentioned locally corresponds to.
 * <p>
 * Instances of this class are not thread-safe. 
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class TypeResolver {

	private List qualifiedClassNameStack = new LinkedList();
	private List simpleClassNameStack = new LinkedList();
	private String packageName;
	private Set classImports = new HashSet();
	private Set wildcardImports = new HashSet();
	
	private static class PrimitiveType {
		public Class klass;
		public char code;
		public PrimitiveType(Class klass, char code) {
			this.klass = klass;
			this.code = code;
		}
	}
	private static final Map primitiveTypes = new HashMap();
	static {
		primitiveTypes.put("boolean", new PrimitiveType(boolean.class, 'Z'));
		primitiveTypes.put("byte", new PrimitiveType(byte.class, 'B'));
		primitiveTypes.put("char", new PrimitiveType(char.class, 'C'));
		primitiveTypes.put("double", new PrimitiveType(double.class, 'D'));
		primitiveTypes.put("float", new PrimitiveType(float.class, 'F'));
		primitiveTypes.put("int", new PrimitiveType(int.class, 'I'));
		primitiveTypes.put("long", new PrimitiveType(long.class, 'J'));
		primitiveTypes.put("short", new PrimitiveType(short.class, 'S'));
	}
	
	public TypeResolver() {
		reset();
	}
	
	/**
	 * Reset this type resolver to a virgin state, as if though newly instantiated.
	 */
	public void reset() {
		packageName = "";
		qualifiedClassNameStack.clear();
		simpleClassNameStack.clear();
		classImports.clear();
		wildcardImports.clear();
		wildcardImports.add("java.lang");
	}

	/**
	 * Set the package name for the source file being parsed.  Can only be called once
	 * between resets.
	 * @param packageName the package name of the current source file
	 * @throws IllegalStateException if the package name was already set
	 */
	public void addPackage(String packageName) {
		if (this.packageName.length() != 0) throw new IllegalStateException("package already set");
		this.packageName = packageName;
	}

	/**
	 * Mark the end of a class definition.
	 * @return the fully qualified name of the class who definition was just ended
	 * @throws IllegalStateException if no class is currently being defined
	 */
	public String endClass() {
		if (qualifiedClassNameStack.isEmpty()) throw new IllegalStateException("not in a class");
		simpleClassNameStack.remove(0);
		return (String) qualifiedClassNameStack.remove(0);
	}
	
	/**
	 * Mark the beginning of a class definition.
	 * @param def the class header, giving its name and other details
	 * @return the fully qualified name of the class whose definition was just begun
	 */
	public String beginClass(ClassDef def) {
		String qualifiedClassName = "";
		if (qualifiedClassNameStack.isEmpty()) {
			if (packageName.length() > 0) qualifiedClassName = packageName + ".";
		} else {
			qualifiedClassName = ((String) qualifiedClassNameStack.get(0)) + "$";
		}
		qualifiedClassName += def.name;
		qualifiedClassNameStack.add(0, qualifiedClassName);
		simpleClassNameStack.add(0, def.name);
		return qualifiedClassName;
	}
	
	/**
	 * Check whether we are currently defining a class.
	 * @return <code>true</code> if there is no class definition currently under way, <code>false</code> otherwise
	 */
	public boolean isTopContext() {
		return qualifiedClassNameStack.isEmpty();
	}

	/**
	 * Get the fully qualified name of the class currently being defined. 
	 * @return the fully qualified name of the class currently being defined
	 * @throws IllegalStateException if there is no class currently being defined
	 */
	public String getCurrentClassName() {
		if (qualifiedClassNameStack.isEmpty()) throw new IllegalStateException("not in a class");
		return (String) qualifiedClassNameStack.get(0);
	}

	/**
	 * Resolve a local type name to a type.  This takes into consideration the local
	 * context with appropriate scoping rules, the package of which the current source
	 * code is a member, and any import declarations that have been made.  If the
	 * target type is a class, it must be present on the thread's classloader's classpath.
	 * This method does not work for array types.
	 * 
	 * @param typeName a type name, qualified or not, primitive or not
	 * @return the type to which the given type name resolves in this context
	 * @throws ClassNotFoundException if the type name could not be resolved
	 * @see #resolve(String,int)
	 */
	public Class resolve(String typeName) throws ClassNotFoundException {
		// 1. if typeName is primitive, return the primitive type immediately
		PrimitiveType primType = (PrimitiveType) primitiveTypes.get(typeName);
		if (primType != null) return primType.klass;
		
		Class klass = null;
		
		// 2. attempt to locate first part of name as simple type using all possible contexts
		String firstPart = typeName;
		final int firstDotIndex = typeName.indexOf('.');
		if (firstDotIndex != -1) firstPart = typeName.substring(0, firstDotIndex);
		{
			String firstPartQualified = findSimpleType(firstPart);
			if (firstPartQualified != null) {
				// found a match, everything following the first part must be a nested class
				if (firstDotIndex != -1) firstPartQualified += typeName.substring(firstDotIndex).replace('.', '$');
				klass = load(firstPartQualified);
				if (klass == null) throw new ClassNotFoundException("class name " + firstPartQualified + " resolving type " + typeName + " not found");
				return klass;
			}
		}
		
		// 3. if first part is not a type, it must be a package; find first element that is a type, then convert rest to nested classes
		if (firstDotIndex == -1) throw new ClassNotFoundException("cannot resolve simple type " + typeName);
		int dotIndex = firstDotIndex;
		while(dotIndex != -1) {
			dotIndex = typeName.indexOf('.', dotIndex+1);
			String fullName = dotIndex == -1 ? typeName : typeName.substring(0, dotIndex);
			klass = load(fullName);
			if (klass != null) {
				if (dotIndex != -1) fullName += typeName.substring(dotIndex).replace('.', '$');
				klass = load(fullName);
				if (klass == null) throw new ClassNotFoundException("class name " + fullName + " resolving type " + typeName + " not found");
				return klass;
			}
		}
		throw new ClassNotFoundException("cannot resolve qualified type " + typeName);
	}
	
	/**
	 * Find a simple type according to name scoping rules.
	 * @param simpleTypeName the name of the simple type to find; must not contain any delimiters ('.' or '$')
	 * @return the fully qualified name of the type if found, <code>null</code> otherwise
	 */
	private String findSimpleType(String simpleTypeName) throws ClassNotFoundException {
		// 1. is it a type name currently being defined, or a member of such?
		// (note that overlap is not possible, since hiding is forbidden)
		for (Iterator it = simpleClassNameStack.iterator(), it2 = qualifiedClassNameStack.iterator(); it.hasNext();) {
			String simpleClassName = (String) it.next();
			String qualifiedClassName = (String) it2.next();
			
			if (simpleClassName.equals(simpleTypeName)) return qualifiedClassName;
			
			qualifiedClassName = qualifiedClassName + "$" + simpleTypeName;
			Class klass = load(qualifiedClassName);
			if (klass != null) return qualifiedClassName;
		}
		
		// 2. is it explicitly imported?
		String suffix = "." + simpleTypeName;
		for (Iterator it = classImports.iterator(); it.hasNext();) {
			String name = (String) it.next();
			if (name.endsWith(suffix)) return name;
		}
		
		// 3. is it in the same package?
		{
			String fullName = packageName + "." + simpleTypeName;
			Class klass = load(fullName);
			if (klass != null) return fullName;
		}
		
		// 4. is it a wildcard import?
		{
			Class klass = null;
			for (Iterator it = wildcardImports.iterator(); it.hasNext();) {
				String name = (String) it.next();
				Class klass2 = load(name + suffix);
				if (klass2 != null) {
					if (klass != null) throw new ClassNotFoundException("Ambiguous wildcard import of " + suffix);
					klass = klass2;
				}
			}
			if (klass != null) return klass.getName();
		}
		
		return null;
	}

	/**
	 * Resolve an array type, with the given base type name and number of dimensions.  If
	 * the number of dimensions is 0, the resolved type is not an array and the method works
	 * exactly like {@link #resolve(String)}.
	 * @param typeName a type name, qualified or not, nested or not
	 * @param dimensions the number of dimensions for the array type
	 * @return the resolved array type
	 * @throws ClassNotFoundException if the base type cannot be resolved
	 */
	public Class resolve(String typeName, int dimensions) throws ClassNotFoundException {
		Class base = resolve(typeName);
		if (dimensions == 0) return base;
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<dimensions; i++) buf.append('[');
		if (base.isPrimitive()) {
			buf.append(((PrimitiveType) primitiveTypes.get(base.getName())).code);
		} else {
			buf.append('L');
			buf.append(base.getName());
			buf.append(';');
		}
		return Class.forName(buf.toString(), false, Thread.currentThread().getContextClassLoader());
	}
	
	private Class load(String className) {
		try {
			return Thread.currentThread().getContextClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Register an import declaration, either fully qualified or wildcard.
	 * 
	 * @param importName the name of the imported type or package (including ".*" if wildcard)
	 */
	public void addImport(String importName) {
		if (importName.endsWith(".*")) {
			wildcardImports.add(importName.substring(0, importName.length()-2));
		} else {
			classImports.add(importName);
		}
	}

	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class Test extends TestCase {
		private TypeResolver resolver;
		public void setUp() {
			resolver = new TypeResolver();
		}
		public void tearDown() {
			resolver = null;
		}
		public void testProcessPackage() {
			resolver.addPackage("com.thoughtworks.qdox.attributes.dev");
			assertEquals("com.thoughtworks.qdox.attributes.dev", resolver.packageName);
		}
		public void testProcessImportClass() {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.Compiler");
			assertEquals(new HashSet(Arrays.asList(new String[]{"com.thoughtworks.qdox.attributes.dev.Compiler"})), resolver.classImports);
			assertEquals(1, resolver.wildcardImports.size());
			assertTrue(resolver.wildcardImports.contains("java.lang"));
		}
		public void testProcessImportClassMultiple() {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.Compiler");
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.alternative.Compiler");
			assertEquals(new HashSet(Arrays.asList(new String[]{"com.thoughtworks.qdox.attributes.dev.Compiler", "com.thoughtworks.qdox.attributes.dev.alternative.Compiler"})), resolver.classImports);
			assertEquals(1, resolver.wildcardImports.size());
			assertTrue(resolver.wildcardImports.contains("java.lang"));
		}
		public void testProcessImportClassDuplicate() {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.Compiler");
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.Compiler");
			assertEquals(new HashSet(Arrays.asList(new String[]{"com.thoughtworks.qdox.attributes.dev.Compiler"})), resolver.classImports);
			assertEquals(1, resolver.wildcardImports.size());
			assertTrue(resolver.wildcardImports.contains("java.lang"));
		}
		public void testProcessImportWildcard() {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
			assertEquals(new HashSet(Arrays.asList(new String[]{"com.thoughtworks.qdox.attributes.dev", "java.lang"})), resolver.wildcardImports);
			assertTrue(resolver.classImports.isEmpty());
		}
		public void testProcessImportWildcardMultiple() {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
			resolver.addImport("com.thoughtworks.qdox.attributes.impl.*");
			assertEquals(new HashSet(Arrays.asList(new String[]{"com.thoughtworks.qdox.attributes.dev", "com.thoughtworks.qdox.attributes.impl", "java.lang"})), resolver.wildcardImports);
			assertTrue(resolver.classImports.isEmpty());
		}
		public void testProcessImportWildcardDuplicate() {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
			assertEquals(new HashSet(Arrays.asList(new String[]{"com.thoughtworks.qdox.attributes.dev", "java.lang"})), resolver.wildcardImports);
			assertTrue(resolver.classImports.isEmpty());
		}
		public void testFindFullyQualifiedClass() throws ClassNotFoundException {
			Class klass = resolver.resolve("com.thoughtworks.qdox.attributes.dev.AttributesBuilder");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.class, klass);
		}
		public void testFindFullyQualifiedClassSamePackage() throws ClassNotFoundException {
			resolver.addPackage("com.thoughtworks.qdox.attributes.dev");
			Class klass = resolver.resolve("com.thoughtworks.qdox.attributes.dev.AttributesBuilder");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.class, klass);
		}
		public void testFindFullyQualifiedClassDifferentPackage() throws ClassNotFoundException {
			resolver.addPackage("com.thoughtworks.qdox.attributes.impl");
			Class klass = resolver.resolve("com.thoughtworks.qdox.attributes.dev.AttributesBuilder");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.class, klass);
		}
		public void testFindTopClassInPackage() throws ClassNotFoundException {
			resolver.addPackage("com.thoughtworks.qdox.attributes.dev");
			Class klass = resolver.resolve("AttributesBuilder");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.class, klass);
		}
		public void testFindNestedClassInPackage() throws ClassNotFoundException {
			resolver.addPackage("com.thoughtworks.qdox.attributes.dev");
			Class klass = resolver.resolve("AttributesBuilder.Mode");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.Mode.class, klass);
		}
		public void testFindDeepNestedClassInPackage() throws ClassNotFoundException {
			resolver.addPackage("com.thoughtworks.qdox.attributes.dev");
			Class klass = resolver.resolve("ObjectMode.TestParseValue.SampleAttribute");
			assertSame(com.thoughtworks.qdox.attributes.dev.ObjectMode.TestParseValue.SampleAttribute.class, klass);
		}
		public void testFindExplicitlyImportedTopClass() throws ClassNotFoundException {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.AttributesBuilder");
			Class klass = resolver.resolve("AttributesBuilder");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.class, klass);
		}
		public void testFindExplicitlyImportedNestedClass() throws ClassNotFoundException {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.AttributesBuilder");
			Class klass = resolver.resolve("AttributesBuilder.Mode");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.Mode.class, klass);
		}
		public void testFindExplicitlyImportedMissing() throws ClassNotFoundException {
			try {
				resolver.addImport("com.thoughtworks.qdox.attributes.dev.alternative.AttributesBuilder");
				resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
				resolver.resolve("AttributesBuilder");
				fail();
			} catch (ClassNotFoundException e) {
			}
		}
		public void testFindExplicitlyImportedOverPackagePrecedence() throws ClassNotFoundException {
			try {
				resolver.addPackage("com.thoughtworks.qdox.attributes.dev");
				resolver.addImport("com.thoughtworks.qdox.attributes.dev.alternative.AttributesBuilder");
				resolver.resolve("AttributesBuilder");
				fail();
			} catch (ClassNotFoundException e) {
			}
		}
		public void testFindWildcardImportedTopClass() throws ClassNotFoundException {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
			Class klass = resolver.resolve("AttributesBuilder");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.class, klass);
		}
		public void testFindWildcardImportedTopClass2() throws ClassNotFoundException {
			resolver.addImport("com.thoughtworks.qdox.attributes.impl.*");
			resolver.addImport("com.ideanest.attributes.*");
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
			Class klass = resolver.resolve("AttributesBuilder");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.class, klass);
		}
		public void testFindWildcardImportedNestedClass() throws ClassNotFoundException {
			resolver.addImport("com.thoughtworks.qdox.attributes.dev.*");
			Class klass = resolver.resolve("AttributesBuilder.Mode");
			assertSame(com.thoughtworks.qdox.attributes.dev.AttributesBuilder.Mode.class, klass);
		}
	}
}
