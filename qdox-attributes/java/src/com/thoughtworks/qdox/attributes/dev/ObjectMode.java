package com.thoughtworks.qdox.attributes.dev;

import java.beans.*;
import java.lang.reflect.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import junit.framework.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.thoughtworks.qdox.attributes.SimpleAttribute;
import com.thoughtworks.qdox.attributes.impl.*;

/**
 * The strict object mode for the attributes builder.  This mode interprets tags as
 * attribute class names, and instantiates matching attribute creators that will
 * recreate the attribute at runtime according to the given parameters.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ObjectMode implements AttributesBuilder.Mode {

	private TypeResolver typeResolver;
	public void setTypeResolver(TypeResolver resolver) {
		this.typeResolver = resolver;
	}
		
	public void processTag(String tag, String text, SimpleBundle bundle) throws IllegalArgumentException, IntrospectionException, TagParseException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class klass = resolveTag(tag);
		if (klass == null) tagClassNotFound(tag, text, bundle);
		else {
			AttributeCreator creator = parse(klass, text);
			Object attribute = creator.create();
			if (attribute instanceof java.io.Serializable) {
				bundle.add(attribute);
			} else {
				bundle.add(attribute, creator);
			}
		}
	}

	private Class resolveTag(String tag) throws TagParseException {
		Class klass = null;
		try {
			klass = typeResolver.resolve(tag);
		} catch (ClassNotFoundException e) {
		}
		try {
			Class klass2 = typeResolver.resolve(tag + "Attribute");
			if (klass != null) throw new TagParseException("ambiguous class mapping for tag " + tag);
			klass = klass2;
		} catch (ClassNotFoundException e) {
		}
		return klass;
	}
		
	protected void tagClassNotFound(String tag, String text, SimpleBundle bundle) throws TagParseException {
		throw new TagParseException("no class found for tag " + tag);
	}
	
	public AttributeCreator parse(Class klass, String value) throws IllegalArgumentException, IntrospectionException, TagParseException, ClassNotFoundException, IllegalAccessException {
		List args = new ArrayList();
		int firstNamedArgIndex = extractParameters(value, args);
		IndirectObjectList propertyTriples = extractPropertyTriples(klass, args, firstNamedArgIndex);
		IndirectObjectList constructorArgs = new IndirectObjectList();
		Class[] argTypes = parseConstructorArgs(klass, args.subList(0, firstNamedArgIndex), constructorArgs);
		return new AttributeCreator(klass, argTypes, constructorArgs, propertyTriples);
	}
	
	private int extractParameters(String value, List args) throws TagParseException {
		return extractParameters(value, 0, args, false);
	}
	
	private int extractParameters(String value, int index, List args, boolean inArray) throws TagParseException {
		int firstNamedArgIndex = -1;
		
		int state = SEEK;
		StringBuffer buf = new StringBuffer();
		for (int i=index; i<value.length(); i++) {
			try {
				char c = value.charAt(i);
				switch(state) {
					case SEEK:
						switch(c) {
							case ' ': case '\t':  break;
							case '"': buf.append(c); state = QUOTES; break;
							case '=': throw new TagParseException("assignment missing parameter name");
							case '{':
								List array = new ArrayList();
								i = extractParameters(value, i+1, array, true);
								args.add(array);
								state = AFTERARRAY;
								break;
							case '}':
								if (!inArray) throw new TagParseException("unmatched closing brace");
								return i;
							default: buf.append(c); state = SIMPLE; break;
						}
						break;
					case AFTERARRAY:
						switch(c) {
							case ' ': case '\t': state = SEEK; break;
							case '{':
								List array = new ArrayList();
								i = extractParameters(value, i+1, array, true);
								args.add(array);
								break;
							case '}':
								if (!inArray) throw new TagParseException("unmatched closing brace");
								return i;
							default:
								throw new TagParseException("invalid character '" + c + "' after closing brace");
						}
						break;
					case QUOTES:
						switch(c) {
							case '"':
								buf.append(c);
								args.add(buf.toString());
								buf.setLength(0);
								state = AFTERQUOTES;
								break;
							case '\\': state = ESCAPE; break;
							default: buf.append(c); break;
						}
						break;
					case SIMPLE:
						switch(c) {
							case ' ': case '\t':
								if (firstNamedArgIndex != -1) throw new TagParseException("positional parameter occurring after named parameter");
								args.add(buf.toString());
								buf.setLength(0);
								state = SEEK;
								break;
							case '{':
								throw new TagParseException("brace embedded in unquoted value");
							case '}':
								if (!inArray) throw new TagParseException("unmatched closing brace");
								args.add(buf.toString());
								return i;
							case '=':
								if (inArray) throw new TagParseException("named parameter occurring in array");
								if (firstNamedArgIndex == -1) firstNamedArgIndex = args.size();
								state = AFTEREQUALS;	// fall through
							default: buf.append(c); break;
						}
						break;
					case ESCAPE:
						switch(c) {
							case '"': buf.append('"'); state = QUOTES; break;
							case '\\': buf.append('\\'); state = QUOTES; break;
							case 'n': buf.append('\n'); state = QUOTES; break;
							case 't': buf.append('\t'); state = QUOTES; break;
							default:
								// TODO: implement all Java escape sequences
								throw new TagParseException("invalid escape sequence '\\" + c + "'");
						}
						break;
					case AFTERQUOTES:
						switch(c) {
							case ' ': case '\t': state = SEEK; break;
							case '}':
								if (!inArray) throw new TagParseException("unmatched closing brace");
								return i;
							default: throw new TagParseException("no space after closing double quote");
						}
						break;
					case AFTEREQUALS:
						switch(c) {
							case '"': buf.append(c); state = QUOTES; break;
							case ' ': case '\t': throw new TagParseException("space after named attribute parameter assignment");
							case '{': case '}': throw new TagParseException("brace embedded in unquoted value");
							default: buf.append(c); state = SIMPLEAFTEREQUALS; break;
						}
						break;
					case SIMPLEAFTEREQUALS:
						switch(c) {
							case ' ': case '\t': args.add(buf.toString()); buf.setLength(0); state = SEEK; break;
							case '{': case '}': throw new TagParseException("brace embedded in unquoted value");
							default: buf.append(c); break;
						}
						break;
					default:
						throw new IllegalStateException("state=" + state);
				}
			} catch (TagParseException e) {
				e.value = value;
				e.column = i;
				throw e;
			}
		}
		if (inArray) throw new TagParseException("unbalanced opening brace");
		switch(state) {
			case SEEK: case AFTERQUOTES: case AFTERARRAY: break;
			case SIMPLE: case SIMPLEAFTEREQUALS: args.add(buf.toString()); break;
			case QUOTES: throw new TagParseException("unterminated quoted string");
			case ESCAPE: throw new TagParseException("unterminated escape");
			case AFTEREQUALS: throw new TagParseException("nothing after named attribute parameter assignment");
			default: throw new RuntimeException("unexpected terminal state " + state);
		}
		if (firstNamedArgIndex == -1) firstNamedArgIndex = args.size();
		return firstNamedArgIndex;
	}

	private static final int SEEK = 0, SIMPLE = 1, QUOTES = 2, ESCAPE = 3, AFTERQUOTES = 4, AFTEREQUALS = 5, SIMPLEAFTEREQUALS = 6, AFTERARRAY = 7;
	
	private IndirectObjectList extractPropertyTriples(Class klass, List args, int firstNamedArgIndex) throws IntrospectionException, TagParseException, IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
		IndirectObjectList propertyTriples = new IndirectObjectList();
		if (firstNamedArgIndex >= args.size()) return propertyTriples;
		
		PropertyDescriptor[] props = Introspector.getBeanInfo(klass).getPropertyDescriptors();
		NAMED_ARGS_LOOP:  for (Iterator it = args.listIterator(firstNamedArgIndex); it.hasNext();) {
			String pair = (String) it.next();
			int eq = pair.indexOf('=');
			assert eq != -1;
			String propertyName = pair.substring(0, eq);
			String propertyValue = pair.substring(eq+1);
			for (int i = 0; i < props.length; i++) {
				PropertyDescriptor descriptor = props[i];
				if (descriptor.getName().equals(propertyName)) {
					Method setMethod = descriptor.getWriteMethod();
					Class[] setMethodParams = setMethod.getParameterTypes();
					if (!(setMethodParams.length == 1 && setMethodParams[0] == String.class)) throw new TagParseException("property " + propertyName + " does not have a one string setter method");
					propertyTriples.add(setMethod.getName());
					propertyTriples.add(descriptor.getPropertyType());
					parseValue(propertyValue, descriptor.getPropertyType(), propertyTriples, klass);
					continue NAMED_ARGS_LOOP;
				}
			}
			throw new TagParseException("no property " + propertyName);
		}
		return propertyTriples;
	}
	
	private Class[] parseConstructorArgs(Class klass, List stringArgs, IndirectObjectList args) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
		Constructor[] constructors = klass.getConstructors();
		Constructor constructor = null;
		for (int i=0; i<constructors.length; i++) {
			if (constructors[i].getParameterTypes().length == stringArgs.size()) {
				if (constructor == null) {
					constructor = constructors[i];
				} else {
					throw new IllegalArgumentException("multiple attribute constructors with " + stringArgs.size() + " formal parameters");
				}
			}
		}
		if (constructor == null) throw new IllegalArgumentException("no attribute constructor with " + stringArgs.size() + " formal parameters");
		Class[] argTypes = constructor.getParameterTypes();
		for (int i = 0; i < argTypes.length; i++) {
			parseValue(stringArgs.get(i), argTypes[i], args, klass);
		}
		return argTypes;
	}
	
	private static Class normalizeType(Class type) {
		if (type == char.class) type = Character.class;
		else if (type == boolean.class) type = Boolean.class;
		else if (type == byte.class) type = Byte.class;
		else if (type == short.class) type = Short.class;
		else if (type == int.class) type = Integer.class;
		else if (type == long.class) type = Long.class;
		else if (type == float.class) type = Float.class;
		else if (type == double.class) type = Double.class;
		return type;
	}
	
	private void parseValue(Object value, Class type, IndirectObjectList dest, Class attributeClass) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		type = normalizeType(type);
		String s = null;
		
		// check for constant reference
		if (value instanceof String) {
			s = (String) value;
			if (s.charAt(0) == '"') {
				// strip quotes, don't check for constant name
				s = s.substring(1, s.length()-1);
			} else {
				// check if simple value refers to a constant
				Class fieldClass = null;
				String fieldName = s;
				Field field = null;
				int lastDot = s.lastIndexOf('.');
				if (lastDot == -1) {
					// simple name, try it as a member of the attribute type
					try {
						fieldClass = attributeClass;
						field = fieldClass.getField(fieldName);
					} catch (NoSuchFieldException e) {
					}
					try {
						// if not of the attribute type, try the value type
						Field field2 = type.getField(fieldName);
						if (field != null) throw new IllegalArgumentException("ambiguous constant reference " + fieldName);
						field = field2;
						fieldClass = type;
					} catch (NoSuchFieldException e2) {
					}
				} else if (lastDot < s.length()) {
					try {
						fieldClass = typeResolver.resolve(s.substring(0, lastDot));
						fieldName = s.substring(lastDot+1);
						field = fieldClass.getField(fieldName);
					} catch (ClassNotFoundException e) {
					} catch (NoSuchFieldException e) {
					}
				}
				if (field != null) {
					// got a possible!
					if (!(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))) throw new IllegalArgumentException("field " + s + " must be static and final");
					if (!type.isAssignableFrom(normalizeType(field.getType()))) throw new IllegalArgumentException("constant field type " + field.getType() + " not compatible with parameter type " + type);
					Object constant = field.get(null);
					if (constant instanceof java.io.Serializable) {
						dest.add(constant);
					} else {
						dest.add(constant, new ConstantCreator(fieldClass, fieldName));
					}
					return;
				}
			}
		}
		
		if (type.isArray()) {

			if (!(value instanceof List)) throw new IllegalArgumentException("other value (" + value + ") supplied where list was expected");
			IndirectObjectList contents = new IndirectObjectList();
			for (Iterator it = ((List) value).iterator(); it.hasNext();) {
				parseValue((Object) it.next(), type.getComponentType(), contents, attributeClass);
			}
			if (type.getComponentType().isPrimitive()) {
				Object a = Array.newInstance(type.getComponentType(), contents.size());
				int i=0;
				for (Iterator it = contents.iterator(); it.hasNext();) Array.set(a, i++, it.next());
				dest.add(a);
			} else {
				ArrayCreator creator = new ArrayCreator(type, contents);
				Class baseType = type;
				while (baseType.isArray()) baseType = baseType.getComponentType();
				// check if array type indicates it's fully serializable; other option would be to check actual contents at bottom levels
				if (baseType.isPrimitive() || java.io.Serializable.class.isAssignableFrom(baseType)) {
					dest.add(creator.create());
				} else {
					dest.add(creator.create(), creator);
				}
			}
			
		} else {
			
			if (s == null) throw new IllegalArgumentException("array supplied where simple attribute parameter was expected");

			// simple if/else selection ladder, could change to hashmap lookup if performance is bad
			if (type == String.class) {
				dest.add(s);
			} else if (type == Character.class) {
				if (s.length() != 1) throw new IllegalArgumentException("invalid character literal: " + s);
				dest.add(new Character(s.charAt(0)));
			} else if (type == Class.class) {
				dest.add(typeResolver.resolve(s));
			} else if (type == Boolean.class) {
				if ("true".equals(s)) dest.add(Boolean.TRUE);
				else if ("false".equals(s)) dest.add(Boolean.FALSE);
				else throw new IllegalArgumentException("invalid boolean literal: " + s);
			} else if (type == Byte.class) {dest.add(Byte.valueOf(s));
			} else if (type == Integer.class) {dest.add(Integer.valueOf(s));
			} else if (type == Long.class) {dest.add(Long.valueOf(s));
			} else if (type == Float.class) {dest.add(Float.valueOf(s));
			} else if (type == Double.class) {dest.add(Double.valueOf(s));
			} else if (type == Short.class) {dest.add(Short.valueOf(s));
			} else {throw new IllegalArgumentException("unsupported parameter type: " + type.getName());
			}
		}
	}


	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class TestParseConstructorArgs extends TestCase {
		private IndirectObjectList args;
		private ObjectMode mode;
		private Class[] types;
		protected void setUp() {
			args = new IndirectObjectList();
			mode = new ObjectMode();
			mode.setTypeResolver(new TypeResolver());
			types = null;
		}
		protected void tearDown() {
			args = null;
			types = null;
		}
		public void testEmpty() throws Exception {
			types = mode.parseConstructorArgs(Vector.class, new ArrayList(), args);
			assertEquals(0, types.length);
			assertEquals(0, args.size());
		}
		public void testSimpleInt() throws Exception {
			types = mode.parseConstructorArgs(Vector.class, Arrays.asList(new String[]{"10", "3"}), args);
			assertTrue(Arrays.equals(new Class[]{int.class, int.class}, types));
			assertTrue(Arrays.equals(new Object[]{new Integer(10), new Integer(3)}, args.toArray()));
		}
		public void testOverload() throws Exception {
			try {
				types = mode.parseConstructorArgs(Vector.class, Arrays.asList(new String[]{"10"}), args);
				fail();
			} catch (IllegalArgumentException e) {
			}
		}
		public void testMissing() throws Exception {
			try {
				types = mode.parseConstructorArgs(Vector.class, Arrays.asList(new String[]{"10", "a", "b"}), args);
				fail();
			} catch (IllegalArgumentException e) {
			}
		}
	}
	
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class TestParseValue extends TestCase {
		private static final Object[][] TESTS = {
			{"\"hello\"", String.class, "hello"},
			{"hello", String.class, "hello"},
			{new Object[]{"hello"}, String.class, IllegalArgumentException.class},
			{"true", boolean.class, Boolean.TRUE},
			{"true", Boolean.class, Boolean.TRUE},
			{"false", boolean.class, Boolean.FALSE},
			{"false", Boolean.class, Boolean.FALSE},
			{"True", boolean.class, IllegalArgumentException.class},
			{new Object[]{"hello"}, boolean.class, IllegalArgumentException.class},
			{"42", int.class, new Integer(42)},
			{"-23", Integer.class, new Integer(-23)},
			{"0x21", int.class, NumberFormatException.class},
			{"abc", Integer.class, NumberFormatException.class},
			{"123456789012345", int.class, NumberFormatException.class},
			{new Object[]{"hello"}, int.class, IllegalArgumentException.class},
			{"42", long.class, new Long(42)},
			{"-23", Long.class, new Long(-23)},
			{"0x21", long.class, NumberFormatException.class},
			{"abc", Long.class, NumberFormatException.class},
			{"123456789012345", long.class, new Long(123456789012345L)},
			{"12345678901234567890", long.class, NumberFormatException.class},
			{new Object[]{"hello"}, long.class, IllegalArgumentException.class},
			{"42", byte.class, new Byte((byte) 42)},
			{"-23", Byte.class, new Byte((byte) -23)},
			{"0x21", byte.class, NumberFormatException.class},
			{"abc", Byte.class, NumberFormatException.class},
			{"100000", byte.class, NumberFormatException.class},
			{new Object[]{"hello"}, byte.class, IllegalArgumentException.class},
			{"42", short.class, new Short((short) 42)},
			{"-23", Short.class, new Short((short) -23)},
			{"0x21", short.class, NumberFormatException.class},
			{"abc", Short.class, NumberFormatException.class},
			{"100000", short.class, NumberFormatException.class},
			{new Object[]{"hello"}, short.class, IllegalArgumentException.class},
			{"42.5", float.class, new Float(42.5f)},
			{"-23.123", Float.class, new Float(-23.123f)},
			{"0x21", float.class, NumberFormatException.class},
			{"abc", Float.class, NumberFormatException.class},
			{"1234567890123456789012345678901234567890", float.class, new Float(Float.POSITIVE_INFINITY)},
			{new Object[]{"hello"}, float.class, IllegalArgumentException.class},
			{"42.5", double.class, new Double(42.5d)},
			{"-23.123", Double.class, new Double(-23.123d)},
			{"0x21", Double.class, NumberFormatException.class},
			{"abc", Double.class, NumberFormatException.class},
			{"1.5e5000", double.class, new Double(Double.POSITIVE_INFINITY)},
			{new Object[]{"hello"}, double.class, IllegalArgumentException.class},
			{"a", char.class, new Character('a')},
			{"b", Character.class, new Character('b')},
			{"ab", char.class, IllegalArgumentException.class},
			{new Object[]{"hello"}, char.class, IllegalArgumentException.class},
			{new String[]{"hello"}, String[].class, new String[]{"hello"}},
			{new String[]{"hello", "bye"}, String[].class, new String[]{"hello", "bye"}},
			{new String[]{"1", "2", "3"}, int[].class, new int[]{1, 2, 3}},
			{new Object[]{new Object[]{"a", "b"}, new Object[]{"c", "d"}}, char[][].class, new char[][]{new char[]{'a', 'b'}, new char[]{'c', 'd'}}},
			{"hello", String[].class, IllegalArgumentException.class},
			{"ObjectMode.TestParseValue.TestAttribute", Class.class, TestAttribute.class},
			{"NAME", String.class, "Toto"},
			{"\"NAME\"", String.class, "NAME"},
			{"INT_CONST", int.class, new Integer(42)},
			{"INT_CONST", Integer.class, new Integer(42)},
			{"INTEGER_CONST", int.class, new Integer(-3)},
			{"INTEGER_CONST", Integer.class, new Integer(-3)},
			{"VALUE1", TestEnum.class, TestEnum.VALUE1},
			{"VALUE2", TestEnum.class, IllegalArgumentException.class},
			{"VALUE3", TestEnum.class, IllegalArgumentException.class},
			{"value4", TestEnum.class, IllegalArgumentException.class},
			{"VALUE5", TestEnum.class, TestAttribute.VALUE5},
		};
		private static List asList(Object[] a) {
			for (int i=0; i<a.length; i++) {
				if (a[i].getClass().isArray()) a[i] = asList((Object[]) a[i]);
			}
			return Arrays.asList(a);
		}
		public static Test suite() {
			TestSuite suite = new TestSuite();
			for (int i=0; i<TESTS.length; i++) {
				Object[] spec = TESTS[i];
				Object v = spec[0];
				if (v.getClass().isArray()) v = asList((Object[]) v);
				suite.addTest(new TestParseValue(v, (Class) spec[1], spec[2]));
			}
			return suite;
		}

		private final Object value;
		private final Class type;
		private final Object expectedResult;
		TestParseValue(Object value, Class type, Object expectedResult) {
			// replace ',' with ';' to work around Eclipse JUnit bug
			super(("Parse value " + value).replace(',', ';'));
			this.value = value;
			this.type = type;
			this.expectedResult = expectedResult;
		}
		protected void runTest() throws Exception {
			try {
				IndirectObjectList dest = new IndirectObjectList();
				TypeResolver resolver = new TypeResolver();
				resolver.addPackage("com.thoughtworks.qdox.attributes.dev");
				ObjectMode mode = new ObjectMode();
				mode.setTypeResolver(resolver);
				mode.parseValue(value, type, dest, TestAttribute.class);
				Object result = dest.toArray()[dest.size()-1];
				if (expectedResult.getClass() == Class.class && Throwable.class.isAssignableFrom((Class) expectedResult)) fail("expected exception " + ((Class) expectedResult).getName() + ", got " + result);
				assertEqualsArray(expectedResult, result);
			} catch (Exception e) {
				if (!(expectedResult.getClass() == Class.class && Throwable.class.isAssignableFrom((Class) expectedResult))) throw e;
				if (!((Class) expectedResult).isAssignableFrom(e.getClass())) throw e;
			}
		}
		public static void assertEqualsArray(Object a1, Object a2) {
			if (a1.getClass().isArray()) {
				assertSame("array type", a1.getClass(), a2.getClass());
				int s1 = Array.getLength(a1), s2 = Array.getLength(a2);
				assertEquals("array size", s1, s2);
				for (int i=0; i<s1; i++) {
					assertEqualsArray(Array.get(a1, i), Array.get(a2, i));
				}
			} else {
				assertEquals(a1, a2);
			}
		}

		/**
		 * @deprecated Test class that should not be javadoc'ed.
		 */
		public static class TestAttribute extends TestCase {
			public static final String NAME = "Toto";
			public static final TestEnum VALUE2 = new TestEnum();
			public static final TestEnum VALUE5 = new TestEnum();
			public static final int INT_CONST = 42;
			public static final Integer INTEGER_CONST = new Integer(-3);
		}
		/**
		 * @deprecated Test class that should not be javadoc'ed.
		 */
		public static class TestEnum {
			public static final TestEnum VALUE1 = new TestEnum();
			public static final TestEnum VALUE2 = new TestEnum();
			public static TestEnum VALUE3 = new TestEnum();
			public final TestEnum value4 = null;
			public TestEnum() {}
		}
	}
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class TestExtractParams extends TestCase {
		private static final Object[][] TESTS = {
			{"", "0", new String[] {} },
			{"hello", "1", new String[] {"hello"} },
			{"hello goodbye argh", "3", new String[]{"hello", "goodbye", "argh"} },
			{"hello    goodbye", "2", new String[]{"hello", "goodbye"} },
			{"hello\t\tgoodbye", "2", new String[]{"hello", "goodbye"} },
			{"\"hellothere\"", "1", new String[]{"\"hellothere\""} },
			{"\"hello there\"", "1", new String[]{"\"hello there\""} },
			{"\"hello=there\"", "1", new String[]{"\"hello=there\""} },
			{"\"hello \\\" \\\\ \\n \\t there\"", "1", new String[]{"\"hello \" \\ \n \t there\""} },
			{"\"\\?\"", null, null},
			{"\"\\", null, null},
			{"\"hellothere\" bye \"no more\"", "3", new String[]{"\"hellothere\"", "bye", "\"no more\""} },
			{"\"hellothere\"bye", null, null},
			{"\"hellothere", null, null},
			{"hello=bye", "0", new String[]{"hello=bye"} },
			{"hello=bye this=that", "0", new String[]{"hello=bye", "this=that"} },
			{"=bye", null, null},
			{"hello=", null, null},
			{"hello= nothing", null, null},
			{"blah hello=bye", "1", new String[]{"blah", "hello=bye"} },
			{"blah glue hello=bye this=that", "2", new String[]{"blah", "glue", "hello=bye", "this=that"} },
			{"blah hello=bye glue this=that", null, null},
			{"hello=\"bye there\"", "0", new String[]{"hello=\"bye there\""} },
			{"{ }", "1", new Object[]{new Object[]{}} },
			{"{ { } }", "1", new Object[]{new Object[]{new Object[]{}}} },
			{"{ hello }", "1", new Object[]{new Object[]{"hello"}} },
			{"{ { hello } }", "1", new Object[]{new Object[]{new Object[]{"hello"}}} },
			{"a { b } c", "3", new Object[]{"a", new Object[]{"b"}, "c"} },
			{"a { b} c", "3", new Object[]{"a", new Object[]{"b"}, "c"} },
			{"a {b } c", "3", new Object[]{"a", new Object[]{"b"}, "c"} },
			{"a {b} c", "3", new Object[]{"a", new Object[]{"b"}, "c"} },
			{"a {\"b\"} c", "3", new Object[]{"a", new Object[]{"\"b\""}, "c"} },
			{"a { b x=1 } c", null, null},
			{"a { b c", null, null},
			{"a { b } } c", null, null},
			{"a { { b } c", null, null},
			{"a { b}x } c", null, null},
			{"a {{b}} c", "3", new Object[]{"a", new Object[]{new Object[]{"b"}}, "c"} },
			{"a {b}{c}", "3", new Object[]{"a", new Object[]{"b"}, new Object[]{"c"}} },
		};
		private static List asList(Object[] a) {
			for (int i=0; i<a.length; i++) {
				if (a[i] instanceof Object[]) a[i] = asList((Object[]) a[i]);
			}
			return Arrays.asList(a);
		}
		public static Test suite() {
			TestSuite suite = new TestSuite();
			for (int i=0; i<TESTS.length; i++) {
				Object[] spec = TESTS[i];
				if (spec[1] == null || spec[2] == null) {
					suite.addTest(new TestExtractParams((String) spec[0], -1, null));
				} else {
					suite.addTest(new TestExtractParams((String) spec[0], Integer.parseInt((String) spec[1]), asList((Object[]) spec[2])));
				}
			}
			return suite;
		}

		private final String input;
		private final int expectedSplit;
		private final List expectedResult;
		TestExtractParams(String input, int expectedSplit, List expectedResult) {
			super("Extract params  \"" + input + "\"");
			this.input = input;
			this.expectedSplit = expectedSplit;
			this.expectedResult = expectedResult;
		}
		protected void runTest() throws TagParseException {
			try {
				List args = new ArrayList();
				int split = new ObjectMode().extractParameters(input, args);
				if (expectedSplit == -1 || expectedResult == null) fail();
				assertEquals(expectedSplit, split);
				assertEquals(expectedResult, args);
			} catch (TagParseException e) {
				if (!(expectedSplit == -1 || expectedResult == null)) throw e;
			}
		}
	}
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class ExtractPropertyTriplesTest extends TestCase {
		private ObjectMode mode;
		protected void setUp() {
			mode = new ObjectMode();
			mode.setTypeResolver(new TypeResolver());
		}
		protected void tearDown() {
			mode = null;
		}
		public void testExtract() throws Exception {
			IndirectObjectList triples = mode.extractPropertyTriples(java.text.DecimalFormat.class, Arrays.asList(new String[]{"#,##0.0#;(#)", "negativePrefix=-", "positiveSuffix=+"}), 1);
			assertEquals(Arrays.asList(new Object[]{"setNegativePrefix", String.class, "-", "setPositiveSuffix", String.class, "+"}), Arrays.asList(triples.toArray()));
		}
		public void testMethodWrongParamTypes() throws Exception {
			try {
				mode.extractPropertyTriples(java.text.DecimalFormat.class, Arrays.asList(new String[]{"#,##0.0#;(#)", "groupingSize=2", "positiveSuffix=+"}), 1);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testMissingProperty() throws Exception {
			try {
				mode.extractPropertyTriples(java.text.DecimalFormat.class, Arrays.asList(new String[]{"#,##0.0#;(#)", "nothere=2", "positiveSuffix=+"}), 1);
				fail();
			} catch (TagParseException e) {
			}
		}
	}
	
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class TestResolveTag extends TestCase {
		private ObjectMode mode;
		public void setUp() {
			mode = new ObjectMode();
			mode.setTypeResolver(new TypeResolver());
		}
		public void tearDown() {
			mode = null;
		}
		public void testTagAttributeSuffix() throws Exception {
			Class klass = mode.resolveTag("com.thoughtworks.qdox.attributes.Simple");
			assertSame(SimpleAttribute.class, klass);
		}
		public void testTagAttributeExplicitSuffix() throws Exception {
			Class klass = mode.resolveTag("com.thoughtworks.qdox.attributes.SimpleAttribute");
			assertSame(SimpleAttribute.class, klass);
		}
		public void testAmbiguousTag() throws Exception {
			try {
				mode.resolveTag(Test.class.getName());
				fail();
			} catch (TagParseException e) {
			}
		}
		
		public static class Test {}
		public static class TestAttribute {}
	}


}
