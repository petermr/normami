package org.contentmine.ami;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.contentmine.norma.picocli.AbstractAMIProcessor;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** cleans some of all of the project.
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-image", 
		//String[] aliases() default {};
aliases = "image",
		//Class<?>[] subcommands() default {};
version = "ami-image 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "processes pdfimages - first pass. "
)

public class AMIImage extends AbstractAMIProcessor {
	private static final Logger LOG = Logger.getLogger(AMIImage.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--minheight"},
    		arity = "0..1",
    		defaultValue = "100",
            description = "minimum height (pixels) to accept")
    private int minHeight;

    @Option(names = {"--minwidth"},
    		arity = "0..1",
    		defaultValue = "100",
            description = "minimum width (pixels) to accept")
    private int minWidth;
    
    @Option(names = {"--smalldir"},
    		arity = "1",
    		defaultValue = "small",
            description = "directory for small images.")
	private File smallDirname;
    
    @Option(names = {"--monochrome"},
    		arity = "1",
    	    defaultValue = "true",
    		description = "discard monochrome images (i.r. only one color)")
	private boolean discardMonochrome;

    @Option(names = {"--monochromedir"},
    		arity = "1",
    		defaultValue = "monochrome",
    		description = "directory for monochrome images")
	private File monochromeDirname;

    @Option(names = {"--duplicates"},
    		arity = "1",
    		defaultValue = "true",
            description = "discard duplicate images ")
	private boolean discardDuplicates;

    @Option(names = {"--duplicatedir"},
    		arity = "1",
    		defaultValue = "duplicates",
            description = "directory for duplicates.")
	private File duplicateDirname;

	public static final String DUPLICATES = "duplicates/";
	public static final String MONOCHROME = "monochrome/";
	public static final String SMALL = "small/";

	private Multiset<String> duplicateSet;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIImage(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIImage() {
	}
	
    public static void main(String[] args) throws Exception {
    	AMIImage amiCleaner = new AMIImage();
    	amiCleaner.runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("minHeight           " + minHeight);
		System.out.println("minWidth            " + minWidth);
		System.out.println("smalldir            " + smallDirname);
		System.out.println("discardMonochrome   " + discardMonochrome);
		System.out.println("monochromeDir       " + monochromeDirname);
		System.out.println("discardDuplicates   " + discardDuplicates);
		System.out.println("duplicateDir        " + duplicateDirname);
	}


    @Override
    protected void runSpecifics() {
    	if (cProject == null) {
    		DebugPrint.debugPrint(Level.ERROR, "no CProject given");
    	} else {
    		CTreeList treeList = cProject.getOrCreateCTreeList();
    		for (CTree cTree : treeList) {
    			runImages(cTree);
    		}
    	}
    	
    }

	private void runImages(CTree cTree) {
		System.out.println("cTree: "+cTree.getName());
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		if (pdfImagesDir == null || !pdfImagesDir.exists()) {
			LOG.warn("no pdfimages/ dir");
		} else {
			duplicateSet = HashMultiset.create();
			List<File> imageFiles = new CMineGlobber("**/*.png", pdfImagesDir).listFiles();
			Collections.sort(imageFiles);
			for (File imageFile : imageFiles) {
				System.err.print(".");
				String basename = FilenameUtils.getBaseName(imageFile.toString());
				BufferedImage image = null;
				try {
					image = ImageIO.read(imageFile);
					if (false) {
					} else if (moveSmallImageTo(image, imageFile, new File(pdfImagesDir, SMALL))) {
						System.out.println("small: "+basename);
					} else if (discardMonochrome && moveMonochromeImagesTo(image, imageFile, new File(pdfImagesDir, MONOCHROME))) {
						System.out.println("monochrome: "+basename);
					} else if (discardDuplicates && moveDuplicateImagesTo(image, imageFile, new File(pdfImagesDir, DUPLICATES))) {
						System.out.println("duplicate: "+basename);
					};
				} catch(IndexOutOfBoundsException e) {
					LOG.error("BUG: failed to read: "+imageFile);
				} catch(IOException e) {
					e.printStackTrace();
					LOG.debug("failed to read file " + imageFile + "; "+ e);
				}
			}
		}
	}

	private boolean moveSmallImageTo(BufferedImage image, File srcImageFile, File destDir) throws IOException {
		boolean createDestDir = true;
		int width = image.getWidth();
		int height = image.getHeight();
		if (width < minWidth || height < minHeight) {
			return moveFileIfNotExists(srcImageFile, destDir, createDestDir);
		}
		return false;
	}

	private boolean moveFileIfNotExists(File srcImageFile, File destDir, boolean createDestDir) throws IOException {
		try {
			FileUtils.moveFileToDirectory(srcImageFile, destDir, createDestDir);
			return true;
		} catch (FileExistsException fee) {
			// probably benign - left over from previous run
//				LOG.warn("file exists, BUG?"+srcImageFile);
		}
		return false;
	}

	private boolean moveMonochromeImagesTo(BufferedImage image, File srcImageFile, File destDir) throws IOException {
		boolean createDestDir = true;
		Integer singleColor = ImageUtil.getSingleColor(image);
		if (singleColor != null && srcImageFile.exists()) {
			moveFileIfNotExists(srcImageFile, destDir, createDestDir);
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
					try {
						FileUtils.moveFileToDirectory(srcImageFile, destDir, createDestDir);
					} catch (FileExistsException e) {
						// skip file exists
					} catch (FileNotFoundException fnfe) {
						LOG.warn("BUG? (FileNotFound) for "+srcImageFile);
					}
					moved = true;
				} catch (FileExistsException fee) {
					throw new IOException("BUG: file should have been deleted"+srcImageFile, fee);
				}
			}
		}
		return moved;
	}


}
