package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.eucl.euclid.Axis.Axis2;
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.eucl.euclid.Int2Range;
import org.contentmine.eucl.euclid.IntArray;
import org.contentmine.eucl.euclid.IntRange;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.eucl.euclid.Real2Range;
import org.contentmine.eucl.euclid.RealArray;
import org.contentmine.eucl.euclid.RealRange;
import org.contentmine.eucl.euclid.util.MultisetUtil;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGLine;
import org.contentmine.graphics.svg.SVGLineList;
import org.contentmine.graphics.svg.SVGRect;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.image.ImageUtil;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.image.pixel.AxialPixelFrequencies;
import org.contentmine.image.pixel.IslandRingList;
import org.contentmine.image.pixel.PixelIsland;
import org.contentmine.image.pixel.PixelIslandList;
import org.contentmine.image.pixel.PixelRing;
import org.contentmine.image.pixel.PixelRingList;
import org.contentmine.image.processing.HilditchThinning;
import org.contentmine.image.processing.Thinning;
import org.contentmine.image.processing.ZhangSuenThinning;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import boofcv.io.image.UtilImageIO;
import nu.xom.Attribute;
import nu.xom.Element;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** analyses bitmaps
 * 
 * @author pm286
 *
 */
@Command(
		//String name() default "<main class>";
name = "ami-pixel", 
		//String[] aliases() default {};
aliases = "pixel",
		//Class<?>[] subcommands() default {};
version = "ami-pixel 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "analyzes bitmaps - generally binary, but may be oligochrome. Creates pixelIslands "
)

public class AMIPixelTool extends AbstractAMITool {



