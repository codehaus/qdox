package com.thoughtworks.qdox.model.expression;

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AnnotationValueList
    implements AnnotationValue
{

    private final List<AnnotationValue> valueList;

    public AnnotationValueList( List<AnnotationValue> valueList )
    {
        this.valueList = valueList;
    }

    public List<AnnotationValue> getValueList()
    {
        return valueList;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append( '{' );

        for ( Iterator<AnnotationValue> i = valueList.iterator(); i.hasNext(); )
        {
            buf.append( i.next().toString() );

            if ( i.hasNext() )
            {
                buf.append( ", " );
            }
        }

        buf.append( '}' );

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see com.thoughtworks.qdox.model.expression.AnnotationValue#accept(com.thoughtworks.qdox.model.expression.AnnotationVisitor)
     */
    public Object accept( AnnotationVisitor visitor )
    {
        return visitor.visit( this );
    }

    /*
     * (non-Javadoc)
     * @see com.thoughtworks.qdox.model.expression.AnnotationValue#getParameterValue()
     */
    public List<Object> getParameterValue()
    {
        List<Object> list = new LinkedList<Object>();

        for ( AnnotationValue value : valueList )
        {
            list.add( value.getParameterValue() );
        }

        return list;
    }
}
