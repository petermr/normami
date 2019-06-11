package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.eucl.euclid.IntArray;
import org.contentmine.eucl.euclid.IntegerMultiset;
import org.contentmine.eucl.euclid.IntegerMultisetList;
import org.contentmine.graphics.html.HtmlBody;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGImage;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGText;
import org.contentmine.graphics.svg.SVGUtil;
import org.contentmine.graphics.svg.text.SVGTextLine;
import org.contentmine.graphics.svg.text.SVGTextLineList;
import org.contentmine.image.ImageUtil;
import org.contentmine.norma.image.ocr.CharBoxList;
import org.contentmine.norma.image.ocr.GOCRConverter;
import org.contentmine.norma.image.ocr.HOCRConverter;
import org.contentmine.norma.image.ocr.TextLineAnalyzer;

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
	public static final String RAW = "raw";
	public static final String HOCR = "hocr";
	public static final String GOCR = "gocr";
	// fraction of font that descender goes below baseline , approximate
	private static final Double DESCENDER_FUDGE = 0.15;
	
//	private static final int DELTA_Y = 3;
	// amount of bin to consider for overlap
	private static final double EDGE_FRACT = 0.3;
	// minimum gap in bin to split into 2 bins
	private static final double SPLIT_FRACT = 0.4;



	public static final String IMAGE_DOT = "image.";
	
	public static final Logger LOG = Logger.getLogger(AMIOCRTool.class);

	/** not yet used*/
	public enum LineDir {
		horiz,
		vert,
		both,
		none
	}
	public enum OcrType {
		gocr,
		hocr
	}
	
	private static final String GOCR_DIR = OcrType.gocr.toString();
	private static final String HOCR_DIR = OcrType.hocr.toString();
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
    @Option(names = {"--filename"},
    		arity = "1",
            description = "name for transformed Imagefile. no default")
	public String basename = null;
    
    @Option(names = {"--gocr"},
    		arity = "1",
            description = "path for running gocr"
//            defaultValue = "/usr/local/bin/gocr"
            )
    private String gocrPath = null;
    
    @Option(names = {"--html"},
    		arity = "0..1",
            description = "create structured html") 
    boolean outputHtml = true;

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
            description = "path for tesseract binary e.g. /usr/local/tesseract/"
