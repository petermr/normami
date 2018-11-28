package org.contentmine.ami;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.CTreeList;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.image.ImageUtil;
import org.contentmine.norma.image.ocr.ImageToHOCRConverter;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class AMIImageProcessor {
	private static final Logger LOG = Logger.getLogger(AMIImageProcessor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public static final String DUPLICATES = "duplicates/";
	public static final String MONOCHROME = "monochrome/";
	public static final String SMALL = "small/";

	private CProject cProject;
	private CTree cTree;
	private int minWidth = 100;
	private int minHeight = 100;
	private boolean discardMonochrome;
	private boolean discardDuplicates;
	private Multiset<String> duplicateSet;
	private boolean extractPixelRings;
	private File cProjectOutputDir;
	private File cTreeOutputDir;
	

	public static void main(String[] args) {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));
		if (argList.size() == 0 || AMIProcessor.HELP.equals(argList.get(0))) {
			if (argList.size() > 0) argList.remove(0);
			AMIImageProcessor.runHelp(argList);
		} else {
			String projectName = argList.get(0);
			AMIImageProcessor amiIP = AMIImageProcessor.createAIProcessor(new CProject(new File(projectName)));
			argList.remove(0);
			amiIP.runImages(argList);
		}
	}

	private static void runHelp(List<String> argList) {
		DebugPrint.debugPrintln("ami-image <cproject> [commands]");
		DebugPrint.debugPrintln("    --threshold [t] // 0 < t < 255 (try ca 190)");
	}

	private void setParams(List<String> argList) {
		// NYI
	}

	public static AMIImageProcessor createAIProcessor(CProject cProject) {
		AMIImageProcessor amiIP = null;
		if (cProject != null) {
			amiIP = new AMIImageProcessor();
			amiIP.setCProject(cProject);
		}
		return amiIP;
	}

	public static AMIImageProcessor createAIProcessor(CTree cTree) {
		AMIImageProcessor amiIP = null;
		if (cTree != null) {
			amiIP = new AMIImageProcessor();
			amiIP.setCTree(cTree);
		}
		return amiIP;
	}

