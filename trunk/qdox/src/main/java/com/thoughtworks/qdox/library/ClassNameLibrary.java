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

import com.thoughtworks.qdox.builder.ModelBuilder;
import com.thoughtworks.qdox.model.DefaultJavaPackage;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
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

    @Override
    protected JavaClass resolveJavaClass( String name )
    {
        ModelBuilder unknownBuilder = getModelBuilder();
        unknownBuilder.beginClass( new ClassDef( name ) );
        unknownBuilder.endClass();
        JavaSource unknownSource = unknownBuilder.getSource();
        JavaClass result = unknownSource.getClasses().get( 0 );
        return result;
    }
    
    @Override
    protected JavaPackage resolveJavaPackage(String name) {
    	return null;
    }
    
    @Override
    protected boolean containsClassReference( String name )
    {
        return false;
    }

}
