package com.thoughtworks.qdox.xml;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * A simple {@link XmlHandler} that produces XML text.
 */
public class TextXmlHandler implements XmlHandler {

    // This avoids having to deal with SAX, and finding a fully-fledged
    // SAX-based XML serializer.

    //---( Member variables )---

    private PrintWriter out;
    private String indentPrefix;
    private int nestingLevel;
    private boolean onNewLine = true;

    //---( Constructors )---

    public TextXmlHandler(Writer out, String indentPrefix) {
        this.out = new PrintWriter(out);
        this.indentPrefix = indentPrefix;
    }

    public TextXmlHandler(Writer out) {
        this(out, "");
    }

    //---( Implement XmlHandler )---

    public void startDocument() {
    }

    public void startElement(String name) {
        if (!onNewLine) {
            out.println();
        }
        indent();
        out.print('<');
        out.print(name);
        out.print('>');
        onNewLine = false;
        nestingLevel++;
    }

    public void addContent(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '<':   out.print("&lt;");  break;
                case '>':   out.print("&gt;");  break;
                case '&':   out.print("&amp;"); break;
                default:    out.print(chars[i]);
            }
        }
    }

    public void endElement(String name) {
        nestingLevel--;
        if (nestingLevel < 0) {
            throw new IllegalStateException();
        }
        if (onNewLine) {
            indent();
        }
        out.println("</" + name + ">");
        onNewLine = true;
    }

    public void endDocument() {
        out.flush();
    }

    //---( Support methods )---

    private void indent() {
        for (int i = 0; i < nestingLevel; i++) {
            out.print(indentPrefix);
        }
    }

}
