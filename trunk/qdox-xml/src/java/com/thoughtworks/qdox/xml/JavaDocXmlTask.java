package com.thoughtworks.qdox.xml;

import com.thoughtworks.qdox.ant.AbstractQdoxTask;
import com.thoughtworks.qdox.model.JavaSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.apache.tools.ant.BuildException;

public class JavaDocXmlTask extends AbstractQdoxTask {

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
			JavaDocXmlGenerator xmlGenerator =
				new JavaDocXmlGenerator(new TextXmlHandler(destWriter, "  "));
			xmlGenerator.write(sources);
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

}
