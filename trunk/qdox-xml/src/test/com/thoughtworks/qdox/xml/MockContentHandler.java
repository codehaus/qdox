package com.thoughtworks.qdox.xml;

import junit.framework.AssertionFailedError;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A simple ContentHandler that serialises events for later comparision.
 */
public class MockContentHandler implements ContentHandler {
    
    //---( Local variables )---
    
    StringBuffer buffer = new StringBuffer();
    
    //---( Buffer access )---
    
    public String getBuffer() {
        return buffer.toString();
    }

    //---( Implement ContentHandler )---
    
    public void setDocumentLocator(Locator locator) {
        throw new UnsupportedOperationException();
    }

    public void startDocument() throws SAXException {
        buffer.append("START-DOCUMENT\n");
    }

    public void endDocument() throws SAXException {
        buffer.append("END-DOCUMENT\n");
    }

    private void checkPrefixMapping(String namespaceUri,
                                    String localName) 
    {
        if (namespaceUri.length() > 0) {
            throw new AssertionFailedError(
                "expected empty namespaceUri, got < " 
                + namespaceUri + ">"
            );
        }
        if (localName.length() > 0) {
            throw new AssertionFailedError(
                "expected empty localName, got < " 
                + localName + ">"
            );
        }
    }
    
    public void startElement(String namespaceUri,
                             String localName, String qName,
                             Attributes attrs)
        throws SAXException 
    {
        checkPrefixMapping(namespaceUri, localName);
        if (attrs.getLength() > 0) {
            throw new AssertionFailedError(
                "no attributes should be generated"
            );
        }
        buffer.append("START " + qName + "\n");
    }

    public void endElement(String namespaceUri,
                           String localName, String qName)
        throws SAXException 
    {
        checkPrefixMapping(namespaceUri, localName);
        buffer.append("END " + qName + "\n");
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException 
    {
        buffer.append("\" " + new String(ch, start, length) + "\n");
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException 
    {
        throw new UnsupportedOperationException();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        throw new UnsupportedOperationException();
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException 
    {
        throw new UnsupportedOperationException();
    }

    public void processingInstruction(String target, String data)
        throws SAXException 
    {
        throw new UnsupportedOperationException();
    }

    public void skippedEntity(String name) throws SAXException {
        throw new UnsupportedOperationException();
    }

}
