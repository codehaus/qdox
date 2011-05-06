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

public class Query implements AnnotationValue {

    private final AnnotationValue condition;
    private final AnnotationValue trueExpression;
    private final AnnotationValue falseExpression;

    public Query( AnnotationValue condition, AnnotationValue trueExpression, AnnotationValue falseExpression ) {
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

	public Object accept( AnnotationVisitor visitor ) {
        return visitor.visitAnnotationQuery( this );
    }

    public AnnotationValue getCondition() {
        return this.condition;
    }

    public AnnotationValue getTrueExpression() {
        return this.trueExpression;
    }

    public AnnotationValue getFalseExpression() {
        return this.falseExpression;
    }

    public Object getParameterValue() {
        return condition.getParameterValue().toString() + " ? " + trueExpression.getParameterValue() + " : "
            + falseExpression.getParameterValue();
    }

    public String toString() {
        return condition.toString() + " ? " + trueExpression.toString() + " : " + falseExpression.toString();
    }
}