//	private static AMIImageProcessor createProcessor(File projectDir) {
//		AMIImageProcessor amiIP = null;
//		if (projectDir != null) {
//			amiIP = new AMIImageProcessor();
//			amiIP.setCProject(new CProject(projectDir));
//		}
//		return amiIP;
//	}

	private void setCProject(CProject cProject) {
		this.cProject = cProject;
	}

	private void setCTree(CTree cTree) {
		this.cTree = cTree;
	}

	public void runImages(CTree cTree) {
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		if (pdfImagesDir == null || !pdfImagesDir.exists()) {
			LOG.warn("no images dir");
		} else {
			duplicateSet = HashMultiset.create();
			List<File> imageFiles = new CMineGlobber("**/*.png", pdfImagesDir).listFiles();
			Collections.sort(imageFiles);
			for (File imageFile : imageFiles) {
				BufferedImage image = null;
				try {
					image = ImageIO.read(imageFile);
					if (moveSmallImageTo(image, imageFile, new File(pdfImagesDir, SMALL))) {
						
					} else if (discardMonochrome && moveMonochromeImagesTo(image, imageFile, new File(pdfImagesDir, MONOCHROME))) {
						
					} else if (discardDuplicates && moveDuplicateImagesTo(image, imageFile, new File(pdfImagesDir, DUPLICATES))) {
						
					};
				} catch(IOException e) {
					e.printStackTrace();
					LOG.debug("failed to read file " + imageFile + "; "+ e);
				}
			}
		}
	}
	
	/** runs processor from args
	 * 
	 * @param argList
	 */
	public void runImages(List<String> argList) {
		if (cProject != null) {
			setParams(argList);
			runImages();
		}
	}

	/** runs over cProject
	 * 
	 */
	public void runImages() {
		if (cProject != null) {
			CTreeList cTreeList = cProject.getOrCreateCTreeList();
			for (CTree cTree : cTreeList) {
				LOG.debug("tree "+cTree);
				runImages(cTree);
			}
		} else {
			LOG.warn(" no CProject");
		}
	}


	public void runImages(String projectName) {
		setCProject(new CProject(new File(projectName)));
		runImages(new ArrayList<String>());
	}
	

	private boolean moveSmallImageTo(BufferedImage image, File srcImageFile, File destDir) throws IOException {
		boolean createDestDir = true;
		if (image.getWidth() < minWidth || image.getHeight() < minHeight) {
			FileUtils.moveFileToDirectory(srcImageFile, destDir, createDestDir);
			return true;
		}
		return false;
	}

	private boolean moveMonochromeImagesTo(BufferedImage image, File srcImageFile, File destDir) throws IOException {
		boolean createDestDir = true;
		Integer singleColor = ImageUtil.getSingleColor(image);
		if (singleColor != null) {
			FileUtils.moveFileToDirectory(srcImageFile, destDir, createDestDir);
			return true;
		}
		return false;
	}

	private boolean moveDuplicateImagesTo(BufferedImage image, File srcImageFile, File destDir) throws IOException {
		boolean createDestDir = true;
		String hash = ""+image.getWidth()+"-"+image.getHeight()+"-"+ImageUtil.createSimpleHash(image);
		duplicateSet.add(hash);
		boolean moved = false;
		if (duplicateSet.count(hash) > 1) {
			if (srcImageFile != null && srcImageFile.exists() && !srcImageFile.isDirectory()) {
				try {
					File destFile = new File(destDir, srcImageFile.getName());
					if (destFile != null && destFile.exists()) {
						FileUtils.forceDelete(destFile);
					}
					FileUtils.moveFileToDirectory(srcImageFile, destDir, createDestDir);
					moved = true;
				} catch (FileExistsException fee) {
					throw new IOException("BUG: file should have been deleted"+srcImageFile, fee);
				}
			}
		}
		return moved;
	}

	public int getMinWidth() {
		return minWidth;
	}

	public AMIImageProcessor setMinWidth(int minWidth) {
		this.minWidth = minWidth;
		return this;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public AMIImageProcessor setMinHeight(int minHeight) {
		this.minHeight = minHeight;
		return this;
	}

	public boolean isDiscardDuplicates() {
		return discardDuplicates;
	}

	public AMIImageProcessor setDiscardDuplicates(boolean discardDuplicates) {
		this.discardDuplicates = discardDuplicates;
		return this;
	}

	public boolean isDiscardMonochrome() {
		return discardMonochrome;
	}

	public AMIImageProcessor setDiscardMonochrome(boolean discardMonochrome) {
		this.discardMonochrome = discardMonochrome;
		return this;
	}

//	public static ImageProcessor getOrCreateImageProcessor() {
//		ImageProcessor imageProcessor = new ImageProcessor();
//		return imageProcessor;
//	}

	public void convertImageAndWriteHOCRFiles(CTree cTree, File outputDir) {
		File imageDir = new File(cTree.getExistingPDFImagesDir(), CTree.DERIVED); 
		List<File> imageFiles = new CMineGlobber().setRegex(".*\\.png").setLocation(imageDir).listFiles();
		for (File imageFile : imageFiles) {
			String base = FilenameUtils.getBaseName(imageFile.toString());
			File outputBase = new File(outputDir, base);
			ImageToHOCRConverter imageToHOCRConverter = new ImageToHOCRConverter();
			imageToHOCRConverter.writeHOCRFile(imageFile, outputBase);
		}
	}


	public void setCProjectOutputDir(File dir) {
		this.cProjectOutputDir = dir;
	}

	public File getCProjectOutputDir() {
		return cProjectOutputDir;
	}

	public void setCTreeOutputDir(File outputDir) {
		cTreeOutputDir = outputDir;
	}

	public File getCTreeOutputDir() {
		return cTreeOutputDir;
	}



	
	

}
