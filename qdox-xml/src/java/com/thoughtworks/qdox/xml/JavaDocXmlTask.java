package com.thoughtworks.qdox.xml;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Writer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

public class JavaDocXmlTask extends Task {

    //---( Constants )---

    static final String XML_VERSION_PREAMBLE = 
        "<?xml version=\"1.0\"?>";

    static final String QD0X_DTD_PREAMBLE = 
        "<!DOCTYPE qdox PUBLIC"
        + " \"-//codehaus.org//QDox 1.0//EN\""
        + " \"http://qdox.codehaus.org/dtd/qdox-1.0.dtd\">";
    
    //---( Arguments )---

    private Path inputJavaFiles;
    private File dest;
    private boolean writeDtd;

    //---( Config )---

    public void setProject(Project project) {
        super.setProject(project);
        inputJavaFiles = new Path(project);
    }

    /**
     * Nested &lt;fileset&gt; element
     */
    public void addConfiguredFileSet(FileSet fileSet) {
        inputJavaFiles.addFileset(fileSet);
    }

    /**
     * Set output file name
     */
    public void setDest(File dest) {
        this.dest = dest;
    }

    /**
     * Set the "writeDtd" attribute.  If true, the QDox DTD will be
     * included in the output.  Defaults to false.
     */
    public void setWriteDtd(boolean writeDtd) {
        this.writeDtd = writeDtd;
    }

    //---( Execution )---

    public void execute() {
        validateAttributes();
        String[] files = inputJavaFiles.list();
        if (upToDate(files)) return;
        processSources(parse(files));
    }

    protected void validateAttributes() {
        if (dest == null) {
            throw new BuildException("no \"dest\" specified");
        }
    }

    private boolean upToDate(String[] files) {
        if (! dest.exists()) return false;
        long destLastMod = dest.lastModified();
        for (int i = 0; i < files.length; i++) {
            long fileLastMod = new File(files[i]).lastModified();
            if (fileLastMod > destLastMod) return false;
        }
        return true;
    } 
    
    private JavaSource[] parse(String[] files) {
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.getClassLibrary().addClassLoader(getClass().getClassLoader());
        for (int i = 0; i < files.length; i++) {
            try {
                builder.addSource(new File(files[i]));
            } catch (FileNotFoundException e) {
                throw new BuildException(e);
            }
        }
        return builder.getSources();
    } 
    
    protected void processSources(JavaSource[] sources) {
        log("writing " + dest);
        Writer out = null;
        try {
            out = new FileWriter(dest);
            writePreamble(out);
            JavaDocXmlGenerator xmlGenerator =
                new JavaDocXmlGenerator(new TextXmlHandler(out, "  "));
            xmlGenerator.write(sources);
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    protected void writePreamble(Writer writer) throws IOException {
        PrintWriter out = new PrintWriter(writer);
        out.println(XML_VERSION_PREAMBLE);
        if (writeDtd) {
            out.println(QD0X_DTD_PREAMBLE);
        }
        out.flush();
    }
    
}