//            defaultValue = "/usr/local/bin/tesseract"
            )
    private String tesseractPath = null;

    @Option(names = {"--extractlines"},
    		arity = "1..*",
            description = "extracts textlines from gocr and/or hocr "
//            defaultValue = "none"
            )
	public List<OcrType> extractLines = new ArrayList<OcrType>(); 


	public File outputHOCRFile;
	public HtmlBody htmlBody;
	private IntegerMultisetList yBinList;
	private HOCRConverter hocrConverter;
	private Multimap<Integer, SVGText> textByYMap;
	private SVGTextLineList textLineList;
	private String newbasename;
	public File imageFile;


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
		System.out.println("gocr                " + gocrPath);
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
		System.out.println("OCR cTree: "+cTree.getName());
		ImageDirProcessor imageDirProcessor = new ImageDirProcessor(this, cTree);
		imageDirProcessor.processImageDirs();
		
	}

	/** this is called from ImageDirProcessor ? move it
	 * 
	 * @param imageFile
	 */
	void runOCR(File imageFile) {
		LOG.debug("image file "+imageFile);
		
		this.imageFile = imageFile;
		basename = FilenameUtils.getBaseName(imageFile.toString());
		newbasename = FilenameUtils.getBaseName(imageFile.getParentFile().toString());
		if (!imageFile.exists()) { 
			System.err.println("image file does not exist "+newbasename);
			return;
		}
		File imageDir = imageFile.getParentFile();
		if (gocrPath != null) {
			GOCRConverter gocrConverter = new GOCRConverter();
			try {
				gocrConverter.setImageFile(imageFile);
				gocrConverter.runGOCR();
			} catch (Exception e) {
				LOG.error("Cannot run GOCR", e);
				return;
			}	
			processGOCR(imageDir);
		} else if (tesseractPath != null) {
			runTesseract(imageFile, basename, newbasename);
			if (outputHtml) {
				HOCRConverter converter = new HOCRConverter(this);
				converter.processTesseractOutput(getHocrDirectory(imageFile));
			}
			processHOCR(imageDir);
		}
		if (extractLines.contains(OcrType.gocr)) {
			processGOCR(imageDir);
		}
		if (extractLines.contains(OcrType.hocr)) {
			processHOCR(imageDir);
		}
	}

	private void runTesseract(File imageFile, String basename, String newbasename) {
		if (scalefactor != null || Boolean.TRUE.equals(applyScale)) {
			imageFile = scaleAndWriteFile(imageFile, newbasename);
		}
		File outputDir = getHocrDirectory(imageFile);
		// messy: tesseract filenames don't have html extension
		outputHOCRFile = outputDir;
		hocrConverter = new HOCRConverter(this);
		hocrConverter.setTesseractPath(tesseractPath);

		try {
			// run the OCR and return HOCR
			outputHOCRFile = hocrConverter.convertImageToHOCR(imageFile, outputHOCRFile);
			if (!outputHOCRFile.exists()) {
				throw new RuntimeException("HOCR HTML should exist: "+outputHOCRFile);
			}
		} catch (Exception e) {
			throw new RuntimeException("cannot convert OCR", e);
		}
	}

	/**
	 * from imageDir/file.png creates imageDir/hocr/
	 * is this a good thing?
	 * 
	 * @param imageFile
	 * @return
	 */
	private File getHocrDirectory(File imageFile) {
		File imageFileDir = new File(imageFile.getParentFile(), FilenameUtils.getBaseName(imageFile.toString()));
		imageFileDir.mkdirs();
		File imageFileHocrDir = new File(imageFileDir, HOCR_DIR);
		imageFileHocrDir.mkdirs();
		return imageFileHocrDir;
	}

	private File scaleAndWriteFile(File imageFile, String basename) {
		File scaledFile = null;
		try {
			BufferedImage image = ImageUtil.readImage(imageFile);
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
	
	public SVGTextLineList createTextLineList(File rawSvgFile) throws FileNotFoundException {
		SVGSVG svg = (SVGSVG) SVGUtil.parseToSVGElement(new FileInputStream(rawSvgFile));
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svg);
		createSortedYBinList(textList); 
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
			textLineList.add(textLine);
		}
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
		yBinList.createMultisets(yArray, deltaY);
		yBinList.splitMultisets((int) (deltaY * SPLIT_FRACT));
		yBinList.mergeNeighbouringMultisets((int) (deltaY * EDGE_FRACT));
		yBinList.removeEmptyMultisets();
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

	public File processHOCR(File processedImageDir) {
		File hocrSVGFile = AMIOCRTool.makeOcrOutputFilename(processedImageDir, basename, AMIOCRTool.HOCR, CTree.SVG);
		SVGElement hocrSVG1 = SVGElement.readAndCreateSVG(hocrSVGFile);
		return hocrSVGFile;
	}

	public File processGOCR(File processedImageDir) {
		File gocrSVGFile = AMIOCRTool.makeOcrOutputFilename(processedImageDir, basename, AMIOCRTool.GOCR, CTree.SVG);
		SVGElement gocrSvgElement = SVGElement.readAndCreateSVG(gocrSVGFile);
		GOCRConverter converter = new GOCRConverter();
		TextLineAnalyzer textLineAnalyzer = converter.createMaps(gocrSvgElement);
		File gocrTextFile = AMIOCRTool.makeOcrOutputFilename(processedImageDir, basename, AMIOCRTool.GOCR, CTree.TXT);
		textLineAnalyzer.outputText(gocrTextFile);
		
		CharBoxList gocrCharBoxList = converter.createCharBoxList(gocrSvgElement);
		LOG.debug("cb "+gocrCharBoxList);
		converter.getTextLineAnalyzer().makeTable(0);
		
		List<SVGImage> gocrImages = SVGImage.extractSelfAndDescendantImages(gocrSvgElement);
		return gocrSVGFile;
	}
	
	private static File makeOcrTextFilename(File processedImageDir, String basename2, String gocr2) {
		// TODO Auto-generated method stub
		return null;
	}

	public static File makeOcrOutputFilename(File imageDir, String basename, String hocrGocr, String suffix) {
		File hocrGocrDir = makeHocrOcrDir(imageDir, basename, hocrGocr);
		File hocrGocrSVG = new File(hocrGocrDir, hocrGocr + "."+suffix);
		return hocrGocrSVG;
	}

	private static File makeHocrOcrDir(File imageDir, String basename, String hocrGocr) {
		File hocrGocrDir1 =  new File(new File(imageDir, basename), hocrGocr);
		hocrGocrDir1.mkdirs();
		File hocrGocrDir = hocrGocrDir1;
		return hocrGocrDir;
	}
	
	@Override
	public void processImageDir(File imageFile) {
		runOCR(imageFile);
	}


}
