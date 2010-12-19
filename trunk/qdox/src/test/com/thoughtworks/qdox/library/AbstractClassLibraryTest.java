package com.thoughtworks.qdox.library;

import java.util.List;

import junit.framework.TestCase;

import com.thoughtworks.qdox.model.DefaultJavaClass;
import com.thoughtworks.qdox.model.JavaClass;

public class AbstractClassLibraryTest
    extends TestCase
{

    private AbstractClassLibrary nullClassLibrary = new AbstractClassLibrary()
    {
        protected JavaClass resolveJavaClass( String name )
        {
            return null;
        }
        
        protected boolean containsClassReference( String name )
        {
            return false;
        }
    };
    private AbstractClassLibrary parentClassLibrary;
    private AbstractClassLibrary filledChildClassLibrary;
    private AbstractClassLibrary emptyChildClassLibrary;
    
    protected void setUp()
        throws Exception
    {
        parentClassLibrary = new AbstractClassLibrary()
        {
            protected JavaClass resolveJavaClass( String name )
            {
                return new DefaultJavaClass(name);
            }
            
            protected boolean containsClassReference( String name )
            {
                throw new RuntimeException();
            }
        };
        
        filledChildClassLibrary = new AbstractClassLibrary(parentClassLibrary)
        {
            protected JavaClass resolveJavaClass( String name )
            {
                return new DefaultJavaClass(name);
            }
            
            protected boolean containsClassReference( String name )
            {
                throw new RuntimeException();
            }
        };
        emptyChildClassLibrary = new AbstractClassLibrary(parentClassLibrary)
        {
            protected JavaClass resolveJavaClass( String name )
            {
                return null;
            }
            
            protected boolean containsClassReference( String name )
            {
                throw new RuntimeException();
            }
        };
    }
    
    /*
     * Never null, empty by default
     */
    public void testGetJavaSources() throws Exception {
        assertEquals( 0, nullClassLibrary.getJavaSources().size() );
    }
    
    /*
     * Never null, empty by default
     */
    public void testGetJavaClasses() throws Exception {
        assertEquals( 0, nullClassLibrary.getJavaClasses().size() );
    }

    public void testGetJavaClassByName() throws Exception {
        assertEquals( null, nullClassLibrary.getJavaClass( "java.lang.String" ) );
        assertEquals( null, nullClassLibrary.getJavaClass( "com.thoughtworks.qdox.JavaProjectBuilder" ) );
    }
    
    public void testChainedJavaClass() throws Exception {
        //prepare libraries
        parentClassLibrary.getJavaClass( "ParentClass" );
        filledChildClassLibrary.getJavaClass( "ChildClass" );
        
        List<JavaClass> parentClasses = parentClassLibrary.getJavaClasses( new AbstractClassLibrary.ClassLibraryFilter(){
            public boolean accept(AbstractClassLibrary classLibrary) { return true; };
        } ); 
        assertEquals(1, parentClasses.size());
        assertEquals( "ParentClass", parentClasses.get(0).getFullyQualifiedName() );
        
        List<JavaClass> filledClasses = filledChildClassLibrary.getJavaClasses(new AbstractClassLibrary.ClassLibraryFilter(){
            public boolean accept(AbstractClassLibrary classLibrary) { return true; };
        } ); 
        assertEquals(2, filledClasses.size() );
        assertEquals( "ChildClass", filledClasses.get(0).getFullyQualifiedName() );
        assertEquals( "ParentClass", filledClasses.get(1).getFullyQualifiedName() );
        
        List<JavaClass> emptyClasses = emptyChildClassLibrary.getJavaClasses(new AbstractClassLibrary.ClassLibraryFilter(){
            public boolean accept(AbstractClassLibrary classLibrary) { return true; };
        } ); 
        assertEquals(1, emptyClasses.size() );
        assertEquals( "ParentClass", emptyClasses.get(0).getFullyQualifiedName() );
    }
    
    
    /*
     * Never null, empty by default
     */
    public void testGetJavaPackages() throws Exception {
        assertEquals( 0, nullClassLibrary.getJavaPackages().size() );
    }
    
    public void testGetJavaPackageByName() throws Exception {
        assertEquals( null, nullClassLibrary.getJavaPackage( "java.lang" ) );
        assertEquals( null, nullClassLibrary.getJavaPackage( "com.thoughtworks" ) );
    }
}