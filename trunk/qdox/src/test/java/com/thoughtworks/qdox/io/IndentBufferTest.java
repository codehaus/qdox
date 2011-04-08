package com.thoughtworks.qdox.io;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class IndentBufferTest {

    private IndentBuffer buffer;

    @Before
    public void setUp() throws Exception {
        buffer = new IndentBuffer();
    }

    @Test
    public void testNoIndentation() throws Exception {
        buffer.write("A string");
        buffer.newline();
        buffer.write("more string");
        buffer.write('s');
        buffer.newline();
        String expected = ""
                + "A string\n"
                + "more strings\n";
        assertEquals(expected, buffer.toString());
    }

    @Test
    public void testIndentation() throws Exception {
        buffer.write("Line1");
        buffer.newline();
        buffer.indent();
        buffer.write("Indent1");
        buffer.newline();
        buffer.write("Indent2");
        buffer.write(" more");
        buffer.newline();
        buffer.deindent();
        buffer.write("Line2");
        buffer.newline();
        String expected = ""
                + "Line1\n"
                + "\tIndent1\n"
                + "\tIndent2 more\n"
                + "Line2\n";
        assertEquals(expected, buffer.toString());
    }
}
