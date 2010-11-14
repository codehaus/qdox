package com.thoughtworks.qdox.library;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.ModelBuilder;
import com.thoughtworks.qdox.parser.structs.ClassDef;

/**
 * This library always resolve a className by generating an empty JavaClass Model
 * 
 * @author Robert Scholte
 * @since 2.0
 */
public class ClassNameLibrary
    extends AbstractClassLibrary
{

    public ClassNameLibrary()
    {
    }

    /**
     * {@inheritDoc}
     */
    protected JavaClass resolveJavaClass( String name )
    {
        ModelBuilder unknownBuilder = getModelBuilder();
        ClassDef classDef = new ClassDef();
        classDef.name = name;
        unknownBuilder.beginClass( classDef );
        unknownBuilder.endClass();
        JavaSource unknownSource = unknownBuilder.getSource();
        JavaClass result = unknownSource.getClasses().get( 0 );
        return result;
    }
    
    protected boolean containsClassReference( String name )
    {
        return false;
    }

}
