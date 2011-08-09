package com.thoughtworks.qdox.model;

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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.thoughtworks.qdox.library.ClassLibrary;

public class Type implements JavaClass, Serializable {

    public static final Type VOID = new Type("void");

    private String name;
    private JavaClassParent context;
    private String fullName;
    private int dimensions;
    private List<Type> actualArgumentTypes;
    
    public Type(String fullName, String name, int dimensions, JavaClassParent context) {
        this.fullName = fullName;
        this.name = name;
        this.dimensions = dimensions;
        this.context = context;
    }
    
    public Type(String fullName, int dimensions, JavaClassParent context) {
        this(fullName, (String) null, dimensions, context);
    }

    public Type(String fullName, int dimensions) {
        this(fullName, dimensions, null);
    }

    /**
     * Should only be used by primitives, since they don't have a classloader.
     * 
     * @param fullName the name of the primitive
     */
    public Type( String fullName ) 
    {
        this( fullName, 0 );
    }
    
	public static Type createUnresolved(String name, int dimensions, JavaClassParent context) {
        return new Type(null, name, dimensions, context);
    }
    
	public JavaClassParent getJavaClassParent() {
        return context;
    }

    /**
     * Returns the FQN of an Object or the handler of a Type.
     * If the name of the can't be resolved based on the imports and the classes on the classpath the name will be returned.
     * InnerClasses will use the $ sign.
     * If the type is an array, the brackets will be included. The get only the name, use {@link #getComponentType()}.
     * 
     * Some examples how names will be translated 
     * <pre>
     * Object > java.lang.Object
     * java.util.List > java.util.List
     * ?  > ?
     * T  > T
     * anypackage.Outer.Inner > anypackage.Outer$Inner
     * String[][] > java.lang.String[][]
     * </pre>
     * 
     * @return the fully qualified name, never <code>null</code>
     * @see #getComponentType()
     */
    public String getFullyQualifiedName() {
        StringBuffer result = new StringBuffer( isResolved() ? fullName : name );
        for (int i = 0; i < dimensions; i++) 
        {
            result.append("[]");
        }
        return result.toString();
    }

    /**
     * Equivalent of {@link Class#getComponentType()}
     * If this type is an array, return its component type
     * 
     * @return the type of array if it's one, otherwise <code>null</code>
     */
    public JavaClass getComponentType() {
      return isArray() ? resolveRealClass() : null;
    }
    
    /**
     * The FQN representation of an Object for code usage
     * This implementation ignores generics
     *
     * Some examples how Objects will be translated
     * <pre>
     * Object > java.lang.object
     * java.util.List<T> > java.util.List
     * ? > ?
     * T > T
     * anypackage.Outer.Inner > anypackage.Outer.Inner
     * </pre>
     * 
     * @return type representation for code usage
     */
    public String getValue() {
        return ( name != null ?  name : getFullyQualifiedName().replaceAll( "\\$", "." ) );
    }
    
    /**
     * The FQN representation of an Object for code usage
     * This implementation ignores generics
     *
     * Some examples how Objects will be translated
     * <pre>
     * Object > java.lang.object
     * java.util.List<T> > java.util.List
     * ? > ?
     * T > T
     * anypackage.Outer.Inner > anypackage.Outer.Inner
     * </pre>

     * @since 1.8
     * @return generic type representation for code usage 
     */
    public String getGenericValue()
    {
        StringBuffer result = new StringBuffer( getValue() );
        if ( actualArgumentTypes != null && actualArgumentTypes.size() > 0 )
        {
            result.append( "<" );
            for ( Iterator<Type> iter = actualArgumentTypes.iterator(); iter.hasNext(); )
            {
                result.append( iter.next().getGenericValue() );
                if ( iter.hasNext() )
                {
                    result.append( "," );
                }
            }
            result.append( ">" );
        }
        for ( int i = 0; i < dimensions; i++ )
        {
            result.append( "[]" );
        }
        return result.toString();
    }
    