	private static final Logger LOG = Logger.getLogger(AMIPixelTool.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public enum ThinningMethod {
		hilditch(new HilditchThinning()),
		none((Thinning)null),
		zhangsuen(new ZhangSuenThinning()),
		;
		
		private Thinning thinning;

		private ThinningMethod(Thinning thinning) {
			this.thinning = thinning;
		}
		
		public static Thinning getThinning(String name) {
			for (ThinningMethod thinningMethod : values()) {
				if (thinningMethod.toString().equals(name)) {
					return thinningMethod.thinning;
				}
			}
			return null;
		}
	}

	/** axis to project pixels onto
	 * 
	 * @author pm286
	 *
	 */
	public enum Projection {
		horizontal,
		vertical
	}
	
    @Option(names = {"--filename"},
    		arity = "1",
            description = "name for transformed Imagefile")
	public String basename = "default";

    @Option(names = {"--islands"},
    		arity = "1",
    		defaultValue = "10",
            description = "create pixelIslands and tabulate properties of first $maxIslandCount islands sorted by size."
            		+ "0 means no anaysis.")
    private Integer maxIslandCount = 10;
	
    @Option(names = {"--lines"},
    		arity = "0",
            description = "extract lines from projections. Result is set of lines in SVGLine format")
    private boolean lines = false;;
	
    @Option(names = {"--maxislands"},
    		arity = "1",
    		defaultValue = "500",
            description = "maximum number of pixelIslands. Only use if the original is 'too spotty' and taking far too long. "
            		+ "The output is truncated.")
    private int maxislands;

    @Option(names = {"--minwidth"},
    		arity = "1",    		
    		defaultValue = "30",
            description = "minimum width for islands ")
    private int minwidth = 30;
    
    @Option(names = {"--minheight"},
    		arity = "1",    		
   	   		defaultValue = "30",
            description = "minimum height range for islands ")
    private int minheight = 30;
    
    @Option(names = {"--outputDirectory"},
    		arity = "1",
//    		defaultValue = "pixels",
            description = "subdirectory for output of pixel analysis and diagrams (if none defaults to <inputBasename>)")
    private String outputDirname = null;
    
    @Option(names = {"--projections"},
    		arity = "0",
            description = "project pixels onto both axes. Results are IntArrays with frequency of"
            		+ "black pixels ")
    private boolean projections = false;
    
    @Option(names = {"--projectionsname"},
    		arity = "1",
    		defaultValue = "projections",
            description = "name for holding extracted projections ")
	public String projectionsName;

    @Option(names = {"--rings"},
    		arity = "1",
    		defaultValue = "-1",
            description = "create pixelRings and tabulate properties. "
            		+ "Islands are only analyzed if they have more than minRingCount. "
            		+ "Default (negative) means analyze none. 0 means all islands. Only '--islands' count are analyzed")
    private Integer minRingCount = -1;
	
    @Option(names = {"--subimage"},
    		arity = "1..*",
            description = "create a subimage and extract projections "
            		+ " '--subimage statascale ycoord 2 10 xprojection' means:"
            		+ "     use statascale protocol 2nd y horizntal line and add 10 pixels and project onto x."
            		+ " Horrible kludge. The first token is the name, the others are more hacky."
            		+ "")
    private List<String> subimageTokens = new ArrayList<String>();
	
    @Option(names = {"--templateinput"},
    		arity = "1",
            description = "generate template from pixel projections; triggers the activity; normally some/where/projections.xml")
    private String templateInput = null;
    
    @Option(names = {"--templateoutput"},
    		arity = "1",
    		defaultValue = "template.xml",
            description = "filename of generated template file")
    private String templateOutput = "template.xml";
    
    @Option(names = {"--templatexsl"},
    		arity = "1",
            description = "stylesheet for generating individual templates. located in "
            		+ "src/main/resources, normally accessed as resource /org/contentmine/ami/tools/foo.xsl")
    private String templateXsl = null;
    
    @Option(names = {"--thinning"},
    		arity = "1",
    		defaultValue = "none",
            description = "Apply thinning (${COMPLETION-CANDIDATES}) (none, or absence -> no thinning)")
    private String thinningName;
    
    @Option(names = {"--xprojection"},
    		arity = "1",
    		defaultValue = "0.5",
            description = "fraction of height for meaningful projection. If greater will contribute to a projection range ")
    private Double xProjectionFactor;
    
    @Option(names = {"--yprojection"},
    		arity = "1",
    		defaultValue = "0.5",
            description = "fraction of width for meaningful projection. If greater will contribute to a projection range ")
    private Double yProjectionFactor;

	private static final String BASENAME = "basename";
	private static final String COORD = "coord";
	private static final String CTREE = "cTree";
	private static final String DELTA = "delta";
	private static final String IMAGE_DIR = "imageDir";
	private static final String LINES = "projections";
	private static final String MAX = "max";
	private static final String MIN = "min";
	private static final String PROJECTION = "projection";
	private static final String SUB_IMAGE = "subImage";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String X_COORD = "xcoord";
	private static final String Y_COORD = "ycoord";
	private static final String X_COORDS = "xcoords";
	private static final String Y_COORDS = "ycoords";
	private static String[] COLORS = new String[] {"red", "green", "blue", "pink", "yellow", "cyan", "magenta", "brown"};

	private DiagramAnalyzer diagramAnalyzer;
	private PixelIslandList pixelIslandList;
	private File outputDirectory;
//	private AxialPixelFrequencies axialPixelFrequencies;
	private File imageDir;
	private File imageFile;
	private List<IntRange> yRangeList;
	private List<IntRange> xRangeList;
	private BufferedImage image;
	private BufferedImage subImage;
	private String initialCoordName;
	private String projectionCoordName;
	private List<List<IntRange>> intRangeListList;

	private File projectionsFile;
	// still being worked out... should be arguments
	private double lineProjectionFactor = 0.1;
	private double minLineLength = 10;

	private List<SVGLineList> verticalLineListList;

	private List<SVGLineList> horizontalLineListList;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIPixelTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIPixelTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIPixelTool().runCommands(args);
    }

    @Override
    protected boolean parseGenerics() {
    	if (inputBasename == null) {
    		inputBasename = RAW;
    	}
    	if (outputDirname == null) {
    		outputDirname = inputBasename;
    	}
    	return super.parseGenerics();
    }
    @Override
	protected void parseSpecifics() {
    	outputDirname = outputDirname.endsWith("/") ? outputDirname : outputDirname + "/";
		System.out.println("basename             " + basename);
		System.out.println("lines                " + lines);
		System.out.println("maxislands           " + maxislands);
		System.out.println("minwidth             " + minwidth);
		System.out.println("minheight            " + minheight);
		System.out.println("maxIslandCount       " + maxIslandCount);
		System.out.println("minRingCount         " + minRingCount);
		System.out.println("outputDirname        " + outputDirname);
		System.out.println("projections          " + projections);
		System.out.println("projectionsName      " + projectionsName);
		System.out.println("subimageTokens       " + subimageTokens);
		System.out.println("templateInput        " + templateInput);
		System.out.println("templateOutput       " + templateOutput);
		System.out.println("templateXsl          " + templateXsl);
		System.out.println("thinning             " + thinningName);
		System.out.println("xProjectionFactor    " + xProjectionFactor);
		System.out.println("yProjectionFactor    " + yProjectionFactor);
		System.out.println();
	}

