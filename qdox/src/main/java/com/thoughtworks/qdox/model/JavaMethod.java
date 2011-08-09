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

import java.util.List;

public interface JavaMethod extends JavaAnnotatedElement, JavaMember, JavaModel, JavaGenericDeclaration
{

    /**
     * 
     * @return the return type
     * @deprecated it is recommended to use getReturnType()
     */
    Type getReturns();

    List<JavaParameter> getParameters();

    JavaParameter getParameterByName( String name );

    List<Type> getExceptions();

    /**
     * Equivalent of {@link java.lang.reflect.Method#isVarArgs()}
     * 
     * @return <code>true</code> if this method was declared to take a variable number of arguments, 
     *          otherwise <code>false</code>
     */
    boolean isVarArgs();

    String getCodeBlock();

    /**
     * @since 1.3
     */
    String getDeclarationSignature( boolean withModifiers );

    /**
     * @since 1.3
     */
    String getCallSignature();

    /**
     * This method is NOT varArg aware.
     * 
     * @param name the name of the method
     * @param parameterTypes the parameter types of the method, can be <code>null</code>
     * @return <code>true</code> if this method matches the signature, otherwise <code>false</code>
     */
    boolean signatureMatches( String name, List<Type> parameterTypes );

    /**
     * @param name the name of the method
     * @param parameterTypes the parameter types, can be <code>null</code>
     * @param varArg <code>true</code> is signature should match a varArg-method, otherwise <code>false</code>
     * @return <code>true</code> if this method matches the signature, otherwise <code>false</code>
     */
    boolean signatureMatches( String name, List<Type> parameterTypes, boolean varArg );

    /**
     * Returns <code>true</code> if this method follows the bean convention of being an accessor.
     * 
     * <pre>
     *   public String getName();             // true
     *   public boolean isValid()             // true
     *   public String getName( String def ); // false, it has a parameter
     *   public String gettingUp();           // false, 'get' is not followed by an uppercase character
     *   public boolean isolate();            // false, 'is' is not followed by an uppercase character
     *   public static String getName();      // false, it is static
     * </pre>
     * 
     * @return <code>true</code> if this method is a Java Bean accessor, otherwise <code>false</code>
     * @since 1.3
     */
    boolean isPropertyAccessor();

    /**
     * Returns <code>true</code> if this method follows the bean convention of being an mutator.
     * 
     * <pre>
     *  public void setName(String name);        // true
     *  public void setUp();                     // false, it has no parameter
     *  public void settingUp(String def);       // false, 'set' is not followed by an uppercase character
     *  public static void setName(String name); // false, it is static
     * </pre>
     * 
     * @return <code>true</code> if this method is a Java Bean mutator, otherwise <code>false</code>
     * @since 1.3
     */
    boolean isPropertyMutator();

    /**
     * @return the type of the property this method represents, or <code>null</code> if this method
     * is not a property mutator or property accessor.
     * @since 1.3
     */
    Type getPropertyType();

    /**
     * @return the name of the property this method represents, or <code>null</code> if this method
     * is not a property mutator or property accessor.
     * @since 1.3
     */
    String getPropertyName();

    List<DocletTag> getTagsByName( String name, boolean inherited );

    /**
     * Get the original source code of the body of this method.
     *
     * @return Code as string.
     */
    String getSourceCode();

    List<TypeVariable> getTypeParameters();

    /**
     * Equivalent of java.lang.reflect.Method.getGenericReturnType()
     * 
     * @return the generic return type
     * @since 1.12
     */
    Type getGenericReturnType();

    /**
     * Equivalent of java.lang.reflect.Method.getReturnType()
     * 
     * @return the return type
     * @since 1.12
     */
    Type getReturnType();

    /**
     * If a class inherits this method from a generic class or interface, you can use this method to get the resolved return type
     * 
     * @param resolve define if generic should be resolved
     * @return the return type
     * @since 1.12
     */
    Type getReturnType( boolean resolve );

    /**
     * 
     * @return the parameter types
     * @since 1.12
     */
    List<Type> getParameterTypes();

    /**
     * If a class inherits this method from a generic class or interface, you can use this method to get the resolved parameter types
     * 
     * @param resolve
     * @return the parameter types
     * @since 1.12
     */
    List<Type> getParameterTypes( boolean resolve );

    JavaClass getParentClass();

    DocletTag getTagByName( String string, boolean b );
}