    protected String getGenericValue( List<TypeVariable> typeVariableList )
    {
        StringBuffer result = new StringBuffer( getResolvedValue( typeVariableList ) );
        if ( actualArgumentTypes != null && actualArgumentTypes.size() > 0 )
        {
            for ( Iterator<Type> iter = actualArgumentTypes.iterator(); iter.hasNext(); )
            {
                result.append( iter.next().resolve( typeVariableList ) );
                if ( iter.hasNext() )
                {
                    result.append( "," );
                }
            }
        }
        return result.toString();
    }
    
    protected String getResolvedValue( List<TypeVariable> typeParameters )
    {
        String result = getValue();
        for ( TypeVariable typeParameter : typeParameters )
        {
            if ( typeParameter.getName().equals( getValue() ) )
            {
                result = typeParameter.getBounds().get( 0 ).getValue();
                break;
            }
        }
        return result;
    }
    
    protected TypeVariable resolve( List<TypeVariable> typeParameters )
    {
        TypeVariable result = null;
        // String result = getGenericValue(typeParameters);
        for ( TypeVariable typeParameter : typeParameters )
        {
            if ( typeParameter.getName().equals( getValue() ) )
            {
                result = typeParameter;
                break;
            }
        }
        return result;
    }

    protected boolean isResolved()
    {
        if ( fullName == null && context != null )
        {
            fullName = context.resolveType( name );
        }
        return ( fullName != null );
    }

    /**
     * Returns true if this Type is an array
     * 
     * @return true if this type is an array, otherwise <code>null</code>
     */
    public boolean isArray() {
        return dimensions > 0;
    }

    /**
     * Returns the depth of this array, 0 if it's not an array
     * 
     * @return The depth of this array, at least <code>0</code>
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * 
     * @return the actualTypeArguments or null
     */
    public List<Type> getActualTypeArguments()
    {
        return actualArgumentTypes;
    }
    
    public void setActualArgumentTypes( List<Type> actualArgumentTypes )
    {
        this.actualArgumentTypes = actualArgumentTypes;
    }
    
    /**
     * Equivalent of {@link Class#toString()}. 
     * Converts the object to a string.
     * 
     * @return a string representation of this type.
     * @see Class#toString()
     */
    public String toString()
    {
        return getFullyQualifiedName();
    }

