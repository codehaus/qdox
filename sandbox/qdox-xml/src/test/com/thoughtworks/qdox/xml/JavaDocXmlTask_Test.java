package com.thoughtworks.qdox.xml;

import java.io.File;
import java.io.FileWriter;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;

public class JavaDocXmlTask_Test extends TestCase {

    //---( Constructor )---
    
    public JavaDocXmlTask_Test(String name) {
        super(name);
    }

    //---( Fixtures )---
    
    File tmpDir = new File("target/tmp");
    File javaDir = new File(tmpDir, "java");
    File outputFile = new File(tmpDir, "out/out.xml");
    
    JavaDocXmlTask task;

    public void setUp() {
        clearTmpDir();
        outputFile.getParentFile().mkdirs();
        task = createTask();
    } 

    public void tearDown() {
        clearTmpDir();
    } 
    
    void clearTmpDir() {
        Delete delete = new Delete();
        delete.setProject(new Project());
        delete.setDir(tmpDir);
        delete.execute();        
    }

    JavaDocXmlTask createTask() {
        JavaDocXmlTask task = new JavaDocXmlTask();
        task.setProject(new Project());
        FileSet fileSet = new FileSet();
        fileSet.setDir(javaDir);
        task.addConfiguredFileSet(fileSet);
        task.setDest(outputFile);
        return task;
    }
    
    //---( Utils )---
    
    private File getJavaClassFile(String classFullName) {
        String fileName = classFullName.replace('.','/') + ".java";
        return new File(javaDir, fileName);
    }

    private void makeJavaClass(String classFullName) throws Exception {
        int lastDot = classFullName.lastIndexOf('.');
        String packageName = classFullName.substring(0, lastDot);
        String className = classFullName.substring(lastDot+1);

        File file = getJavaClassFile(classFullName);
        file.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(file);
        writer.write("package " + packageName + ";\n\n");
        writer.write("public class " + className + " {\n\n  // empty\n\n}\n");
        writer.close();
    }

    void pause() throws InterruptedException {
        Thread.sleep(1000);    
    }
    
    void assertNewer(File f1, File f2) {
        long f1LastMod = f1.lastModified();
        long f2LastMod = f2.lastModified();
        assertTrue(
            "expected > " + f1LastMod +", got " + f2LastMod,
            f2LastMod > f1LastMod
        ); 
    }

    //---( Tests )---
    
    public void testRunOnEmpty() throws Exception {
        assertTrue(!outputFile.exists());
        makeJavaClass("x.A");
        makeJavaClass("x.B");
        task.execute();
        assertTrue(
            "outputFile was not generated",
            outputFile.exists()
        );
    }

    public void testDoNothingIfSourceUnchanged() throws Exception {
        makeJavaClass("x.A");
        task.execute();
        long origTime = outputFile.lastModified();
        pause();
        task.execute();
        assertEquals(
            "outputFile was re-generated unnecessarily",
            origTime, outputFile.lastModified()
        );
    }

    public void testRegenerateIfSourceChanged() throws Exception {
        makeJavaClass("x.A");
        task.execute();
        long origTime = outputFile.lastModified();
        pause();
        makeJavaClass("x.A");
        task.execute();
        assertTrue(
            "outputFile was not re-generated",
            outputFile.lastModified() > origTime 
        );
    }

}


