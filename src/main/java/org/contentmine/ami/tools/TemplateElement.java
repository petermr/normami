package org.contentmine.ami.tools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import nu.xom.Element;

/** AMIImage template element
 * 
 * @author pm286
 *
 */
public class TemplateElement extends Element {


	private static final Logger LOG = Logger.getLogger(TemplateElement.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public static String TAG = "template";

	
	private TemplateElement(Element element) {
		super(element);
	}
	
	public static TemplateElement readTemplateElement(Element element) {
		TemplateElement templateElement = null;
		if (element != null) {
			if (TAG.contentEquals(element.getLocalName())) {
				templateElement = new TemplateElement(element);
			}
		}
		return templateElement;
	}

	public static TemplateElement read(Element tElement) {
		throw new RuntimeException("TemplateElement.read() NYI");
	}

}
