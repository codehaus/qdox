package com.thoughtworks.qdox.xml;

import junit.framework.TestCase;

public class SaxXmlHandlerTest extends TestCase {

	public SaxXmlHandlerTest(String name) {
		super(name);
	}
	
	private MockContentHandler mockContentHandler = 
		new MockContentHandler();
	private SaxXmlHandler saxXmlHandler = 
		new SaxXmlHandler(mockContentHandler);
	
	public void testStartDocument() {
		saxXmlHandler.startDocument();
		assertEquals("START-DOCUMENT\n", 
					 mockContentHandler.getBuffer());
	}

	public void testEndDocument() {
		saxXmlHandler.endDocument();
		assertEquals("END-DOCUMENT\n", 
					 mockContentHandler.getBuffer());
	}

	public void testStartElement() {
		saxXmlHandler.startElement("foo");
		assertEquals("START foo\n", 
					 mockContentHandler.getBuffer());
	}

	public void testEndElement() {
		saxXmlHandler.endElement("foo");
		assertEquals("END foo\n", 
					 mockContentHandler.getBuffer());
	}

	public void testAddContent() {
		saxXmlHandler.addContent("blah");
		assertEquals("\" blah\n", 
					 mockContentHandler.getBuffer());
	}
	
}
