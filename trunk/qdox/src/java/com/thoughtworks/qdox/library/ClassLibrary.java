package com.thoughtworks.qdox.library;

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
import java.util.List;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * Main methods of a ClassLibrary, which can be used by every Model 
 * 
 * @author Robert Scholte
 * @since 2.0
 */
public interface ClassLibrary
    extends Serializable
{
    /**
     * Check if this library holds a reference based on the name.
     * 
     * @param name the Fully Qualified Name trying to resolve
     * @return true if the classLibrary has a reference
     */
    public boolean hasClassReference( String name );

    /**
     * Get the JavaClass or null if it's not possible
     * 
     * @param name The fully qualified name of the JavaClass
     * @return The JavaClass or null
     */
    public JavaClass getJavaClass( String name );

    public JavaClass getJavaClass( String name, boolean createStub );

    
    /**
     * Return all JavaClasses of the current library.
     * It's up to the library to decide if also collects JavaClasses from it's ancestors 
     * 
     * @return all JavaClasses as a List, never <code>null</code>
     */
    public List<JavaClass> getJavaClasses();
    
    /**
     * Return all JavaSources of the current library.
     * It's up to the library to decide if also collects JavaSources from it's ancestors 
     * 
     * @return all JavaSources as a List, never <code>null</code>
     */
    public List<JavaSource> getJavaSources();
    
    /**
     * Get the JavaPackage or null if it's not possible
     * 
     * @param name The fully qualified name of the JavaPackage
     * @return The JavaPackage or null
     */
    public JavaPackage getJavaPackage( String name );

    /**
     * Return all JavaPackages of the current library.
     * It's up to the library to decide if also collects JavaPackages from it's ancestors 
     * 
     * @return all JavaPackages as a List, never <code>null</code>
     */
    public List<JavaPackage> getJavaPackages();
}