    @Override
    protected void runSpecifics() {
    	if (processTrees()) { 
    		// this usedto be a list of imageDirs... but never implemented
//    	} else if (imageFilenames != null) {
//    		for (String imageFilename : imageFilenames) {
//    			runPixel(new File(imageFilename));
//    		}
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree ie imageFile");
	    }
    }

	public void processTree() {
		ImageDirProcessor imageDirProcessor = new ImageDirProcessor(this, cTree);
		imageDirProcessor.processImageDirs();
	}

	private List<File> createSortedImageDirectories() {
		List<File> imageDirs = cTree.getPDFImagesImageDirectories();
		Collections.sort(imageDirs);
		return imageDirs;
	}
	
	private void runPixel(File imageFile) {
		imageDir = imageFile.getParentFile();
		System.out.println(">imageDir> "+imageDir.getName());
		outputDirectory = new File(imageDir, outputDirname+"/");
		outputDirectory.mkdirs();
		basename = FilenameUtils.getBaseName(imageFile.toString());
		if (includeExclude(basename)) {
			System.out.println(">basename> "+basename);
		}
		if (!imageFile.exists()) {
			throw new RuntimeException("Image file does not exist: "+imageFile);
		}
		image = UtilImageIO.loadImage(imageFile.toString());
		if (image == null) {
			LOG.error("Null image for: "+imageFile );
			image = ImageUtil.readImage(imageFile);
			if (image == null) {
				LOG.error("STILL Null image for: "+imageFile );
				return;
			}
		}
		diagramAnalyzer = new DiagramAnalyzer().setImage(image);
		diagramAnalyzer.setMaxIsland(maxislands);
		Thinning thinning = thinningName == null ? null : ThinningMethod.getThinning(thinningName);
		diagramAnalyzer.setThinning(thinning);
		pixelIslandList = diagramAnalyzer.getOrCreateSortedPixelIslandList();
//		System.out.println("pixel island sizes "+pixelIslandList.size());
		if (maxIslandCount > 0) {
			analyzeIslandSizes();
		}
		if (minRingCount >= 0) {
			analyzeRings();
		}
		if (minwidth > 0 && minheight > 0) {
			selectIslands();
		}
		if (projections) {
			createProjections();
			if (subimageTokens.size() > 0) {
				subImage = createSubimage();
				if (subImage != null) {
					intRangeListList = extractProjectionRangeLists(subImage, 0.5, 0.7);
				}
			}
			if (projectionsName != null) {
				try {
					writeProjections();
				} catch (Exception e) {
					throw new RuntimeException("Cannot write XML file", e);
				}
			}
		}
		if (templateInput != null) {
			makeTemplate();
		}
		if (minRingCount > 0 || maxIslandCount > 0) {
			this.analyzeAndPlotIslands();
		}

	}

