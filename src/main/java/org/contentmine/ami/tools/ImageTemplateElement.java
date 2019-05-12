package org.contentmine.ami.tools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import nu.xom.Element;

public class ImageTemplateElement extends TemplateElement {


	private static final Logger LOG = Logger.getLogger(ImageTemplateElement.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public static final String TAG = "imageTemplate";

	private ImageTemplateElement(Element element) {
		super(element);
	}
	
	public static ImageTemplateElement createImageTemplateElement(Element element) {
		LOG.debug("ImageTemplateElement NYI");
		return null;
	}
}
