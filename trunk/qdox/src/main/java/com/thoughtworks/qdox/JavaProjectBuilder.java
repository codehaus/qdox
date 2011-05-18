package com.thoughtworks.qdox;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.qdox.directorywalker.DirectoryScanner;
import com.thoughtworks.qdox.directorywalker.FileVisitor;
import com.thoughtworks.qdox.directorywalker.SuffixFilter;
import com.thoughtworks.qdox.library.ClassLibraryBuilder;
import com.thoughtworks.qdox.library.ErrorHandler;
import com.thoughtworks.qdox.library.OrderedClassLibraryBuilder;
import com.thoughtworks.qdox.library.SortedClassLibraryBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * This is the improved version of the JavaDocBuilder of QDox 1.x, which has the following tasks:
 * <ul>
 *   <li>Provide adders for all kind of resources, such as classloaders, java files and source directories</li>
 *   <li>Provide setters to enable the debug-mode for the Lexer and Parser (which are used when parsing sourcefiles) and the encoding
 *   <li>Provide getter for retrieving Java Object Models from these libraries, such as JavaSources, JavaClasses and JavaPackages</li>
 *   <li>Provide a method to search through all the parsed JavaClasses </li>
 *   <li>Provide store and load methods for the JavaProjectBuilder</li> 
 * </ul>
 * 
 * By default the JavaProjectBuilder will use the {@link} SortedClassLibraryBuilder}, which means it doesn't matter in which order you add the resources,
 * first all sources and sourcefolders, followed by the classloaders. Another implementation for the ClassLibraryBuilder is the
 * {@link OrderedClassLibraryBuilder}, which preserves the order in which resources are added. 
 * By creating a new JavaProjectBuilder with your own ClassLibraryBuilder you can decide which loading strategy should be used.  
 * 
 * @author Robert Scholte
 * @since 2.0
 */
public class JavaProjectBuilder
{
    private final ClassLibraryBuilder classLibraryBuilder;
    
    // Constructors
    
    /**
     * Default constructor, which will use the {@link SortedClassLibraryBuilder} implementation
     */
    public JavaProjectBuilder()
    {        
        this.classLibraryBuilder = new SortedClassLibraryBuilder();
    }

    /**
     * Custom constructor, so another resource loading strategy can be defined
     * 
     * @param classLibraryBuilder custom implementation of {@link ClassLibraryBuilder}
     */
    public JavaProjectBuilder(ClassLibraryBuilder classLibraryBuilder)
    {        
        this.classLibraryBuilder = classLibraryBuilder;
    }

    // Lexer and Parser -setters
    
    /**
     * Enable the debugmode for the Lexer
     * 
     * @param debugLexer <code>true</code> to enable, <code>false</code> to disable
     * @return This javaProjectBuilder itself 
     */
    public JavaProjectBuilder setDebugLexer( boolean debugLexer )
    {
        classLibraryBuilder.setDebugLexer( debugLexer );
        return this;
    }

    /**
     * Enable the debugmode for the Parser
     * 
     * @param debugParser <code>true</code> to enable, <code>false</code> to disable
     * @return This javaProjectBuilder itself
     */
    public JavaProjectBuilder setDebugParser( boolean debugParser )
    {
        classLibraryBuilder.setDebugParser( debugParser );
        return this;
    }

    /**
     * Sets the encoding when using Files or URL's to parse.
     * 
     * @param encoding the encoding to use for {@link File} or ({@link URL}
     * @return this javaProjectBuilder itself
     */
    public JavaProjectBuilder setEncoding( String encoding )
    {
        classLibraryBuilder.setEncoding( encoding );
        return this;
    }
    
    /**
     * Sets the errorHandler which will be triggered when a parse exception occurs.
     * 
     * @param errorHandler the errorHandler
     * @return this javaProjectBuilder itself
     */
    public JavaProjectBuilder setErrorHandler( ErrorHandler errorHandler) {
        classLibraryBuilder.setErrorHander( errorHandler );
        return this;
    }

    /**
     * Add a java file to this JavaProjectBuilder
     * 
     * @param file a java file
     * @return the {@link JavaSource} of the parsed file 
     * @throws IOException
     */
    public JavaSource addSource(File file) throws IOException 
    {
        return classLibraryBuilder.addSource( file );
    }
    
    // Resource adders
    
    public JavaSource addSource( Reader reader ) 
    {
        return classLibraryBuilder.addSource( reader );
    }

