package com.thoughtworks.qdox.attributes.dev;

import java.lang.reflect.*;
import java.util.Iterator;

import com.thoughtworks.qdox.attributes.Attributes;
import com.thoughtworks.qdox.attributes.Bundle;
import com.thoughtworks.qdox.attributes.impl.AttributesImplBase;

/**
 * Print the attributes of a list of elements to standard out.
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class ShowAttribs {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Attributes attribs = Attributes.getInstance();
		for (int i = 0; i < args.length; i++) {
			Class klass = Class.forName(args[i]);
			print(klass.getName(), attribs.get(klass));
			
			Field[] fields = klass.getFields();
			for (int j=0; j<fields.length; j++) {
				print(klass.getName() + "#" + fields[j].getName(), attribs.get(fields[j]));
			}
			
			Constructor[] constructors = klass.getConstructors();
			for (int j=0; j<constructors.length; j++) {
				StringBuffer buf = new StringBuffer();
				buf.append(klass.getName());
				buf.append('#');
				AttributesImplBase.appendParamTypes(buf, constructors[j].getParameterTypes());
				print(buf.toString(), attribs.get(constructors[j]));
			}

			Method[] methods = klass.getMethods();
			for (int j=0; j<methods.length; j++) {
				StringBuffer buf = new StringBuffer();
				buf.append(klass.getName());
				buf.append('#');
				buf.append(methods[j].getName());
				AttributesImplBase.appendParamTypes(buf, methods[j].getParameterTypes());
				print(buf.toString(), attribs.get(methods[j]));
			}
		}
	}
	
	private static void print(String elementName, Bundle bundle) {
		if (bundle.size() == 0) return;
		System.out.println(elementName + ":");
		for (Iterator it = bundle.iterator(); it.hasNext(); ) {
			System.out.println(it.next());
		}
		System.out.println();
	}
}