	private void makeTemplate() {
		if (projectionsFile == null || !projectionsFile.exists()) {
			throw new RuntimeException("null or non-existent projections file "+projectionsFile);
		}
		InputStream xmlIs = null;
		try {
			xmlIs = new FileInputStream(projectionsFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		String xslName = templateXsl;
		InputStream xslIs = this.getClass().getResourceAsStream(xslName);
		if (xslIs == null) {
			throw new RuntimeException("XSL file / resource not found or nor read "+ xslName);
		}
		File templateOutputFile = null;
		try {
			String xmlString = transform(xmlIs, xslIs);
			templateOutputFile = new File(projectionsFile.getParentFile(), templateOutput);
			FileUtils.writeStringToFile(templateOutputFile, xmlString, "UTF-8");
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException("cannot write file: "+templateOutputFile);
		}
	}
	
	private String transform(InputStream xmlIs, InputStream xslIs) throws TransformerException {
		TransformerFactory tfactory = TransformerFactory.newInstance();
	    StreamSource source = new StreamSource(xslIs);
	    if (source == null) {
	    	LOG.debug("null transformer source");
	    	return null;
	    }
		Transformer javaxTransformer = tfactory.newTransformer(source);
		OutputStream baos = new ByteArrayOutputStream();
		javaxTransformer.transform(new StreamSource(xmlIs),  new StreamResult(baos));
		return baos.toString();
	}

	private static List<List<IntRange>> extractProjectionRangeLists(BufferedImage image, double xProjectionFactor, double yProjectionFactor) {
		int height = image.getHeight();
		int width = image.getWidth();
		LOG.trace("w/h "+width+"/"+height);
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.setImage(image);
		List<List<IntRange>> intRangeListList = new ArrayList<List<IntRange>>();
		AxialPixelFrequencies axialPixelFrequencies = diagramAnalyzer.getAxialPixelFrequencies();
		
		intRangeListList.add(extractRangeList(xProjectionFactor, height, width, axialPixelFrequencies, Axis2.X));
		intRangeListList.add(extractRangeList(yProjectionFactor, height, width, axialPixelFrequencies, Axis2.Y));
		
		
		return intRangeListList;
	}

	private static List<IntRange> extractRangeList(double projectionFactor, int height, int width,
			AxialPixelFrequencies axialPixelFrequencies, Axis2 axis) {
		RealArray frequencies = new RealArray((Axis2.X.equals(axis) ? axialPixelFrequencies.getXFrequencies() : axialPixelFrequencies.getYFrequencies()));
		int limit1 = Axis2.X.equals(axis) ? width/2 : height/2;
		frequencies.getIndexesWithinRange(new RealRange(limit1, 9999));
		double limit2 = projectionFactor * (Axis2.X.equals(axis) ? height : width); 
		List<IntRange> rangeList = frequencies.createMaskArray(limit2);
		return rangeList;
	}

	/** --subimage statascale y 2 delta 10 projection x
	 *  the ycoord index runs from 1 
	 * @return 
	 * */
	private BufferedImage createSubimage() {
		String subImageName = null;
		initialCoordName = null;
		Integer coord1 = -1;
		Integer coord2 = -1;
		Integer delta = 0;
		projectionCoordName = null;
				
		int itoken = 0;
		while (itoken < subimageTokens.size()) {
			String token = subimageTokens.get(itoken);
			if (itoken == 0) {
				subImageName = token;
			} else if (X.equals(token) || Y.equals(token)) {
				initialCoordName = token;
				int coordIndex = parseInt(subimageTokens.get(++itoken)); 
				List<IntRange> rangeList = X.equals(token) ? xRangeList : yRangeList;
				coord1 = rangeList.size() < coordIndex ? null : rangeList.get(coordIndex - 1).getMax();
				if (coord1 == null) {
					LOG.warn("cannot find yRange: "+coordIndex+" "+yRangeList);
				}
			} else if (DELTA.equals(token)) {
				itoken++;
				if (coord1 == null) {
					LOG.warn("missing coord: "+subimageTokens);
				} else {
					delta = parseInt(subimageTokens.get(itoken));
					coord2 = coord1 + delta;
				}
			} else if (PROJECTION.equals(token)) {
				projectionCoordName = subimageTokens.get(++itoken).toLowerCase(); 
				if (projectionCoordName == null || (!projectionCoordName.equals(X) && !projectionCoordName.equals(Y))) {
					throw new RuntimeException(PROJECTION+" must be "+X+" or "+Y);
				}
			} else {
				throw new RuntimeException("Cannot parse subImage token "+itoken+" "+token+" "+subimageTokens);
			}
			itoken++;
		}
		BufferedImage subImage = null;
		if (coord1 != null && coord2 != null) {
			int xoff = X.equals(initialCoordName) ? coord1 : 0;
			int yoff = X.equals(initialCoordName) ? 0 : coord1;
			int newWidth = X.equals(initialCoordName) ? coord2 - coord1 : image.getWidth();
			int newHeight = X.equals(initialCoordName) ? image.getHeight() : coord2 - coord1;
			subImage = ImageUtil.createClippedImage(image, xoff, yoff, newWidth, newHeight);
			if (false) {
				ImageUtil.writePngQuietly(subImage, new File(imageFile.getParentFile(), basename+".scale"+"."+CTree.PNG));
			}
		}
		return subImage;
	}

	private Integer parseInt(String token) {
		Integer ii = null;
		if (token != null) {
			try {
				ii = Integer.parseInt(token);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException("cann parse integer: "+token);
			}
		}
		return ii;
	}

	private void writeProjections() throws IOException {
		File outputDir = new File(imageDir, basename);
		projectionsFile = new File(outputDir, projectionsName+"."+CTree.XML);
		Element linesElement = new Element(LINES);
		linesElement.addAttribute(new Attribute(CTREE, cTree.getName()));
		linesElement.addAttribute(new Attribute(IMAGE_DIR, imageDir.getName()));
		linesElement.addAttribute(new Attribute(BASENAME, basename));
		
		linesElement.appendChild(createRangeElement(X_COORDS, X_COORD, xRangeList));
		linesElement.appendChild(createRangeElement(Y_COORDS, Y_COORD, yRangeList));
		linesElement.appendChild(createLineListListElement(horizontalLineListList, "horizontal"));
		linesElement.appendChild(createLineListListElement(verticalLineListList, "vertical"));
	
		if (intRangeListList != null) {
			linesElement.appendChild(subImageProjections());
		}
		
		XMLUtil.debug(linesElement, projectionsFile, 1);
	}

	private Element createLineListListElement(List<SVGLineList> lineListList, String direction) {
		Element element = new Element(direction+"lines");
		for (SVGLineList lineList : lineListList) {
			element.appendChild(lineList.createSVGElement());
		}
		return element;
	}

	private Element subImageProjections() {
		Element subImageElement = new Element(SUB_IMAGE);
		addRangeList(subImageElement, intRangeListList.get(0), X);
		addRangeList(subImageElement, intRangeListList.get(1), Y);
		return subImageElement;
		
	}

	private void addRangeList(Element subImageElement, List<IntRange> xRangeList, String name) {
		for (IntRange range : xRangeList) {
			subImageElement.appendChild(createRangeElement(name, range));
		}
	}

	private Element createRangeElement(String coordsName, String coordName, List<IntRange> rangeList) {
		Element linesElement = new Element(coordsName);
		addCoordRanges(linesElement, coordName, rangeList);
		return linesElement;
	}

	private void addCoordRanges(Element lineElement, String name, List<IntRange> rangeList) {
		for (IntRange range : rangeList) {
			Element rangeElement = createRangeElement(name, range);
			lineElement.appendChild(rangeElement);
		}
	}

	private Element createRangeElement(String name, IntRange range) {
		Element rangeElement = new Element(name);
		rangeElement.addAttribute(new Attribute(MIN, String.valueOf(range.getMin())));
		rangeElement.addAttribute(new Attribute(MAX, String.valueOf(range.getMax())));
		return rangeElement;
	}

	private void createProjections() {
		diagramAnalyzer.createAxialPixelFrequencies();
		BufferedImage image = diagramAnalyzer.getImage();
		if (image == null) {
			LOG.error("null image");
			return;
		}
		
		int height = image.getHeight();
		int width = image.getWidth();
		AxialPixelFrequencies axialPixelFrequencies = diagramAnalyzer.getAxialPixelFrequencies();
		
		verticalLineListList = createRangesAndLineLists(height, width, axialPixelFrequencies, Axis2.X);		
		horizontalLineListList = createRangesAndLineLists(height, width, axialPixelFrequencies, Axis2.Y);
		
		return;
	}

	private List<SVGLineList> createRangesAndLineLists(int height, int width, AxialPixelFrequencies axialPixelFrequencies, Axis2 axis) {
		IntArray intFrequencies = (Axis2.X.equals(axis)) ? 
				axialPixelFrequencies.getXFrequencies() : axialPixelFrequencies.getYFrequencies();
		RealArray frequencies = new RealArray(intFrequencies);
		int minv = (Axis2.X.equals(axis)) ? width/2 : height/2;
		frequencies.getIndexesWithinRange(new RealRange(minv, 9999));
		double limit = (Axis2.X.equals(axis)) ? (double)height * xProjectionFactor : (double)width * yProjectionFactor;
		List<IntRange> maskArray = frequencies.createMaskArray(limit);
		// save ranges
		if (Axis2.X.equals(axis)) {
			xRangeList = maskArray;
		} else {
			yRangeList = maskArray;
		}
		return (lines) ? extractLineLists(maskArray, axis) : new ArrayList<>();
	}

	private List<SVGLineList> extractLineLists(List<IntRange> rangeList, Axis2 axis) {
		List<SVGLineList> lineListList = new ArrayList<>();
		for (IntRange range : rangeList) {
			SVGLineList lineList = extractLineList(range, axis);
			lineListList.add(lineList);
		}
		return lineListList;
	}

	private SVGLineList extractLineList(IntRange projectionRange, Axis2 axis) {
		int lineBase = projectionRange.getMin();
		int lineWidth = projectionRange.getRange();
		Int2Range boundingBox = Axis2.X.equals(axis) ? new Int2Range(projectionRange, new IntRange(0, image.getHeight())) : 
			new Int2Range(new IntRange(0, image.getWidth()), projectionRange);
		BufferedImage subImage = ImageUtil.clipSubImage(image, boundingBox);
		SVGLineList lineList = new SVGLineList();
		if (subImage == null) {
			LOG.debug("Cannot clip: "+boundingBox);
		} else {
			List<List<IntRange>> rangeListList = extractProjectionRangeLists(subImage, lineProjectionFactor , lineProjectionFactor);
			for (List<IntRange> rangeList : rangeListList) {
				for (IntRange rng : rangeList) {
					Real2 x1 = Axis2.Y.equals(axis) ? new Real2(rng.getMin(), lineBase) : new Real2(lineBase, rng.getMin()); 
					Real2 x2 = Axis2.Y.equals(axis) ? new Real2(rng.getMax(), lineBase) : new Real2(lineBase, rng.getMax()); 
					SVGLine line = new SVGLine(x1, x2);
					if (line.getLength() > minLineLength) {
						line.setWidth(lineWidth);
						lineList.add(line);
					}
				}
			}
			LOG.trace(">"+lineList.size()+">>"+lineList.createSVGElement().toXML());
		}
		return lineList;
	}

	private void analyzeRings() {
		for (int i = 0; i < Math.min(pixelIslandList.size(), maxislands); i++) {
			PixelIsland island = pixelIslandList.get(i);
			PixelRingList pixelRingList = island.getOrCreateInternalPixelRings();
			int size = pixelRingList.size();
//			if (size >= minRingCount) {
//				LOG.debug("rings "+size);
//			}
		}
	}

	private void analyzeIslandSizes() {
		Multiset<Int2> pixelIslandBoxSet = HashMultiset.create();
		Multiset<Integer> pixelIslandXSet = HashMultiset.create();
		Multiset<Integer> pixelIslandYSet = HashMultiset.create();
		for (PixelIsland pixelIsland : pixelIslandList) {
			Int2 box = pixelIsland.getIntBoundingBox().getLimits();
			pixelIslandBoxSet.add(box);
			pixelIslandXSet.add(box.getX());
			pixelIslandYSet.add(box.getY());
		}
		List<Entry<Int2>> boxes = MultisetUtil.createListSortedByCount(pixelIslandBoxSet);
		System.out.println("boxes "+boxes);
		List<Entry<Integer>> xx = MultisetUtil.createListSortedByCount(pixelIslandXSet);
		System.out.println("commonest x "+xx);
		List<Entry<Integer>> yy = MultisetUtil.createListSortedByCount(pixelIslandYSet);
		System.out.println("commonest y "+yy);
		
//		List<Entry<Int2>> boxes1 = MultisetUtil.createListSortedByValue(pixelIslandBoxSet);
//		System.out.println("boxes "+boxes1);
		List<Entry<Integer>> xx1 = MultisetUtil.createListSortedByValue(pixelIslandXSet);
		System.out.println("increasing x "+xx1);
		List<Entry<Integer>> yy1 = MultisetUtil.createListSortedByValue(pixelIslandYSet);
		System.out.println("increasing y "+yy1);
	}

	private void selectIslands() {
		pixelIslandList.removeIslandsWithBBoxesLessThan(new Int2((int)minwidth, (int)minheight));
	}

	public PixelIslandList getPixelIslandList() {
		return pixelIslandList;
	}

	public void setPixelIslandList(PixelIslandList pixelIslandList) {
		this.pixelIslandList = pixelIslandList;
	}

	public void analyzeAndPlotIslands() {
		pixelIslandList = getPixelIslandList();
		// all the islands, includes the text (some are only 1 pixel)
		// the largest pixel island (most of the plot, with horizontal, vertical lines squares and rhombus)
		// analyze first island
		SVGG g = new SVGG();
		for (int islandx = 0; islandx < 20; islandx++) {
			System.out.print("I");
			SVGG gg = this.findBoxesAndPlotSubIslands(islandx);
			g.appendChild(gg);
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File(outputDirectory, basename+"." + CTree.SVG));
	}
	
	public SVGG findBoxesAndPlotSubIslands(int island) {
		SVGG g = new SVGG();
		PixelIsland pixelIsland = pixelIslandList.get(island);
		if (pixelIsland != null) {
			System.out.print(" "+pixelIsland.size()+" ");
			List<IslandRingList> islandRingListListx = pixelIsland.getOrCreateIslandRingListList();
			SVGRect box = SVGRect.createFromReal2Range(Real2Range.createReal2Range(pixelIsland.getIntBoundingBox()));
			String boxColor = COLORS[island % COLORS.length];
			box.setStroke(boxColor);
			box.setFill("none");
			g.appendChild(box);
			SVGG gg = pixelIsland.createSVG();
			String fill = COLORS[island % COLORS.length];
			gg.setFill(fill);
			g.appendChild(gg);
			int lvl = pixelIsland.getLevelForMaximumRingCount();
			// FIXME
			lvl = Math.min(islandRingListListx.size() - 1, lvl + 2); // kludge for single subisland islands
			
			plotBoxForLevel(islandRingListListx, boxColor, gg, lvl);
		}
		return g;
	}

	private void plotBoxForLevel(List<IslandRingList> islandRingListListx, String boxColor, SVGG gg, int lvl) {
		IslandRingList ringListx = islandRingListListx.get(lvl);
		for (PixelRing pixelRingx : ringListx) {
			SVGRect box1 = SVGRect.createFromReal2Range(Real2Range.createReal2Range(pixelRingx.getIntBoundingBox()));
			box1.setStroke("black");
			box1.setFill(boxColor);
			box1.setOpacity(0.3);
			gg.appendChild(box1);
		}
	}



	/** from ForestPlotIT
			BufferedImage image1 = imageProcessor.getBinarizedImage();
			ImageIOUtil.writeImageQuietly(image1, new File(targetDir, fileRoot+"/raw.png"));
			PixelIslandList pixelIslandList = imageProcessor.getOrCreatePixelIslandList();
			List<PixelRingList> pixelRingListList = pixelIslandList.createRingListList();
	//		Assert.assertEquals("characters", 178, points.size());
			PlotTest.drawRings(pixelRingListList, new File(targetDir, fileRoot+"/points00.svg"));
			PixelRingListComparator pixelRingListComparator = new PixelRingListComparator();
			Collections.sort(pixelRingListList, pixelRingListComparator);
//			pixelRingListList.sort(new PixelRingListComparator());
			Collections.reverse(pixelRingListList);
			for (PixelRingList pixelRingList : pixelRingListList) {
				LOG.trace(pixelRingList.get(0).size());
			}
			PixelRingList pixelRingList = pixelRingListList.get(0);
			SVGG g = null;
			pixelRingList.plotRings(g, new String[] {"red", "cyan", "purple", "yellow", "blue", "pink", "green"});
			SVGSVG.wrapAndWriteAsSVG(g, new File(targetDir, fileRoot+"/allRings.svg"));
			for (int i = 0; i < pixelRingList.size(); i+=5) {
				PixelRing pixelRing = pixelRingList.get(i);
				g = null;
				g = pixelRing.plotPixels(g, "red");
				SVGSVG.wrapAndWriteAsSVG(g, new File(targetDir, fileRoot+"/allRings"+i+".svg"));
			}
			PixelRing pixelRing10 = pixelRingList.get(10);
			PixelIslandList pl;
	//		PixelIslandList ringIslandList = PixelIslandList.;
	 */
	
	@Override
	public void processImageDir(File imageFile) {
		this.imageFile = imageFile;
		runPixel(imageFile);
	}

}