    /**
     * Add a sourcefolder to this javaprojectbuilder, but don't parse any file.
     * This is a lazy parser. 
     * Only if a JavaClass is called it will be searched by matching the package with the folder structure and the classname with the filename
     * 
     * @see {@link #addSourceTree(File)}
     * @param sourceFolder the sourcefolder to add
     */
    public void addSourceFolder( File sourceFolder )
    {
        classLibraryBuilder.appendSourceFolder( sourceFolder );
    }

    
    /**
     * Add all java files of the {@value directory} recursively
     * 
     * @param directory the directory from which all java files should be parsed.
     */
    public void addSourceTree( File directory )
    {
        FileVisitor visitor = new FileVisitor() {
            public void visitFile(File badFile) {
                throw new RuntimeException("Cannot read file : " + badFile.getName());
            }
        };
        addSourceTree(directory, visitor);        
    }

    /**
     * Add all java files of the {@value directory} recursively
     * 
     * @param directory the directory from which all java files should be parsed.
     * @param errorHandler a fileVisitor which will be triggered when an {@link IOException} occurs.
     */
    public void addSourceTree( File directory, final FileVisitor errorHandler )
    {
        DirectoryScanner scanner = new DirectoryScanner(directory);
        scanner.addFilter(new SuffixFilter(".java"));
        scanner.scan(new FileVisitor() {
            public void visitFile(File currentFile) {
                try {
                    addSource(currentFile);
                } catch (IOException e) {
                    errorHandler.visitFile(currentFile);
                }
            }
        });
    }
    
    /**
     * Add the classLoader to this JavaProjectBuilder
     * 
     * @param classLoader
     */
    public void addClassLoader( ClassLoader classLoader )
    {
        classLibraryBuilder.appendClassLoader( classLoader );
    }

    // Java Object Model -getters

    /**
     * Try to retrieve a JavaClass by its name.
     * 
     * @param name the fully qualified name of the class
     * @return the matching {@link JavaClass}, otherwise <code>null</code>
     */
    public JavaClass getClassByName( String name )
    {
        return classLibraryBuilder.getClassLibrary().getJavaClass( name );
    }
    
    /**
     * Get all the sources added.
     * This will only contain the sources added as sourcefile, sourcetree or sourcefolder.
     * 
     * @return a list of sources
     * @see {@link #addSource(File)}
     * @see {@link #addSource(Reader)}
     * @see {@link #addSourceFolder(File)}
     * @see {@link #addSourceTree(File)}
     */
    public List<JavaSource> getSources() {
        return classLibraryBuilder.getClassLibrary().getJavaSources();
    }

    /**
     * Retrieve all classes which were added by sources
     * 
     * @return a list of javaclasses, never <code>null</code>
     * @see {@link #addSource(File)}
     * @see {@link #addSource(Reader)}
     * @see {@link #addSourceFolder(File)}
     * @see {@link #addSourceTree(File)}
     */
    public List<JavaClass> getClasses()
    {
        return classLibraryBuilder.getClassLibrary().getJavaClasses();
    }

    /**
     * 
     * @param name
     * @return
     */
    public JavaPackage getPackageByName( String name )
    {
        return classLibraryBuilder.getClassLibrary().getJavaPackage( name );
    }

    /**
     * Retrieve all packages which were added by sources.
     * 
     * @return a list of packages, never <code>null</code>
     * @see {@link #addSource(File)}
     * @see {@link #addSource(Reader)}
     * @see {@link #addSourceFolder(File)}
     * @see {@link #addSourceTree(File)}
     */
    public List<JavaPackage> getPackages()
    {
        return classLibraryBuilder.getClassLibrary().getJavaPackages();
    }

    // Searcher
    
    public List<JavaClass> search( Searcher searcher )
    {
        List<JavaClass> result = new LinkedList<JavaClass>();
        List<JavaClass> classArray = classLibraryBuilder.getClassLibrary().getJavaClasses();
        for (int classIndex = 0;classIndex < classArray.size(); classIndex++) {
            JavaClass cls = classArray.get(classIndex);
            if (searcher.eval(cls)) {
                result.add(cls);
            }
        }
        return result;
    }

    /**
     * Persist the classLibraryBuilder to a file
     * 
     * @param file the file to serialize to
     * @throws IOException Any exception thrown by the underlying OutputStream
     */
    public void save( File file ) throws IOException
    {
        FileOutputStream fos = new FileOutputStream( file );
        ObjectOutputStream out = new ObjectOutputStream( fos );
        try
        {
            out.writeObject( classLibraryBuilder );
        }
        finally
        {
            out.close();
            fos.close();
        }
    }

    /**
     * Note that after loading JavaDocBuilder classloaders need to be re-added.
     */
    public static JavaProjectBuilder load(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fis);
        JavaProjectBuilder builder;
        try {
            ClassLibraryBuilder libraryBuilder = (ClassLibraryBuilder) in.readObject();
            builder = new JavaProjectBuilder(libraryBuilder);
        } catch (ClassNotFoundException e) {
            throw new Error("Couldn't load class : " + e.getMessage());
        } finally {
            in.close();
            fis.close();
        }
        return builder;
    }
}
