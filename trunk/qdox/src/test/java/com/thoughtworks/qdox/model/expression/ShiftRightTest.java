package com.thoughtworks.qdox.model.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;


public class ShiftRightTest
{

    @Test
    public void testParameterValue()
    {
        AnnotationValue lhs = mock( AnnotationValue.class );
        AnnotationValue rhs = mock( AnnotationValue.class );
        when( lhs.getParameterValue() ).thenReturn( "2" );
        when( rhs.getParameterValue() ).thenReturn( "3" );
        ShiftRight expr = new ShiftRight( lhs, rhs );
        assertEquals( "2 >> 3", expr.getParameterValue() );
    }

    @Test
    public void testToString()
    {
        AnnotationValue lhs = mock( AnnotationValue.class );
        AnnotationValue rhs = mock( AnnotationValue.class );
        ShiftRight expr = new ShiftRight( lhs, rhs );
        assertEquals( lhs + " >> " + rhs, expr.toString() );
    }
    
    @Test
    public void testAccept()
    {
        AnnotationVisitor visitor = mock( AnnotationVisitor.class );
        ShiftRight expr = new ShiftRight( null, null );
        Object visitResult = new Object();
        when( visitor.visit( expr ) ).thenReturn( visitResult );
        assertSame( expr.accept( visitor ), visitResult );
    }
}
