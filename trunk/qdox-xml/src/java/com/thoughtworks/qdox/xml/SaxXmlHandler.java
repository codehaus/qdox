package com.thoughtworks.qdox.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** 
 * An adaptor that allows the JavaDocXmlGenerator to produce SAX events
 */
 public class SaxXmlHandler implements XmlHandler {

	//---( Member variables )---

	private static final AttributesImpl EMPTY_ATTRIBUTES =
		new AttributesImpl();

	private ContentHandler contentHandler;

	//---( Constructor )---

	public SaxXmlHandler(ContentHandler contentHandler) {
		this.contentHandler = contentHandler;
	}

	//---( Implement XmlHandler )---

	public void startDocument() {
		try {
			contentHandler.startDocument();
		} catch (SAXException e) {
			throw new XmlHandlerException(e);
		}
	}

	public void startElement(String name) {
		try {
			contentHandler.startElement("", "", name, EMPTY_ATTRIBUTES);
		} catch (SAXException e) {
			throw new XmlHandlerException(e);
		}
	}

	public void addContent(String text) {
		try {
			contentHandler.characters(text.toCharArray(),
									  0, text.length());
		} catch (SAXException e) {
			throw new XmlHandlerException(e);
		}
	}

	public void endElement(String name) {
		try {
			contentHandler.endElement("", "", name);
		} catch (SAXException e) {
			throw new XmlHandlerException(e);
		}
	}

	public void endDocument() {
		try {
			contentHandler.endDocument();
		} catch (SAXException e) {
			throw new XmlHandlerException(e);
		}
	}

}
