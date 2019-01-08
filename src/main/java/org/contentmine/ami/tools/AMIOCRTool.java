package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlBody;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.image.ImageUtil;
import org.contentmine.image.ocr.HOCRReader;
import org.contentmine.norma.image.ocr.ImageToHOCRConverter;
import org.contentmine.norma.util.CommandRunner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Optical Character Recognition (OCR) of text
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-ocr", 
		//String[] aliases() default {};
aliases = "ocr",
		//Class<?>[] subcommands() default {};
version = "ami-ocr 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "Extracts text from OCR and (NYI) postprocesses HOCR output to create HTML."
)

public class AMIOCRTool extends AbstractAMITool {
	private static final String IMAGE_DOT = "image.";


	private static final Logger LOG = Logger.getLogger(AMIOCRTool.class);


	private static final String HOCR_DIR = "hocr";
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--filename"},
    		arity = "1",
            description = "name for transformed Imagefile")
    private String basename = "default";
    
    @Option(names = {"--html"},
    		arity = "0..1",
            description = "create structured html")
    private boolean outputHtml = true;

    @Option(names = {"--maxsize"},
    		arity = "1",
            description = "maximum size of small dimension after scaling")
    private Double maxsize = null;

    @Option(names = {"--scalefactor"},
    		arity = "1",
            description = "increase geometric scale - helps tesseract. Normally '--maxize' will autoscale, "
            		+ "but this alternative allows forcing scale")
    private Double scalefactor;

    @Option(names = {"--scale"},
    		arity = "0..1",
            description = "apply - helps tesseract to have larger images")
    private Boolean applyScale;


	private File derivedImagesDir;
	private File outputHOCRFile;
	private HtmlBody htmlBody;


	private ImageToHOCRConverter imageToHOCRConverter;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIOCRTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIOCRTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIOCRTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("html                " + outputHtml);
		System.out.println("maxsize             " + maxsize);
		System.out.println("scale               " + applyScale);
		System.out.println("scalefactor         " + scalefactor);
		System.out.println("scaledFilename      " + basename);
		System.out.println();
	}


    @Override
    protected void runSpecifics() {
    	if (processTrees()) { 
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree");
	    }
    }

	protected void processTree(CTree cTree) {
		System.out.println("cTree: "+cTree.getName());
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		if (pdfImagesDir == null || !pdfImagesDir.exists()) {
			LOG.warn("no pdfimages/ dir");
		} else {
			derivedImagesDir = cTree.getOrCreateDerivedImagesDir();
			List<File> imageFiles = CMineGlobber.listSortedChildFiles(derivedImagesDir, CTree.PNG);
			Collections.sort(imageFiles);
			for (File imageFile : imageFiles) {
				System.err.print(".");
				runOCR(imageFile);
				if (outputHtml) {
					createStructuredHtml();
				}
			}
		}
	}
	
	private void createStructuredHtml() {
		HOCRReader hocrReader = new HOCRReader();
		if (!outputHOCRFile.exists()) {
			throw new RuntimeException("Cannot find: "+outputHOCRFile);
		}
		String filename = outputHOCRFile.toString();
		try {
			InputStream inputStream = new FileInputStream(filename);
			// analyze the HOCR
			hocrReader.readHOCR(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("cannot read "+filename, e);
		}
		String basename = FilenameUtils.getBaseName(filename);
		//remove unnecessary "image."
		if (basename.startsWith(IMAGE_DOT)) basename = basename.substring(IMAGE_DOT.length());
		SVGSVG svgSvg = (SVGSVG) hocrReader.getOrCreateSVG();
		File parentFile = outputHOCRFile.getParentFile();
		File outputTop = new File(parentFile, basename);
		outputTop.mkdirs();
		SVGSVG.wrapAndWriteAsSVG(svgSvg, new File(outputTop, basename+"."+CTree.SVG));
		htmlBody = hocrReader.getOrCreateHtmlBody();
		// debug
		try {
			if (outputHOCRFile.exists()) {
				File destFile = new File(parentFile, basename+"."+CommandRunner.RAW_HTML);
				if (destFile.exists()) FileUtils.deleteQuietly(destFile);
				FileUtils.moveFile(outputHOCRFile, destFile);
			} else {
				System.err.println("html file does not exist "+outputHOCRFile);
			}
		} catch (FileExistsException e) {
			LOG.warn("dest file already exists: "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot move file ", e);
		}
		try {
			XMLUtil.debug(htmlBody, new FileOutputStream(outputHOCRFile),1);
		} catch (IOException e) {
			throw new RuntimeException("cannot write file: "+outputHOCRFile, e);
		}
	}

	private void runOCR(File imageFile) {
		String basename = FilenameUtils.getBaseName(imageFile.toString());
		if (!imageFile.exists()) {
			System.err.println("!not exist "+basename+"!");
		} else {
			if (scalefactor != null || Boolean.TRUE.equals(applyScale)) {
				imageFile = scaleAndWriteFile(imageFile, basename);
			}
			System.out.println("?"+basename+"?");
			File outputDir = new File(derivedImagesDir, HOCR_DIR);
			// messy: tesseract filenames don't have html extension
			outputHOCRFile = new File(outputDir, basename);
			imageToHOCRConverter = new ImageToHOCRConverter();
			try {
				// run the OCR and return HOCR
				outputHOCRFile = imageToHOCRConverter.convertImageToHOCR(imageFile, outputHOCRFile);
				if (!outputHOCRFile.exists()) {
					throw new RuntimeException("HOCR HTML should exist: "+outputHOCRFile);
				}
			} catch (Exception e) {
				throw new RuntimeException("cannot convert OCR", e);
			}
		}
	}

	private File scaleAndWriteFile(File imageFile, String basename) {
		File scaledFile = null;
		try {
			BufferedImage image = ImageIO.read(imageFile);
			if (applyScale != null && applyScale) {
				double height = image.getHeight();
				double width = image.getWidth();
				double scalex = maxsize / width;
				double scaley = maxsize / width;
				scalefactor = Math.max(scalex,  scaley);
			}
			image = ImageUtil.scaleImage(scalefactor, image);
			File parentFile = imageFile.getParentFile();
			File scaledDir  = new File(parentFile, basename);
			scaledDir.mkdirs();
			scaledFile = new File(scaledDir, basename +"."+CTree.PNG);
			ImageIO.write(image, CTree.PNG, scaledFile);
			imageFile =  scaledFile;
		} catch (IOException e) {
			throw new RuntimeException("Cannot write scaled file: "+scaledFile, e);
		}
		return imageFile;
	}
}
