package org.contentmine.ami.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIOCRTool.LineDir;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.text.SVGTextLineList;

public class ImageDirProcessor {

	private static final String TEXT_LINE_LIST_SVG = "textLineList.svg";
	private static final Logger LOG = Logger.getLogger(ImageDirProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private CTree cTree;
	private AbstractAMITool amiTool;
	private File imageDir;
	private AMIOCRTool amiocrTool;

	public ImageDirProcessor() {
		
	}

	public ImageDirProcessor(AbstractAMITool amiTool) {
		this();
		this.amiTool = amiTool;
		
	}

	public ImageDirProcessor(AbstractAMITool amiTool, CTree cTree) {
		this(amiTool);
		this.cTree = cTree;
	}

	void processImageDirs() {
		List<File> imageDirs = null;
		File rawImageDir = null;
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		if (pdfImagesDir != null && pdfImagesDir.exists()) {
			imageDirs = cTree.getPDFImagesImageDirectories();
		}
		if (imageDirs == null) {
			rawImageDir = cTree.getExistingImageDir();
		}
		if (imageDirs == null && rawImageDir == null) {
			LOG.warn("no pdfimages/ dir and no image/ dir");
			return;
		}
			
		if (imageDirs != null) {
			System.out.println("imageDirs: "+imageDirs.size());
			Collections.sort(imageDirs);
			for (File imageDir : imageDirs) {
				processImageDir(imageDir);
			}
		} else {
			processRawImageDir(rawImageDir);
		}
	}

	void processRawImageDir(File rawImageDir) {
		if (rawImageDir == null || !rawImageDir.exists()) {
			throw new RuntimeException("cannot find imageDir: "+rawImageDir);
		}
		List<File> imageDirs = CMineGlobber.listSortedChildDirectories(rawImageDir);
		for (File imageDir : imageDirs) {
			processImageDir(imageDir);
		}
	}

	void processImageDir(File imageDir) {
		this.imageDir = imageDir;
		File imageFile = AbstractAMITool.getRawImageFile(imageDir);
		amiocrTool = null;
		// this is crude; should use interfaces later!
		if (amiTool instanceof AMIOCRTool) {
			amiocrTool = (AMIOCRTool) amiTool;
			amiocrTool.runOCR(imageFile);
			if (amiocrTool.outputHtml) {
				amiocrTool.createStructuredHtml();
				if (LineDir.horiz.equals(amiocrTool.extractLines)) {
					try {
						File hocrRawDir = getHocrRawFilename(imageDir);
						File rawSvgFile = new File(hocrRawDir, "raw.svg");
						SVGTextLineList textLineList = amiocrTool.createTextLineList(rawSvgFile);
						textLineList.getOrCreateTypeAnnotations();
						SVGSVG.wrapAndWriteAsSVG((SVGElement)textLineList.createSVGElement(),
								getTextLineListFilename(imageDir));
					} catch (FileNotFoundException e) {
						throw new RuntimeException("Cannot find file in ImageDir "+imageDir, e);
					}
				}
			}
		}
	}

	public static File getTextLineListFilename(File imageDir) {
		File hocrRawDir = getHocrRawFilename(imageDir);
		return new File(hocrRawDir, TEXT_LINE_LIST_SVG);
	}

	private static File getHocrDirFilename(File imageDir) {
		File hocrDir = new File(imageDir, AMIOCRTool.HOCR);
		return hocrDir;
	}

	public static File getHocrRawFilename(File imageDir) {
		File hocrDir = getHocrDirFilename(imageDir);
		File hocrRawDir = new File(hocrDir, AMIOCRTool.RAW);
		return hocrRawDir;
	}
}
