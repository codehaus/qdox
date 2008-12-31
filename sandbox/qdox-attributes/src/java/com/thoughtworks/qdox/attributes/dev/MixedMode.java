package com.thoughtworks.qdox.attributes.dev;

import com.thoughtworks.qdox.attributes.SimpleAttribute;
import com.thoughtworks.qdox.attributes.impl.SimpleBundle;


/**
 * Mixed mode for the attributes builder, tries to locate a class for each tag but if
 * none can be found, treats it as a simple string key/value pair without complaining.
 * Note that if a class is matched, it will attempt to create an instance as in object
 * mode and, should validation fail, it will <em>not</em> fall back on string mode.
 * Nonetheless, this mode should only be used to transition from string to object
 * modes, since it'll silently hide typos in tag names.
 * 
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 * @version $Revision$ ($Date$)
 */
public class MixedMode extends ObjectMode {
	protected void tagClassNotFound(String tag, String text, SimpleBundle bundle) {
		bundle.add(new SimpleAttribute(tag, text));
	}
}