package com.thoughtworks.qdox.attributes.dev;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;

import junit.framework.TestCase;

import com.thoughtworks.qdox.attributes.impl.AttributeCreator;
import com.thoughtworks.qdox.attributes.impl.TagParseException;

/**
 * A static factory for attribute creators.  This code is in a separate class to reduce the size
 * of the runtime jar.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class AttributeCreatorFactory {
	
	public static AttributeCreator make(Class klass, String value) throws TagParseException, IntrospectionException {
		List args = new ArrayList();
		int firstNamedArgIndex = extractParameters(value, args);
		List propertyPairs = extractPropertyPairs(klass, args, firstNamedArgIndex);
		return new AttributeCreator(
			klass,
			(String[]) args.subList(0, firstNamedArgIndex).toArray(EMPTY_STRING_ARRAY),
			(String[]) propertyPairs.toArray(EMPTY_STRING_ARRAY)
		);
	}

	private static int extractParameters(String value, List args) throws TagParseException {
		int firstNamedArgIndex = -1;
		
		int state = SEEK;
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<value.length(); i++) {
			char c = value.charAt(i);
			switch(state) {
				case SEEK:
					switch(c) {
						case ' ': case '\t':  break;
						case '"': state = QUOTES; break;
						case '=': throw new TagParseException("assignment missing parameter name");
						default: buf.append(c); state = SIMPLE; break;
					}
					break;
				case QUOTES:
					switch(c) {
						case '"': args.add(buf.toString()); buf.setLength(0); state = AFTERQUOTES; break;
						case '\\': state = ESCAPE; break;
						default: buf.append(c); break;
					}
					break;
				case SIMPLE:
					switch(c) {
						case ' ': case '\t':
							if (firstNamedArgIndex != -1) throw new TagParseException("positional argument occurring after named parameter");
							args.add(buf.toString()); buf.setLength(0); state = SEEK;
							break;
						case '=':
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
							throw new TagParseException("invalid escape sequence \\" + c);
					}
					break;
				case AFTERQUOTES:
					switch(c) {
						case ' ': case '\t': state = SEEK; break;
						default: throw new TagParseException("no space after closing double quote at column " + i + " in '" + value + "'");
					}
					break;
				case AFTEREQUALS:
					switch(c) {
						case '"': state = QUOTES; break;
						case ' ': case '\t': throw new TagParseException("space after named attribute parameter assignment");
						default: buf.append(c); state = SIMPLEAFTEREQUALS; break;
					}
					break;
				case SIMPLEAFTEREQUALS:
					switch(c) {
						case ' ': case '\t': args.add(buf.toString()); buf.setLength(0); state = SEEK; break;
						default: buf.append(c); break;
					}
					break;
				default:
					throw new IllegalStateException("state=" + state);
			}
		}
		switch(state) {
			case SEEK: case AFTERQUOTES: break;
			case SIMPLE: case SIMPLEAFTEREQUALS: args.add(buf.toString()); break;
			case QUOTES: throw new TagParseException("unterminated quoted string");
			case ESCAPE: throw new TagParseException("unterminated escape");
			case AFTEREQUALS: throw new TagParseException("nothing after named attribute parameter assignment");
		}
		if (firstNamedArgIndex == -1) firstNamedArgIndex = args.size();
		return firstNamedArgIndex;
	}
	
	private static List extractPropertyPairs(Class klass, List args, int firstNamedArgIndex) throws IntrospectionException, TagParseException {
		List propertyPairs = new ArrayList();
		if (firstNamedArgIndex >= args.size()) return propertyPairs;
		
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
					propertyPairs.add(setMethod.getName());
					propertyPairs.add(propertyValue);
					continue NAMED_ARGS_LOOP;
				}
			}
			throw new TagParseException("no property " + propertyName);
		}
		return propertyPairs;
	}
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final int SEEK = 0, SIMPLE = 1, QUOTES = 2, ESCAPE = 3, AFTERQUOTES = 4, AFTEREQUALS = 5, SIMPLEAFTEREQUALS = 6;;
	

	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class ExtractParametersTest extends TestCase {
		private List args;
		public void setUp() {
			args = new ArrayList();
		}
		public void tearDown() {
			args = null;
		}
		public void testNoParams() throws TagParseException {
			int split = extractParameters("", args);
			assertEquals(0, args.size());
			assertEquals(0, split);
		}
		public void testOnePositionalParam() throws TagParseException {
			int split = extractParameters("hello", args);
			assertEquals(Arrays.asList(new String[]{"hello"}), args);
			assertEquals(1, split);
		}
		public void testManyPositionalParams() throws TagParseException {
			int split = extractParameters("hello goodbye argh", args);
			assertEquals(Arrays.asList(new String[]{"hello", "goodbye", "argh"}), args);
			assertEquals(3, split);
		}
		public void testMultipleSpaces() throws TagParseException {
			int split = extractParameters("hello    goodbye", args);
			assertEquals(Arrays.asList(new String[]{"hello", "goodbye"}), args);
			assertEquals(2, split);
		}
		public void testTabs() throws TagParseException {
			int split = extractParameters("hello\t\tgoodbye", args);
			assertEquals(Arrays.asList(new String[]{"hello", "goodbye"}), args);
			assertEquals(2, split);
		}
		public void testQuotes() throws TagParseException {
			int split = extractParameters("\"hellothere\"", args);
			assertEquals(Arrays.asList(new String[]{"hellothere"}), args);
			assertEquals(1, split);
		}
		public void testQuotesWithSpaces() throws TagParseException {
			int split = extractParameters("\"hello there\"", args);
			assertEquals(Arrays.asList(new String[]{"hello there"}), args);
			assertEquals(1, split);
		}
		public void testQuotesWithEquals() throws TagParseException {
			int split = extractParameters("\"hello=there\"", args);
			assertEquals(Arrays.asList(new String[]{"hello=there"}), args);
			assertEquals(1, split);
		}
		public void testQuotesWithEscapes() throws TagParseException {
			int split = extractParameters("\"hello \\\" \\\\ \\n \\t there\"", args);
			assertEquals(Arrays.asList(new String[]{"hello \" \\ \n \t there"}), args);
			assertEquals(1, split);
		}
		public void testQuotesWithInvalidEscapes() throws TagParseException {
			try {
				extractParameters("\"\\?\"", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testQuotesUnterminatedEscape() throws TagParseException {
			try {
				extractParameters("\"\\", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testMultipleMixedQuotes() throws TagParseException {
			int split = extractParameters("\"hellothere\" bye \"no more\"", args);
			assertEquals(Arrays.asList(new String[]{"hellothere", "bye", "no more"}), args);
			assertEquals(3, split);
		}
		public void testQuotesMissingSpace() throws TagParseException {
			try {
				extractParameters("\"hellothere\"bye", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testQuotesUnterminated() throws TagParseException {
			try {
				extractParameters("\"hellothere", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testOneNamedParam() throws TagParseException {
			int split = extractParameters("hello=bye", args);
			assertEquals(Arrays.asList(new String[]{"hello=bye"}), args);
			assertEquals(0, split);
		}
		public void testMultipleNamedParams() throws TagParseException {
			int split = extractParameters("hello=bye this=that", args);
			assertEquals(Arrays.asList(new String[]{"hello=bye", "this=that"}), args);
			assertEquals(0, split);
		}
		public void testMissingParamName() throws TagParseException {
			try {
				extractParameters("=bye", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testMissingParamValue1() throws TagParseException {
			try {
				extractParameters("hello=", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testMissingParamValue2() throws TagParseException {
			try {
				extractParameters("hello= nothing", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testMixedParams1() throws TagParseException {
			int split = extractParameters("blah hello=bye", args);
			assertEquals(Arrays.asList(new String[]{"blah", "hello=bye"}), args);
			assertEquals(1, split);
		}
		public void testMixedParams2() throws TagParseException {
			int split = extractParameters("blah glue hello=bye this=that", args);
			assertEquals(Arrays.asList(new String[]{"blah", "glue", "hello=bye", "this=that"}), args);
			assertEquals(2, split);
		}
		public void testMixedParamsBadOrder() throws TagParseException {
			try {
				extractParameters("blah hello=bye glue this=that", args);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testNamedParamWithQuotes() throws TagParseException {
			int split = extractParameters("hello=\"bye there\"", args);
			assertEquals(Arrays.asList(new String[]{"hello=bye there"}), args);
			assertEquals(0, split);
		}
	}
	
	/**
	 * @deprecated Test class that should not be javadoc'ed.
	 */
	public static class ExtractPropertyPairsTest extends TestCase {
		public void testExtract() throws Exception {
			List pairs = extractPropertyPairs(java.text.DecimalFormat.class, Arrays.asList(new String[]{"#,##0.0#;(#)", "negativePrefix=-", "positiveSuffix=+"}), 1);
			assertEquals(Arrays.asList(new String[]{"setNegativePrefix", "-", "setPositiveSuffix", "+"}), pairs);
		}
		public void testMethodWrongParamTypes() throws Exception {
			try {
				extractPropertyPairs(java.text.DecimalFormat.class, Arrays.asList(new String[]{"#,##0.0#;(#)", "groupingSize=2", "positiveSuffix=+"}), 1);
				fail();
			} catch (TagParseException e) {
			}
		}
		public void testMissingProperty() throws Exception {
			try {
				extractPropertyPairs(java.text.DecimalFormat.class, Arrays.asList(new String[]{"#,##0.0#;(#)", "nothere=2", "positiveSuffix=+"}), 1);
				fail();
			} catch (TagParseException e) {
			}
		}
	}

}
