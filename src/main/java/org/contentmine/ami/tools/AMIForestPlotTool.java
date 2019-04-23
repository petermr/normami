package org.contentmine.ami.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.eucl.euclid.Real2Array;
import org.contentmine.eucl.euclid.util.MultisetUtil;
import org.contentmine.graphics.svg.SVGCircle;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGLineList;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGUtil;
import org.contentmine.graphics.svg.text.SVGTextLine;
import org.contentmine.graphics.svg.text.SVGTextLineList;
import org.contentmine.image.diagram.DiagramAnalyzer;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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

	private Map<String, String> lineTypeByAbbrev;

	private Multiset<String> abbrevSet;

    /** used by some non-picocli calls
     * obsolete it
     * @param cProject
     */
	public AMIForestPlotTool(CProject cProject) {
		this.cProject = cProject;
	}
	
	public AMIForestPlotTool() {
		init();
	}
	
	public void init() {
		lineTypeByAbbrev = new HashMap<>();
		
		lineTypeByAbbrev.put("AIIIIIII%FBFPFC", "1");
		lineTypeByAbbrev.put("AIIIII%FBFPFCI", "2");
		lineTypeByAbbrev.put("AAII", "3");
		lineTypeByAbbrev.put("AB%ACII%FBFPFC", "4");
		lineTypeByAbbrev.put("SIIII%FBFPFC", "5");
		lineTypeByAbbrev.put("AAASAEFBAEFC", "6");
		lineTypeByAbbrev.put("AB%ACII%FBFPFC", "7");
		lineTypeByAbbrev.put("AIIIII%FBFPFC", "8");
		lineTypeByAbbrev.put("AABICIIII%FBFPFC", "9");
		lineTypeByAbbrev.put("AB%ACII%FBFPFC", "10");
		lineTypeByAbbrev.put("ASSEFPAEIBAEFCPSE%", "11");
		lineTypeByAbbrev.put("B%ACII%FBFPFCA", "12");
		lineTypeByAbbrev.put("SABICIIII%FBFPFC", "13");
		
		abbrevSet = HashMultiset.create();
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
			this.basename = FilenameUtils.getBaseName(imageDir.toString());
			System.out.println("======>"+basename);
//			System.err.print(".");
			if (useHocr) {
				File textLineListFile = ImageDirProcessor.getTextLineListFilename(imageDir);
				createForestPlotFromImageText(textLineListFile);
			} else {
				File imageFile = getRawImageFile(imageDir);
				createForestPlotFromImage(imageFile);
			}
		}
		LOG.debug(MultisetUtil.createListSortedByCount(abbrevSet));
	}

	private void createForestPlotFromImageText(File textLineListFile) {
		SVGElement svgElement = null;
		try {
			svgElement = SVGUtil.parseToSVGElement(new FileInputStream(textLineListFile));
		} catch (FileNotFoundException fnfe) {
			throw new RuntimeException("Cannot find file: "+textLineListFile, fnfe);
		}
		SVGTextLineList textLineList = SVGTextLineList.createSVGTextLineList(svgElement);
		textLineList.splitAtCharacters("[]{}(),<>");
//		LOG.debug("tll"+textLineList);
		for (SVGTextLine textLine : textLineList) {
			String abb = textLine.getOrCreateTypeAnnotatedString();
			System.out.println("tl: "+abb+";"+textLine);
		}
		List<String> textLineAbbs = textLineList.getOrCreateTypeAnnotations();
//		System.out.println("typeLines");
		for (String tl : textLineAbbs) {
//			System.out.print(tl);
			String lineType = lineTypeByAbbrev.get(tl);
			if (lineType != null) {
//				System.out.print(" "+lineType);
				
			} else {
				abbrevSet.add(tl);
			}
//			System.out.println();
		}
		return;
	}


	public void createForestPlotFromImage(File imageFile) {
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
