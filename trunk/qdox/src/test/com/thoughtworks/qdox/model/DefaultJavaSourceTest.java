package com.thoughtworks.qdox.model;


public class DefaultJavaSourceTest extends JavaSourceTest<DefaultJavaSource>
{

    public DefaultJavaSourceTest( String s )
    {
        super( s );
    }

    public DefaultJavaSource newJavaSource(com.thoughtworks.qdox.library.ClassLibrary classLibrary)
    {
        return new DefaultJavaSource(classLibrary);
    }

    public JavaClass newJavaClass()
    {
        return new DefaultJavaClass();
    }

    public void setName( JavaClass clazz, String name )
    {
        ((DefaultJavaClass) clazz).setName( name );
    }

    public void addClass( JavaSource source, JavaClass clazz )
    {
        ((DefaultJavaClass) clazz).setSource( source );
        ((DefaultJavaSource) source).addClass( clazz );
    }

    public JavaPackage newJavaPackage( String name )
    {
        return new DefaultJavaPackage(name);
    }

    public void setPackage( DefaultJavaSource source, JavaPackage pckg )
    {
        source.setPackage( pckg );
    }

    public void addImport( JavaSource source, String imp )
    {
        ((DefaultJavaSource) source).addImport( imp );
    }
}
