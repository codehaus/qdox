package com.thoughtworks.qdox;

import java.io.StringReader;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import junit.framework.TestCase;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

public class MultipleJavaDocBuilderTest extends TestCase {

	public MultipleJavaDocBuilderTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createFile("tmp/sourcetest/com/blah/Thing.java", "com.blah", "Thing");
		createFile("tmp/sourcetest/com/blah/Another.java", "com.blah", "Another");
		createFile("tmp/sourcetest/com/blah/subpackage/Cheese.java", "com.blah.subpackage", "Cheese");
		createFile("tmp/sourcetest/com/blah/Ignore.notjava", "com.blah", "Ignore");
	}

	public void testParsingMultipleJavaFiles(){
		MultipleJavaDocBuilder builder = new MultipleJavaDocBuilder();
		builder.addSource(new StringReader(createTestClassList()));
		builder.addSource(new StringReader(createTestClass()));
		JavaSource[] sources = builder.getSources();
		assertEquals(2, sources.length);

		JavaClass testClassList = sources[0].getClasses()[0];
		assertEquals("TestClassList", testClassList.getName());
		assertEquals("com.thoughtworks.util.TestClass", testClassList.getSuperClass().getValue());

		JavaClass testClass = sources[1].getClasses()[0];
		assertEquals("TestClass", testClass.getName());


		JavaClass testClassListByName = builder.getClassByName("com.thoughtworks.qdox.TestClassList");
		assertEquals("TestClassList", testClassListByName.getName());

		JavaClass testClassByName = builder.getClassByName("com.thoughtworks.util.TestClass");
		assertEquals("TestClass", testClassByName.getName());

		assertNull(builder.getClassByName("this.class.should.not.Exist"));
	}

	private String createTestClassList(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("package com.thoughtworks.qdox;");
		buffer.append("import com.thoughtworks.util.*;");
		buffer.append("public class TestClassList extends TestClass{");
		buffer.append("private int numberOfTests;");
		buffer.append("public int getNumberOfTests(){return numberOfTests;}");
		buffer.append("public void setNumberOfTests(int numberOfTests){this.numberOfTests = numberOfTests;}");
		buffer.append("}");
		return buffer.toString();
	}

	private String createTestClass(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("package com.thoughtworks.util;");
		buffer.append("public class TestClass{");
		buffer.append("public void test(){}");
		buffer.append("}");
		return buffer.toString();
	}

	public void testSourceTree() throws Exception {
		MultipleJavaDocBuilder builder = new MultipleJavaDocBuilder();
		builder.addSourceTree(new File("tmp/sourcetest"));

		assertNotNull(builder.getClassByName("com.blah.Thing"));
		assertNotNull(builder.getClassByName("com.blah.Another"));
		assertNotNull(builder.getClassByName("com.blah.subpackage.Cheese"));
		assertNull(builder.getClassByName("com.blah.Ignore"));
	}

	public void testSearcher() throws Exception {
		MultipleJavaDocBuilder builder = new MultipleJavaDocBuilder();
		builder.addSourceTree(new File("tmp/sourcetest"));

		List results = builder.search(new Searcher() {
			public boolean eval(JavaClass cls) {
				return cls.getPackage().equals("com.blah");
			}
		});

		assertEquals(2, results.size());
		assertEquals("Another", ((JavaClass)results.get(0)).getName());
		assertEquals("Thing", ((JavaClass)results.get(1)).getName());
	}

	private void createFile(String fileName, String packageName, String className) throws Exception {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		writer.write("// this file generated by MultipleJavaDocBuilderTest - feel free to delete it\n");
		writer.write("package " + packageName + ";\n\n");
		writer.write("public class " + className + " {\n\n  // empty\n\n}\n");
		writer.close();
	}

	public void testDefaultClassLoader() throws Exception {
		MultipleJavaDocBuilder builder = new MultipleJavaDocBuilder();
		String in = ""
			+ "package x;"
			+ "import java.util.*;"
			+ "import java.awt.*;"
			+ "class X extends List {}";
		builder.addSource(new StringReader(in));
		JavaClass cls = builder.getClassByName("x.X");
		assertEquals("java.util.List", cls.getSuperClass().getValue());
	}

	public void testAddMoreClassLoaders() throws Exception {
		MultipleJavaDocBuilder builder = new MultipleJavaDocBuilder();

		builder.getClassLibrary().addClassLoader(new ClassLoader() {
			public Class loadClass(String name) throws ClassNotFoundException {
				return name.equals("com.thoughtworks.Spoon") ? this.getClass() : null;
			}
		});

		builder.getClassLibrary().addClassLoader(new ClassLoader() {
			public Class loadClass(String name) throws ClassNotFoundException {
				return name.equals("com.thoughtworks.Fork") ? this.getClass() : null;
			}
		});

		String in = ""
			+ "package x;"
			+ "import java.util.*;"
			+ "import com.thoughtworks.*;"
			+ "class X {"
			+ " Spoon a();"
			+ " Fork b();"
			+ " Cabbage c();"
			+ "}";
		builder.addSource(new StringReader(in));

		JavaClass cls = builder.getClassByName("x.X");
		assertEquals("com.thoughtworks.Spoon", cls.getMethod(0).getReturns().getValue());
		assertEquals("com.thoughtworks.Fork", cls.getMethod(1).getReturns().getValue());
		// unresolved
		assertEquals("Cabbage", cls.getMethod(2).getReturns().getValue());

	}


}
