package com.thoughtworks.qdox.library;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import junit.framework.TestCase;

public abstract class ClassLibraryBuilderTest
    extends TestCase 
{

    protected abstract ClassLibraryBuilder getClassLibraryBuilder();

    public final void testAppendClassLoader() throws Exception
    {
        ClassLibraryBuilder libraryBuilder = getClassLibraryBuilder();
        ClassLibraryBuilder result = libraryBuilder.appendClassLoader( null );
        assertSame( libraryBuilder, result );
    }

    public final void testAppendSourceFolder() throws Exception
    {
        ClassLibraryBuilder libraryBuilder = getClassLibraryBuilder();
        ClassLibraryBuilder result = libraryBuilder.appendSourceFolder( null );
        assertSame( libraryBuilder, result );
    }

    public final void testAppendSourceInputStream() throws Exception
    {
        ClassLibraryBuilder libraryBuilder = getClassLibraryBuilder();
        ClassLibraryBuilder result = libraryBuilder.appendSource( new ByteArrayInputStream( new byte[]{} ) );
        assertSame( libraryBuilder, result );
    }

    public final void testAppendSourceReader() throws Exception
    {
        ClassLibraryBuilder libraryBuilder = getClassLibraryBuilder();
        ClassLibraryBuilder result = libraryBuilder.appendSource( new StringReader("") );
        assertSame( libraryBuilder, result );
    }

//    public final void testAppendSourceURL() throws Exception
//    {
//        ClassLibraryBuilder libraryBuilder = getClassLibraryBuilder();
//        ClassLibraryBuilder result = libraryBuilder.appendSource( (URL) null );
//        assertSame( libraryBuilder, result );
//    }
//
//    public final void testAppendSourceFile() throws Exception
//    {
//        ClassLibraryBuilder libraryBuilder = getClassLibraryBuilder();
//        ClassLibraryBuilder result = libraryBuilder.appendSource( (File) null );
//        assertSame( libraryBuilder, result );
//    }

    public final void testGetClassLibrary() throws Exception
    {
        ClassLibraryBuilder libraryBuilder = getClassLibraryBuilder();
        assertNotNull( libraryBuilder.getClassLibrary() );
    }
    
}
