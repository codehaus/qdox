package com.thoughtworks.qdox.xml;

import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;
import java.util.Arrays;
import java.util.Collection;

public class JavaDocXmlGenerator {

	//---( Constants )---

	/** list of modifiers, to provide canonical ordering */
	private static final String[] POSSIBLE_MODIFIERS = {
		"public",
		"protected",
		"private",
		"static",
		"abstract",
		"final",
		"transient",
		"synchronized",
	};
	
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
		if (javaSource.getFile() != null) {
			addElement("file", javaSource.getFile().toString());
		}
		addElement("package", javaSource.getPackage());
		String[] imports = javaSource.getImports();
		for (int i = 0; i < imports.length; i++) {
			addElement("import", imports[i]);
		}
		JavaClass[] classes = javaSource.getClasses();
		for (int i = 0; i < classes.length; i++) {
			writeJavaClass(classes[i]);
		}
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
		Type[] interfaces = javaClass.getImplements();
		for (int i = 0; i < interfaces.length; i++) {
			addElement("implements", interfaces[i].getValue());
		}
		JavaField[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			writeJavaField(fields[i]);
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
		writeModifiers(javaEntity);
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

	void writeModifiers(AbstractJavaEntity javaEntity) {
		Collection modifierSet = Arrays.asList(javaEntity.getModifiers());
		for (int i = 0; i < POSSIBLE_MODIFIERS.length; i++) {
			String modifier = POSSIBLE_MODIFIERS[i];
			if (modifierSet.contains(modifier)) {
				addElement("modifier", modifier);
			}
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
		startElement("type");
		addElement("name", type.getValue());
		if (type.getDimensions() > 0) {
			addElement("dimensions", String.valueOf(type.getDimensions()));
		}
		endElement();
	}

	//---( Support routines )---

	private void startElement(String name) {
		if (name == null) throw new NullPointerException();
		handler.startElement(name);
	}

	private void addText(String text) {
		if (text == null) throw new NullPointerException();
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
