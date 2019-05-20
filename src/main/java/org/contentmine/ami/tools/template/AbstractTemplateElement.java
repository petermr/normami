package org.contentmine.ami.tools.template;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.xml.XMLUtil;

import net.sf.saxon.functions.Abs;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

/** AMIImage template element
 * 
 * @author pm286
 *
 */
public abstract class AbstractTemplateElement extends Element {


	private static final Logger LOG = Logger.getLogger(AbstractTemplateElement.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public static String TAG = "template";
	
	protected File currentDir;

	
	protected AbstractTemplateElement(Element element) {
		super(element);
	}
	
	public AbstractTemplateElement(String tag) {
		super(tag);
	}

	public static AbstractTemplateElement createTemplateElement(Element element, File currentDir) {
		AbstractTemplateElement templateElement = null;
		if (element != null) {
			String tag = element.getLocalName();
			if (false) {
			} else if (TemplateElement.TAG.equals(tag)) {
				templateElement = new TemplateElement();
			} else if (ImageTemplateElement.TAG.equals(tag)) {
				templateElement = new ImageTemplateElement();
			} else {
				throw new RuntimeException("Unknown tag: "+tag);
			}
			if (templateElement != null) {
				templateElement.setCurrentDir(currentDir);
				XMLUtil.copyAttributes(element, templateElement);
				for (int i = 0; i < element.getChildCount(); i++) {
					Node child = element.getChild(i);
					Node newChild = (child instanceof Element) ? AbstractTemplateElement.createTemplateElement((Element)child, currentDir) :
						child.copy();
					if (newChild != null) {
						templateElement.appendChild(newChild);
					}
				}
			}

		}
		return templateElement;

	}

	public static AbstractTemplateElement readTemplateElement(File file, File currentDir) {
		LOG.debug(">>>>>>reading template: "+file);
		Element element = XMLUtil.parseQuietlyToRootElement(file);
		return AbstractTemplateElement.createTemplateElement(element, currentDir);
	}

	protected static String getNonNullAttributeValue(Element element, String attname) {
		String attval = element == null || attname == null ? null : element.getAttributeValue(attname);
		if (attval == null) {
			throw new RuntimeException("Must give "+attname+" attribute");
		}
		return attval;
	}

	public static AbstractTemplateElement readTemplateElement(File currentDir, String templateFilename) {
		AbstractTemplateElement templateElement = readTemplateElement(new File(currentDir, templateFilename), currentDir);
		if (templateElement != null) {
			templateElement.currentDir = currentDir;
		}
		return templateElement;
	}
	
	public void process() {
		Elements childElements = this.getChildElements();
		for (int i = 0; i < childElements.size(); i++) {
			Element childElement = childElements.get(i);
			if (childElement instanceof AbstractTemplateElement) {
				((AbstractTemplateElement) childElement).process();
			} else {
				LOG.debug("skipped non TemplateElement: "+childElement.getLocalName());
			}
		}
	}
	
	public File getCurrentDir() {
		return currentDir;
	}

	public void setCurrentDir(File currentDir) {
		this.currentDir = currentDir;
	}


	
	

}