    /**
     * Returns getGenericValue() extended with the array information
     * 
     * <pre>
     * Object > java.lang.Object
     * Object[] > java.lang.Object[]
     * List<Object> > java.lang.List<java.lang.Object>
     * Outer.Inner > Outer.Inner 
     * Outer.Inner<Object>[][] > Outer.Inner<java.lang.Object>[][] 
     * </pre>
     * @return a generic string representation of this type.
     */
    public String toGenericString() {
        return getGenericFullyQualifiedName();
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof Type ) )
        {
            return false;
        }
        Type t = (Type) obj;
        return getFullyQualifiedName().equals( t.getFullyQualifiedName() ) && t.getDimensions() == getDimensions();
    }

    @Override
    public int hashCode() {
        return getFullyQualifiedName().hashCode();
    }
    
    private JavaClass resolveRealClass() 
    {
        JavaClass result;
        String qualifiedName = isResolved() ? fullName : name;
        if ( isPrimitive( qualifiedName ) )
        {
            result = new DefaultJavaClass( qualifiedName );
        }
        else
        {
            JavaClassParent javaClassParent = getJavaClassParent();
            result = javaClassParent.getNestedClassByName( qualifiedName );
            if ( result == null )
            {
                result = getJavaClassLibrary().getJavaClass( qualifiedName, true );
            }
        }

        return result;
    }

    /**
     *  
     * @return this
     * @deprecated Type already has the JavaClass interface
     */
    public JavaClass getJavaClass()
    {
        return this;
    }

    /**
     * @since 1.3
     */
    public boolean isA( Type type )
    {
        if ( this == type )
        {
            return true;
        }
        else
        {
            return this.isA( type );
        }
    }

    /**
     * @since 1.6
     */
    public boolean isPrimitive() {
       return isPrimitive( getValue() );
    }
    
    private static boolean isPrimitive( String value )
    {
        return "void".equals(value)           
        || "boolean".equals(value)
        || "byte".equals(value)
        || "char".equals(value)
        || "short".equals(value)
        || "int".equals(value)
        || "long".equals(value)
        || "float".equals(value)
        || "double".equals(value);
        
    }

    /**
     * @since 1.6
     */
    public boolean isVoid() {
        return "void".equals(getValue());
    }

    protected Type resolve( JavaClass parentClass, JavaClass subclass )
    {
        Type result = this;

        int typeIndex = -1;
        for ( ListIterator<TypeVariable> iter = parentClass.getTypeParameters().listIterator(); iter.hasNext(); )
        {
            if ( iter.next().getFullyQualifiedName().equals( getFullyQualifiedName() ) )
            {
                typeIndex = iter.previousIndex();
                break;
            }
        }

        if ( typeIndex >= 0 )
        {
            String fqn = parentClass.getFullyQualifiedName();
            if ( subclass.getSuperClass() != null && fqn.equals( subclass.getSuperClass().getFullyQualifiedName() ) )
            {
                result = subclass.getSuperClass().getActualTypeArguments().get( typeIndex );
            }
            else if ( subclass.getImplementedInterfaces() != null )
            {
                for ( Type implement : subclass.getImplements() )
                {
                    if ( fqn.equals( implement.getFullyQualifiedName() ) )
                    {
                        JavaClass argument = implement.getJavaClass();
                        result = implement.getActualTypeArguments().get( typeIndex ).resolve( argument, argument );
                        break;
                    }
                }
                // no direct interface available, try indirect
            }
        }
        
        if ( this.actualArgumentTypes != null ) {
            result = new Type( this.fullName, this.name, this.dimensions, this.context );
            
            result.actualArgumentTypes = new LinkedList<Type>();
            for (Type actualArgType : getActualTypeArguments())
            {
                result.actualArgumentTypes.add(actualArgType.resolve( parentClass, subclass ));
            }
        }
        return result;
    }

    /**
     * 
     * @return a generic string representation of this type with fully qualified names.
     */
    public String getGenericFullyQualifiedName()
    {
        StringBuffer result = new StringBuffer( isResolved() ? fullName : name );
        if ( actualArgumentTypes != null && actualArgumentTypes.size() > 0 )
        {
            result.append( "<" );
            for ( Iterator<Type> iter = actualArgumentTypes.iterator(); iter.hasNext(); )
            {
                result.append( iter.next().getGenericFullyQualifiedName() );
                if ( iter.hasNext() )
                {
                    result.append( "," );
                }
            }
            result.append( ">" );
        }
        for ( int i = 0; i < dimensions; i++ )
        {
            result.append( "[]" );
        }
        return result.toString();
    }

    public String getResolvedGenericValue( List<TypeVariable> typeParameters )
    {
        StringBuffer result = new StringBuffer();
        TypeVariable variable = resolve( typeParameters );
        result.append( variable == null ? getValue() : variable.getBounds().get(0).getValue() );
        if ( actualArgumentTypes != null && actualArgumentTypes.size() > 0 )
        {
            result.append( "<" );
            for ( Iterator<Type> iter = actualArgumentTypes.iterator(); iter.hasNext(); )
            {
                result.append( iter.next().getGenericValue(typeParameters) );
                if ( iter.hasNext() )
                {
                    result.append( "," );
                }
            }
            result.append( ">" );
        }
        for ( int i = 0; i < dimensions; i++ )
        {
            result.append( "[]" );
        }
        return result.toString();
    }

    protected String getResolvedGenericFullyQualifiedName( List<TypeVariable> typeParameters )
    {
        StringBuffer result = new StringBuffer();
        TypeVariable variable = resolve( typeParameters );
        result.append( variable == null ? getFullyQualifiedName() : variable.getBounds().get(0).getFullyQualifiedName() );
        if ( actualArgumentTypes != null && actualArgumentTypes.size() > 0 )
        {
            result.append( "<" );
            for ( Iterator<Type> iter = actualArgumentTypes.iterator(); iter.hasNext(); )
            {
                result.append( iter.next().getResolvedFullyQualifiedName( typeParameters) );
                if ( iter.hasNext() )
                {
                    result.append( "," );
                }
            }
            result.append( ">" );
        }
        for ( int i = 0; i < dimensions; i++ )
        {
            result.append( "[]" );
        }
        return result.toString();
    }

    protected String getResolvedFullyQualifiedName( List<TypeVariable> typeParameters )
    {
        TypeVariable variable = resolve( typeParameters );
        return (variable == null ? getFullyQualifiedName() : variable.getBounds().get(0).getFullyQualifiedName() );
    }

    //Delegating methods
    public JavaSource getSource()
    {
        return resolveRealClass().getSource();
    }

    public int getLineNumber()
    {
        return resolveRealClass().getLineNumber();
    }

    public boolean isInterface()
    {
        return resolveRealClass().isInterface();
    }

    public List<Annotation> getAnnotations()
    {
        return resolveRealClass().getAnnotations();
    }

    public boolean isEnum()
    {
        return resolveRealClass().isEnum();
    }

    public String getComment()
    {
        return resolveRealClass().getComment();
    }

    public List<DocletTag> getTags()
    {
        return resolveRealClass().getTags();
    }

    public boolean isAnnotation()
    {
        return resolveRealClass().isAnnotation();
    }

    public List<DocletTag> getTagsByName( String name )
    {
        return resolveRealClass().getTagsByName( name );
    }

    public DocletTag getTagByName( String name )
    {
        return resolveRealClass().getTagByName( name );
    }

    public Type getSuperClass()
    {
        return resolveRealClass().getSuperClass();
    }

    public JavaClass getSuperJavaClass()
    {
        return resolveRealClass().getSuperJavaClass();
    }

    public List<Type> getImplements()
    {
        return resolveRealClass().getImplements();
    }

    public List<JavaClass> getImplementedInterfaces()
    {
        return resolveRealClass().getImplementedInterfaces();
    }
    
    public List<JavaClass> getInterfaces()
    {
        return resolveRealClass().getImplementedInterfaces();
    }

    public String getNamedParameter( String tagName, String parameterName )
    {
        return resolveRealClass().getNamedParameter( tagName, parameterName );
    }

    public String getCodeBlock()
    {
        return resolveRealClass().getCodeBlock();
    }

    public List<TypeVariable> getTypeParameters()
    {
        return resolveRealClass().getTypeParameters();
    }

    public JavaSource getParentSource()
    {
        return resolveRealClass().getParentSource();
    }

    public JavaPackage getPackage()
    {
        return resolveRealClass().getPackage();
    }

    public JavaClassParent getParent()
    {
        return resolveRealClass().getParent();
    }

    public String getPackageName()
    {
        return resolveRealClass().getPackageName();
    }

    public boolean isInner()
    {
        return resolveRealClass().isInner();
    }

    public String resolveType( String name )
    {
        return resolveRealClass().resolveType( name );
    }

    public String resolveCanonicalName( String name )
    {
        return resolveRealClass().resolveCanonicalName( name );
    }

    public String resolveFullyQualifiedName( String name )
    {
        return resolveRealClass().resolveFullyQualifiedName( name );
    }

    public String getClassNamePrefix()
    {
        return resolveRealClass().getClassNamePrefix();
    }

    public Type asType()
    {
        return resolveRealClass().asType();
    }

    public List<JavaMethod> getMethods()
    {
        return resolveRealClass().getMethods();
    }

    public List<JavaConstructor> getConstructors()
    {
        return resolveRealClass().getConstructors();
    }

    public JavaConstructor getConstructor( List<Type> parameterTypes )
    {
        return resolveRealClass().getConstructor( parameterTypes );
    }

    public JavaConstructor getConstructor( List<Type> parameterTypes, boolean varArg )
    {
        return resolveRealClass().getConstructor( parameterTypes, varArg );
    }

    public List<JavaMethod> getMethods( boolean superclasses )
    {
        return resolveRealClass().getMethods( superclasses );
    }

    public JavaMethod getMethodBySignature( String name, List<Type> parameterTypes )
    {
        return resolveRealClass().getMethodBySignature( name, parameterTypes );
    }

    public JavaMethod getMethod( String name, List<Type> parameterTypes, boolean varArgs )
    {
        return resolveRealClass().getMethod( name, parameterTypes, varArgs );
    }

    public JavaMethod getMethodBySignature( String name, List<Type> parameterTypes, boolean superclasses )
    {
        return resolveRealClass().getMethodBySignature( name, parameterTypes, superclasses );
    }

    public JavaMethod getMethodBySignature( String name, List<Type> parameterTypes, boolean superclasses, boolean varArg )
    {
        return resolveRealClass().getMethodBySignature( name, parameterTypes, superclasses, varArg );
    }

    public List<JavaMethod> getMethodsBySignature( String name, List<Type> parameterTypes, boolean superclasses )
    {
        return resolveRealClass().getMethodsBySignature( name, parameterTypes, superclasses );
    }

    public List<JavaMethod> getMethodsBySignature( String name, List<Type> parameterTypes, boolean superclasses,
                                                   boolean varArg )
    {
        return resolveRealClass().getMethodsBySignature( name, parameterTypes, superclasses, varArg );
    }

    public List<JavaField> getFields()
    {
        return resolveRealClass().getFields();
    }

    public JavaField getFieldByName( String name )
    {
        return resolveRealClass().getFieldByName( name );
    }

    public List<JavaClass> getClasses()
    {
        return resolveRealClass().getClasses();
    }

    public List<JavaClass> getNestedClasses()
    {
        return resolveRealClass().getNestedClasses();
    }

    public JavaClass getNestedClassByName( String name )
    {
        return resolveRealClass().getNestedClassByName( name );
    }

    public boolean isA( String fullClassName )
    {
        return resolveRealClass().isA( fullClassName );
    }

    public boolean isA( JavaClass javaClass )
    {
        return resolveRealClass().isA( javaClass );
    }

    public List<BeanProperty> getBeanProperties()
    {
        return resolveRealClass().getBeanProperties();
    }

    public List<BeanProperty> getBeanProperties( boolean superclasses )
    {
        return resolveRealClass().getBeanProperties( superclasses );
    }

    public BeanProperty getBeanProperty( String propertyName )
    {
        return resolveRealClass().getBeanProperty( propertyName );
    }

    public BeanProperty getBeanProperty( String propertyName, boolean superclasses )
    {
        return resolveRealClass().getBeanProperty( propertyName, superclasses );
    }

    public List<JavaClass> getDerivedClasses()
    {
        return resolveRealClass().getDerivedClasses();
    }

    public List<DocletTag> getTagsByName( String name, boolean superclasses )
    {
        return resolveRealClass().getTagsByName( name, superclasses );
    }

    public ClassLibrary getJavaClassLibrary()
    {
        return context.getJavaClassLibrary();
    }

    public String getName()
    {
        return resolveRealClass().getName();
    }

    public String getCanonicalName()
    {
        return resolveRealClass().getCanonicalName();
    }

    public List<String> getModifiers()
    {
        return resolveRealClass().getModifiers();
    }

    public boolean isPublic()
    {
        return resolveRealClass().isPublic();
    }

    public boolean isProtected()
    {
        return resolveRealClass().isProtected();
    }

    public boolean isPrivate()
    {
        return resolveRealClass().isPrivate();
    }

    public boolean isFinal()
    {
        return resolveRealClass().isFinal();
    }

    public boolean isStatic()
    {
        return resolveRealClass().isStatic();
    }

    public boolean isAbstract()
    {
        return resolveRealClass().isAbstract();
    }

}