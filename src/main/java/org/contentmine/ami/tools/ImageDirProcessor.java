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
	private File currentImageDir;
	public AbstractAMITool amiTool;
	private List<File> imageDirs;

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
		imageDirs = null;
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
				try {
					processImageDir(imageDir);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Cannot process imageDir: "+imageDir + e.getMessage());
				}
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
		this.currentImageDir = imageDir;
//		System.out.println("image: "+imageDir.getName());
		String inputname = amiTool.getInputBasename();
		File imageFile = inputname != null ? new File(currentImageDir, inputname+".png") :
			AbstractAMITool.getRawImageFile(imageDir);
		if (imageFile == null || !imageFile.exists()) {
			System.out.println("image file does not exist: "+imageFile);
			return;
		}
		amiTool.processImageDir(imageFile);
	}

}
