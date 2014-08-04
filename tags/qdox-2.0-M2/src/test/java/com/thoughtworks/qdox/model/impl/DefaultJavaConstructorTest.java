package com.thoughtworks.qdox.model.impl;

import static org.mockito.Mockito.*;
import java.util.List;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaConstructorTest;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.impl.DefaultJavaConstructor;


public class DefaultJavaConstructorTest extends JavaConstructorTest<DefaultJavaConstructor>
{

    @Override
    protected DefaultJavaConstructor newJavaConstructor( String name )
    {
        DefaultJavaConstructor result = new DefaultJavaConstructor();
        result.setName( name );
        return result;
    }
    
    @Override
    protected void setModifiers( DefaultJavaConstructor constructor, List<String> modifiers )
    {
        constructor.setModifiers( modifiers );
        
    }
    
    @Override
    protected void setParameters( DefaultJavaConstructor constructor, List<JavaParameter> parameters )
    {
        constructor.setParameters( parameters );
    }
    
    @Override
    protected void setParentClass( DefaultJavaConstructor constructor, JavaClass parentClass )
    {
        constructor.setParentClass( parentClass );
    }
}
