package org.contentmine.ami.tools;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.norma.image.ocr.HOCRConverter;


public class ImageDirProcessor {

	public static final Logger LOG = Logger.getLogger(ImageDirProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private CTree cTree;
	private File imageDir;
	public AbstractAMITool amiTool;

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
		System.out.println("image: "+imageDir.getName());
		String inputname = amiTool.getInputBasename();
		File imageFile = inputname != null ? new File(imageDir, inputname+".png") :
			AbstractAMITool.getRawImageFile(imageDir);
		amiTool.processImageDir(imageFile);
	}

}
