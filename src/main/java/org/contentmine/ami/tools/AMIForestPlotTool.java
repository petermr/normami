package org.contentmine.ami.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.eucl.euclid.IntArray;
import org.contentmine.eucl.euclid.IntegerMultiset;
import org.contentmine.eucl.euclid.IntegerMultisetList;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.eucl.euclid.Real2Array;
import org.contentmine.graphics.svg.SVGCircle;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGLineList;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGText;
import org.contentmine.graphics.svg.SVGUtil;
import org.contentmine.image.diagram.DiagramAnalyzer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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

public class AMIForestPlotTool extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMIForestPlotTool.class);

	private static final Double DESCENDER_FUDGE = 0.15;

	private static final int DELTA_Y = 3;

	private static final double EDGE_FRACT = 0.3;
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public enum PlotType {
		spss,
		stata
	}
	
    @Option(names = {"--minnested"},
    		arity = "1",
            description = "minimum level for internal countours to be significant. "
            		+ "Above this level we new expect isolated pixel islands to appear."
            		+ "Arcane and experiemental.")
    private Integer minNestedRings = 2;
	
    @Option(names = {"--plottype"},
    		arity = "1",
            description = "type of SPSS plot")
    private PlotType plotType;

    @Option(names = {"--hocr"},
    		arity = "1",
            description = "use HOCR output from Tesseract",
            defaultValue = "true"
            )
    private boolean useHocr;

    @Option(names = {"--radius"},
    		arity = "1",
            description = "radius for drawing circle round centroid")
    private Double radius = 4.0;

	private Real2Array localSummitCoordinates;
	private DiagramAnalyzer diagramAnalyzer;
	private SVGLineList horizontalLines;
	private String basename;
	
    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIForestPlotTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIForestPlotTool() {
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIForestPlotTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("min nested rings    " + minNestedRings);
		System.out.println("radius of contonurs " + radius);
		System.out.println("plot type           " + plotType);
		System.out.println("use Hocr            " + useHocr);
		System.out.println("scaledFilename      " + basename);
		System.out.println();

		System.out.println();
	}

    @Override
    protected void runSpecifics() {
    	if (processTrees()) { 
//    	} else if (imageFilenames != null) {
//    		for (String imageFilename : imageFilenames) {
//    			runForestPlot(new File(imageFilename));
//    		}
    	} else {
			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree ie imageFile");
	    }
    }

	public void processTree() {
		System.out.println("cTree: "+cTree.getName());
		
		List<File> imageDirs = cTree.getPDFImagesImageDirectories();
		Collections.sort(imageDirs);
		for (File imageDir : imageDirs) {
			File imageFile = getRawImageFile(imageDir);
			this.basename = FilenameUtils.getBaseName(imageDir.toString());
			System.err.print(".");
			if (useHocr) {
				System.out.println(">> "+basename);
				try {
					createTableFromSVG(imageDir);
				} catch (FileNotFoundException fnfe) {
					LOG.debug("SVG File not found: "+imageDir);
				}
			} else {
				runForestPlot(imageFile);
			}
		}
	}

	private void createTableFromSVG(File imageDir) throws FileNotFoundException {
		File hocrDir = new File(imageDir, "hocr");
		File hocrRawDir = new File(hocrDir, "raw");
		File rawSvgFile = new File(hocrRawDir, "raw.svg");
		SVGSVG svg = (SVGSVG) SVGUtil.parseToSVGElement(new FileInputStream(rawSvgFile));
//		LOG.debug("SVG: "+rawSvgFile+" "+rawSvgFile.exists());
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svg);
		createBins(textList);
		
	}

	private List<IntegerMultiset> createBins(List<SVGText> textList) {
		int deltaY = 10; // guess bin separation
		Multimap<Integer, SVGText> textMap = createNonEmptyTextMultimap(textList);
		IntArray yArray = createSortedYCoordinates(textMap);
		IntegerMultisetList yBinList = new IntegerMultisetList();
		List<IntegerMultiset> bins = yBinList.createBins(yArray, deltaY);
		yBinList.mergeNeighbouringBins((int) (deltaY * EDGE_FRACT));
		bins = yBinList.getBins();
//		for (IntegerMultiset bin : bins) {
//			if (bin.size() > 0) {
//				LOG.debug("bin "+bin);
//			}
//		}
		return bins;
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

	public void runForestPlot(File imageFile) {
		diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.setInputFile(imageFile);
		localSummitCoordinates = diagramAnalyzer.extractLocalSummitCoordinates(minNestedRings, 1);
		horizontalLines = diagramAnalyzer.extractHorizontalLines();
		displayPointsAndLines();
	}

	private void displayPointsAndLines() {
		SVGG g = new SVGG();
		if (localSummitCoordinates != null && localSummitCoordinates.size() > 0) {
			for (Real2 xy : localSummitCoordinates) {
				SVGCircle circle = (SVGCircle) new SVGCircle(xy.format(2), radius).setFill("none").setStrokeWidth(0.7).setStroke("blue");
				g.appendChild(circle);
			}
		}
		if (horizontalLines != null) {
			g.appendChild(horizontalLines.createSVGElement());
		}
		File svgFile = new File("target/"+basename+".junk.svg");
		LOG.debug("svg "+svgFile);
		SVGSVG.wrapAndWriteAsSVG(g, svgFile);
	}
}
