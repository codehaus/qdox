package com.thoughtworks.qdox.xml;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import junit.framework.TestCase;

public class JavaDocXmlGenerator_Test extends TestCase {

	//~~~( Constants )~~~

	private static final String TEST_FILE_PREFIX =
		"src/test/com/thoughtworks/qdox/xml/input";

	//~~~( Constructors )~~~

	public JavaDocXmlGenerator_Test(String name) {
		super(name);
	}

	//~~~( Tests )~~~

	public void testEmptyClass() throws Exception {
		testConversion("EmptyClass");
	}

	public void testDocumentedClass() throws Exception {
		testConversion("DocumentedClass");
	}

	public void testSuperClass() throws Exception {
		testConversion("SuperClass");
	}

	public void testImplements() throws Exception {
		testConversion("Implements");
	}

	public void testMultipleTags() throws Exception {
		testConversion("MultipleTags");
	}

	public void testFields() throws Exception {
		testConversion("Fields");
	}

	public void testDocumentedField() throws Exception {
		testConversion("DocumentedField");
	}

	public void testMethod() throws Exception {
		testConversion("Method");
	}

	public void testConstructor() throws Exception {
		testConversion("Constructor");
	}

	public void testMethodExceptions() throws Exception {
		testConversion("MethodExceptions");
	}

	public void testImports() throws Exception {
		testConversion("Imports");
	}

	public void testInnerClass() throws Exception {
		testConversion("InnerClass");
	}

	public void testModifiers() throws Exception {
		testConversion("Modifiers");
	}

	//~~~( Test utils )~~~

	protected void testConversion(String testCaseName) throws Exception {
		String actualXml =
			toXml(parse(getTestFile(testCaseName + ".in")));
		String expectedXml =
			readExpectedFile(getTestFile(testCaseName + ".xml"));
		if (!expectedXml.equals(actualXml)) {
			fail("EXPECTED:\n" + expectedXml + "\nBUT WAS:\n" + actualXml);
		}
	}

	/**
	 * Parse a single Java source-file
	 */
	private JavaSource[] parse(File file) throws Exception {
		JavaDocBuilder builder = new JavaDocBuilder();
		builder.addSource(file);
		return builder.getSources();
	}

	/**
	 * Convert parsed JavaDoc to XML.
	 */
	private String toXml(JavaSource[] javaDoc) throws Exception {
		StringWriter buffer = new StringWriter();
		JavaDocXmlGenerator generator =
			new JavaDocXmlGenerator(new TextXmlHandler(buffer));
		generator.write(javaDoc);
		return buffer.toString();
	}

	private File getTestFile(String name) {
		return new File(TEST_FILE_PREFIX + "/" + name);
	}

	/**
	 * Read a file containing "expected" XML.  Trim lines to ignore leading
	 * whitespace.
	 */
	private String readExpectedFile(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		for (String line = in.readLine();
			 line != null; line = in.readLine())
		{
			out.println(line.trim());
		}
		return buffer.toString();
	}

}
