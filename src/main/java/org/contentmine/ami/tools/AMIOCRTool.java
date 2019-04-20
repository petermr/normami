package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.contentmine.eucl.euclid.IntArray;
import org.contentmine.eucl.euclid.IntegerMultiset;
import org.contentmine.eucl.euclid.IntegerMultisetList;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlBody;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGText;
import org.contentmine.graphics.svg.SVGUtil;
import org.contentmine.graphics.svg.text.SVGTextLine;
import org.contentmine.graphics.svg.text.SVGTextLineList;
import org.contentmine.image.ImageUtil;
import org.contentmine.image.ocr.HOCRReader;
import org.contentmine.norma.image.ocr.ImageToHOCRConverter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
	static final String RAW = "raw";
	private static final Double DESCENDER_FUDGE = 0.15;
	private static final int DELTA_Y = 3;
	private static final double EDGE_FRACT = 0.3;


	private static final String IMAGE_DOT = "image.";
	private static final Logger LOG = Logger.getLogger(AMIOCRTool.class);
	
	private enum LineDir {
		horiz,
		vert,
		both,
		none
	}


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

    @Option(names = {"--tesseract"},
    		arity = "1",
            description = "path for tesseract binary e.g. /usr/local/tesseract/",
            defaultValue = "/usr/local/bin/tesseract")
    private String tesseractPath = null;

    @Option(names = {"--extractlines"},
    		arity = "1",
            description = "extracts textlines ",
            defaultValue = "none"
            )
    private LineDir extractLines = null; 


	private File outputHOCRFile;
	private HtmlBody htmlBody;
	private IntegerMultisetList yBinList;
	private ImageToHOCRConverter imageToHOCRConverter;
	private Multimap<Integer, SVGText> textByYMap;
	private SVGTextLineList textLineList;


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
		System.out.println("extractlines        " + extractLines);
		System.out.println("html                " + outputHtml);
		System.out.println("maxsize             " + maxsize);
		System.out.println("scale               " + applyScale);
		System.out.println("scalefactor         " + scalefactor);
		System.out.println("scaledFilename      " + basename);
		System.out.println("tesseractPath       " + tesseractPath);
		System.out.println();
	}


    @Override
    protected void runSpecifics() {
    	if (processTrees()) { 
    	} else {
    		
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree");
	    }
    }

	protected void processTree() {
		System.out.println("cTree: "+cTree.getName());
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

	private void processRawImageDir(File rawImageDir) {
		if (rawImageDir == null || !rawImageDir.exists()) {
			throw new RuntimeException("cannot find imageDir: "+rawImageDir);
		}
		List<File> imageDirs = CMineGlobber.listSortedChildDirectories(rawImageDir);
		for (File imageDir : imageDirs) {
			processImageDir(imageDir);
		}
	}

	private void processImageDir(File imageDir) {
		File imageFile = getRawImageFile(imageDir);
//		LOG.debug(imageFile + ": " + imageFile.exists());
//		System.err.print(".");
		runOCR(imageFile);
		if (outputHtml) {
			createStructuredHtml();
			if (LineDir.horiz.equals(extractLines)) {
				try {
					File hocrDir = new File(imageDir, "hocr");
					File hocrRawDir = new File(hocrDir, "raw");
					File rawSvgFile = new File(hocrRawDir, "raw.svg");
					SVGTextLineList textLineList = this.createTextLineList(rawSvgFile);
					SVGSVG.wrapAndWriteAsSVG((SVGElement)textLineList.createSVGElement(), new File(hocrRawDir, "textLineList.svg"));
				} catch (FileNotFoundException e) {
					throw new RuntimeException("Cannot find file in ImageDir "+imageDir, e);
				}
			}
		}
	}

	private void createStructuredHtml() {
		HOCRReader hocrReader = new HOCRReader();
		if (outputHOCRFile == null || !outputHOCRFile.exists()) {
			throw new RuntimeException("Cannot find outputHOCRFile: "+outputHOCRFile);
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
		File svgFile = new File(outputTop, basename+"."+CTree.SVG);
//  		LOG.debug("svg file "+svgFile);
		SVGSVG.wrapAndWriteAsSVG(svgSvg, svgFile);
		htmlBody = hocrReader.getOrCreateHtmlBody();
		// debug
		try {
			if (outputHOCRFile.exists()) {
				File destFile = new File(outputTop, basename+".hocr.html");
				if (destFile.exists()) FileUtils.deleteQuietly(destFile);
				FileUtils.moveFile(outputHOCRFile, destFile);
//				LOG.debug("raw html "+destFile);
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
			XMLUtil.debug(htmlBody, new FileOutputStream(new File(outputTop, basename+".body.html")),1);
//			LOG.debug("raw html 1"+outputHOCRFile);
		} catch (IOException e) {
			throw new RuntimeException("cannot write file: "+outputHOCRFile, e);
		}
	}

	private void runOCR(File imageFile) {
		String basename = FilenameUtils.getBaseName(imageFile.toString());
		String newbasename = FilenameUtils.getBaseName(imageFile.getParentFile().toString());
		if (!imageFile.exists()) {
			System.err.println("!not exist "+newbasename+"!");
		} else {
			if (scalefactor != null || Boolean.TRUE.equals(applyScale)) {
				imageFile = scaleAndWriteFile(imageFile, newbasename);
			}
			System.out.println("["+newbasename+"]");
			File outputDir = new File(imageFile.getParentFile(), HOCR_DIR);
			// messy: tesseract filenames don't have html extension
			outputHOCRFile = new File(outputDir, basename);
			imageToHOCRConverter = new ImageToHOCRConverter();
			if (tesseractPath != null) {
				imageToHOCRConverter.setTesseractPath(tesseractPath);
			}
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
			image = ImageUtil.scaleImageScalr(scalefactor, image);
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
	
	private SVGTextLineList createTextLineList(File rawSvgFile) throws FileNotFoundException {
		SVGSVG svg = (SVGSVG) SVGUtil.parseToSVGElement(new FileInputStream(rawSvgFile));
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svg);
		createSortedYBinList(textList); 
//		LOG.debug(yBinList);
		textLineList = new SVGTextLineList();
		for (IntegerMultiset yBin : yBinList) {
			for (Integer y : yBin.getSortedValues()) {
				List<SVGText> textListY = new ArrayList<SVGText>(textByYMap.get(y));
				List<String> texts = SVGText.getTextStrings(textListY);
			}
			List<SVGText> binTextList = createTextList(textByYMap, yBin);
			SVGTextLine textLine = new SVGTextLine();
			for (SVGText binText : binTextList) {
				textLine.add(binText);
			}
//			System.out.println(">bl>"+SVGText.extractStrings(binTextList)) ;
//			System.out.println(">t>"+textLine) ;
			textLineList.add(textLine);
		}
//		System.out.println(">tll>"+textLineList.getText()) ;
		return textLineList;
	}

	private List<SVGText> createTextList(Multimap<Integer, SVGText> textByYMap, IntegerMultiset yBin) {
		List<SVGText> textList = new ArrayList<SVGText>();
		for (Integer i : yBin.elementSet()) {
			List<SVGText> textListY = new ArrayList<SVGText>(textByYMap.get(i));
			textList.addAll(textListY);
		}
		SVGText.sortByX(textList);
		return textList;
	}

	private IntegerMultisetList createSortedYBinList(List<SVGText> textList) {
		int deltaY = 10; // guess bin separation
		textByYMap = createNonEmptyTextMultimap(textList);
		IntArray yArray = createSortedYCoordinates(textByYMap);
		yBinList = new IntegerMultisetList();
		yBinList.createBins(yArray, deltaY);
		yBinList.mergeNeighbouringBins((int) (deltaY * EDGE_FRACT));
		yBinList.removeEmptyBins();
		Collections.sort(yBinList);
		return yBinList;
	}

	private IntArray createSortedYCoordinates(Multimap<Integer, SVGText> textMap) {
		List<Integer> keySet = new ArrayList<Integer>(textMap.keySet());
		Collections.sort(keySet);
		IntArray yArray = new IntArray();
		for (Integer y : keySet) {
			List<SVGText> entryTextList = new ArrayList<SVGText>(textMap.get(y));
			for (int i = 0; i < entryTextList.size(); i++) {
				yArray.addElement(y);
			}
		}
		return yArray;
	}

	private Multimap<Integer, SVGText> createNonEmptyTextMultimap(List<SVGText> textList) {
		Multimap<Integer, SVGText> textMap = ArrayListMultimap.create();
		for (SVGText text : textList) {
			String clazz =  text.getAttributeValue("class");
			if (clazz != null) {
				String txt = text.getText();
				if (txt != null && !"".equals(txt.trim())) {
					Integer y = new Integer((int)(double)text.getY());
					textMap.put(y, text);
				}
			}
		}
		return textMap;
	}
	
    public SVGTextLineList getTextLineList() {
		return textLineList;
	}

    public void writeTextLineList(File file) {
    	if (textLineList != null) {
    		SVGSVG.wrapAndWriteAsSVG((SVGElement) textLineList, file);
    	}
    }
}
