package com.thoughtworks.qdox.xml;

import com.thoughtworks.qdox.model.*;

import java.io.IOException;
import java.io.Writer;

public class JavaDocXmlGenerator {

	//---( Member variables )---

	private XmlHandler handler;

	//---( Constructor )---

	public JavaDocXmlGenerator(XmlHandler handler) {
		this.handler = handler;
	}

	//---( Public interface )---

	public void write(JavaSource[] javaSources) {
		handler.startDocument();
		startElement("qdox");
		for (int i = 0; i < javaSources.length; i++) {
			writeJavaSource(javaSources[i]);
		}
		endElement();
		handler.endDocument();
	}

	void writeJavaSource(JavaSource javaSource) {
		startElement("source");
		addElement("package", javaSource.getPackage());
		String[] imports = javaSource.getImports();
		for (int i = 0; i < imports.length; i++) {
			addElement("import", imports[i]);
		}
		JavaClass[] classes = javaSource.getClasses();
		for (int i = 0; i < classes.length; i++) {
			writeJavaClass(classes[i]);
		}
		// TODO: handle file?
		endElement();
	}

	void writeJavaClass(JavaClass javaClass) {
		startElement("class");
		writeAbstractJavaEntity(javaClass);
		if (javaClass.getSuperClass() != null) {
			String superClass = javaClass.getSuperClass().getValue();
			if (!superClass.equals("java.lang.Object")) {
				addElement("extends", superClass);
			}
		}
		JavaField[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			writeJavaField(fields[i]);
		}
		Type[] interfaces = javaClass.getImplements();
		for (int i = 0; i < interfaces.length; i++) {
			addElement("implements", interfaces[i].getValue());
		}
		JavaMethod[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			writeJavaMethod(methods[i]);
		}
		JavaClass[] classes = javaClass.getClasses();
		for (int i = 0; i < classes.length; i++) {
			writeJavaClass(classes[i]);
		}
		endElement();
	}

	void writeJavaField(JavaField javaField) {
		startElement("field");
		writeAbstractJavaEntity(javaField);
		addTypeInfo(javaField.getType());
		endElement();
	}

	void writeJavaMethod(JavaMethod javaMethod) {
		String nodeType =
			javaMethod.isConstructor() ? "constructor" : "method";
		startElement(nodeType);
		writeAbstractJavaEntity(javaMethod);
		addTypeInfo(javaMethod.getReturns());
		JavaParameter[] parameters = javaMethod.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			writeJavaParameter(parameters[i]);
		}
		Type[] exceptions = javaMethod.getExceptions();
		for (int i = 0; i < exceptions.length; i++) {
			addElement("exception", exceptions[i].getValue());
		}
		endElement();
	}

	void writeJavaParameter(JavaParameter javaParameter) {
		startElement("parameter");
		addElement("name", javaParameter.getName());
		addTypeInfo(javaParameter.getType());
		endElement();
	}

	void writeAbstractJavaEntity(AbstractJavaEntity javaEntity) {
		addElement("name", javaEntity.getName());
		String[] modifiers = javaEntity.getModifiers();
		for (int i = 0; i < modifiers.length; i++) {
			addElement("modifier", modifiers[i]);
		}
		if (javaEntity.getComment() != null
			&& javaEntity.getComment().length() > 0)
		{
			addElement("comment", javaEntity.getComment());
		}
		DocletTag[] tags = javaEntity.getTags();
		for (int i = 0; i < tags.length; i++) {
			writeDocletTag(tags[i]);
		}
	}

	void writeDocletTag(DocletTag tag) {
		startElement("tag");
		addElement("name", tag.getName());
		addElement("value", tag.getValue());
		endElement();
	}

	void addTypeInfo(Type type) {
		if (type == null) return;
		addElement("type", type.getValue());
		if (type.getDimensions() > 0) {
			addElement("dimensions", String.valueOf(type.getDimensions()));
		}
	}

	//---( Support routines )---

	private void startElement(String name) {
		handler.startElement(name);
	}

	private void addText(String text) {
		handler.addContent(text);
	}

	private void endElement() {
		handler.endElement();
	}

	private void addElement(String name, String value) {
		startElement(name);
		addText(value);
		endElement();
	}

}
