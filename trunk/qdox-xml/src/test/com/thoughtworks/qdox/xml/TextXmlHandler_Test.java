package com.thoughtworks.qdox.xml;

import java.io.StringWriter;
import junit.framework.TestCase;

public class TextXmlHandler_Test extends TestCase {

	public TextXmlHandler_Test(String name) {
		super(name);
	}

	//---( Fixtures )---

	StringWriter buffer = new StringWriter();
	TextXmlHandler handler = new TextXmlHandler(buffer);

	//---( Utils )---

	void assertBuffered(String expected) {
		String actual = stripCarriageReturns(buffer.toString());
		if (!expected.equals(actual)) {
			fail("EXPECTED:\n" + expected + "\nBUT WAS:\n" + actual);
		}
	}

	String stripCarriageReturns(String s) {
		StringBuffer buffer = new StringBuffer();
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] != '\r') {
				buffer.append(chars[i]);
			}
		}
		return buffer.toString();
	}

	//---( Tests )---

	public void testStart() {
		handler.startElement("a");
		assertBuffered("<a>");
	}

	public void testEndWithoutStart() {
		try {
			handler.endElement();
			fail("IllegalStateException expected");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testStartThenEnd() {
		handler.startElement("a");
		handler.endElement();
		assertBuffered("<a></a>\n");
	}

	public void testNestedStarts() {
		handler.startElement("a");
		handler.startElement("b");
		handler.endElement();
		handler.endElement();
		assertBuffered("<a>\n<b></b>\n</a>\n");
	}

	public void testAddContent() {
		handler.addContent("xyz");
		assertBuffered("xyz");
	}

	/**
	 * Check that XML special-characters are handled
	 */
	public void testHandleHtmlContent() {
		handler.addContent("< > &");
		assertBuffered("&lt; &gt; &amp;");
	}

	public void testFullElement() {
		handler.startElement("a");
		handler.addContent("xyz");
		handler.endElement();
		assertBuffered("<a>xyz</a>\n");
	}

	public void testIndentation() {
		handler = new TextXmlHandler(buffer, "  ");
		handler.startElement("a");
		handler.startElement("b");
		handler.addContent("xyz");
		handler.endElement();
		handler.startElement("c");
		handler.startElement("d");
		handler.addContent("mno");
		handler.endElement();
		handler.endElement();
		handler.endElement();
		assertBuffered(
			"<a>\n" + 
			"  <b>xyz</b>\n" + 
			"  <c>\n" + 
			"    <d>mno</d>\n" + 
			"  </c>\n" + 
			"</a>\n"
		);
	}

}
