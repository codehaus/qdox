package com.thoughtworks.qdox.xml;

import com.thoughtworks.qdox.ant.AbstractQdoxTask;
import com.thoughtworks.qdox.model.JavaSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.apache.tools.ant.BuildException;

public class JavaDocXmlTask extends AbstractQdoxTask {

	static final String XML_PREAMBLE = 
		"<?xml version=\"1.0\"?>\n" +
		"<!DOCTYPE qdox PUBLIC"
		+ " \"-//codehaus.org//QDox 1.0//EN\""
		+ " \"http://qdox.codehaus.org/dtd/qdox-1.0.dtd\">\n";
	
	private File dest;

	public void setDest(File dest) {
		this.dest = dest;
	}

	protected void validateAttributes() {
		super.validateAttributes();
		if (dest == null) {
			throw new BuildException("no \"dest\" specified");
		}
	}

	protected void processSources(JavaSource[] sources) {
		try {
			Writer destWriter = new FileWriter(dest);
			destWriter.write(XML_PREAMBLE);
			JavaDocXmlGenerator xmlGenerator =
				new JavaDocXmlGenerator(new TextXmlHandler(destWriter, "  "));
			xmlGenerator.write(sources);
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

}
