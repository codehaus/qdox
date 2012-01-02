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

public class Assignment implements AnnotationValue
{
    private Expression leftHandSide;
    private String operator;
    private Expression assignmentExpression;

    public Assignment( Expression leftHandSide, String operator, Expression assignmentExpression )
    {
        this.leftHandSide = leftHandSide;
        this.operator = operator;
        this.assignmentExpression = assignmentExpression;
    }

    /** {@inheritDoc} */
    public Object accept( ExpressionVisitor visitor )
    {
        return visitor.visit( this );
    }

    /** {@inheritDoc} */
    public Object getParameterValue()
    {
        return leftHandSide.getParameterValue() + " " + operator + ' ' + assignmentExpression.getParameterValue();
    }

    @Override
    public String toString()
    {
        return leftHandSide.toString() + ' ' + operator+ ' ' + assignmentExpression.toString();
    }
}