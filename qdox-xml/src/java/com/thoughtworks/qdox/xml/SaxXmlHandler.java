package com.thoughtworks.qdox.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Stack;

/// UNTESTED!

public class SaxXmlHandler implements XmlHandler {

	//~~~( Member variables )~~~

	private static final AttributesImpl EMPTY_ATTRIBUTES =
		new AttributesImpl();

	private Stack nodeStack = new Stack();
	private ContentHandler contentHandler;

	//~~~( Constructor )~~~

	public SaxXmlHandler(ContentHandler contentHandler) {
		this.contentHandler = contentHandler;
	}

	//~~~( Implement XmlHandler )~~~

	public void startDocument() {
		try {
			contentHandler.startDocument();
		} catch (SAXException e) {
			throw new XmlHandlerException(e);
		}
	}

	public void startElement(String name) {
		try {
			nodeStack.push(name);
			contentHandler.startElement("", name, name, EMPTY_ATTRIBUTES);
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

	public void endElement() {
		try {
			String name = (String) nodeStack.pop();
			contentHandler.endElement("", name, name);
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
