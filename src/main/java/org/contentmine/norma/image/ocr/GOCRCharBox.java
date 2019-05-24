package org.contentmine.norma.image.ocr;

import java.awt.image.BufferedImage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.eucl.euclid.Int2Range;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGImage;
import org.contentmine.graphics.svg.SVGRect;
import org.contentmine.graphics.svg.SVGText;

/** holds the output of GOCR combined with the underlying image.
 * requires parentG to have children
 *     SVGRect (from GOCR), 
 *     SVGText (with GOCR char)
 *     SVGImage (clipped to fit box)
 * 
 * @author pm286
 *
 */
public class GOCRCharBox {

	
	private static final Logger LOG = Logger.getLogger(GOCRCharBox.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private Int2 bboxSize;
	private SVGG parentG;
	private SVGText svgText;
	private SVGRect svgRect;
	private Int2Range boundingBox;
	private SVGImage svgImage;

	private GOCRCharBox() {
	}

	/** create from <g> that has svgText, svgRect, and svgImage children
	 * 
	 * @param parentG
	 * @return
	 */
	public static GOCRCharBox createFrom(SVGG parentG) {
		GOCRCharBox charBox = null;
		if (parentG != null) {
			SVGText svgText = (SVGText) XMLUtil.getSingleElement(parentG, "*[local-name()='"+SVGText.TAG+"']");
			SVGRect svgRect = (SVGRect) XMLUtil.getSingleElement(parentG, "*[local-name()='"+SVGRect.TAG+"']");
			SVGImage svgImage = (SVGImage) XMLUtil.getSingleElement(parentG, "*[local-name()='"+SVGImage.TAG+"']");
			if (svgText != null && svgRect != null) {
				charBox = new GOCRCharBox();
				charBox.parentG = parentG;
				charBox.svgText = svgText;
				charBox.svgRect = svgRect;
				charBox.svgImage = svgImage;
				charBox.boundingBox = svgRect.createIntBoundingBox();
			} else {
				LOG.debug(parentG.toXML());
				throw new RuntimeException("need rect and text children");
			}
		}
		return charBox;
	}

	 
	public Int2 getBoundingBoxSize() {
		bboxSize = new Int2(boundingBox.getXRange().getRange(), boundingBox.getYRange().getRange());
		return bboxSize;
	}

	public SVGG getParentG() {
		return parentG;
	}

	public SVGText getSvgText() {
		return svgText;
	}

	public SVGRect getSvgRect() {
		return svgRect;
	}

	public Int2Range getBoundingBox() {
		return boundingBox;
	}

	public SVGImage getSvgImage() {
		return svgImage;
	}
	
	public BufferedImage getBufferedImage() {
		return svgImage == null ? null : svgImage.getBufferedImage();
	}
	
	public String toString() {
		String s = svgText.getText() +";" + boundingBox;
		return s;
	}
}
