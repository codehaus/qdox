package com.thoughtworks.qdox.xml;

// This is a simplified version of the SAX ContentHandler interface,
// sufficient for the purposes of dealing with QDox serialization.

/**
 * Interface provided by objects that can serialize JavaDoc information as
 * XML (or in an XML-like form).
 */
public interface XmlHandler {

	void startDocument();

	void startElement(String name);

	void addContent(String text);

	void endElement();

	void endDocument();

}
