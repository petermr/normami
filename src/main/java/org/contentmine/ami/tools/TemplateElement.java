package org.contentmine.ami.tools;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.xml.XMLUtil;

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

	
	protected TemplateElement(Element element) {
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
		TemplateElement templateElement = null;
		List<Element> childElements = XMLUtil.getQueryElements(tElement, "./*");
		for (Element cElement : childElements) {
			TemplateElement childElement = TemplateElement.createTemplateElement(cElement);
		}
		return templateElement;
	}

	private static TemplateElement createTemplateElement(Element cElement) {
		String tag = cElement.getLocalName();
		if (ImageTemplateElement.TAG.equals(tag)) {
			return ImageTemplateElement.createImageTemplateElement(cElement);
		}
		return null;

	}
	
	

}
