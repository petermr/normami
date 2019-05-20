package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.template.AbstractTemplateElement;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.eucl.euclid.Axis.Axis2;
import org.contentmine.eucl.euclid.Int2Range;
import org.contentmine.eucl.euclid.IntRange;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.eucl.euclid.Real2Array;
import org.contentmine.eucl.euclid.util.MultisetUtil;
import org.contentmine.graphics.html.HtmlElement;
import org.contentmine.graphics.html.util.HtmlUtil;
import org.contentmine.graphics.svg.SVGCircle;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGLineList;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGUtil;
import org.contentmine.graphics.svg.text.SVGPhrase;
import org.contentmine.graphics.svg.text.SVGTextLine;
import org.contentmine.graphics.svg.text.SVGTextLineList;
import org.contentmine.image.ImageLineAnalyzer;
import org.contentmine.image.ImageUtil;
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
name = "ami-forest", 
		//String[] aliases() default {};
aliases = "forest",
		//Class<?>[] subcommands() default {};
version = "ami-forext 0.1",
		//Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;
description = "analyzes ForestPlot images; uses template.xml to steer the operations "
)

public class AMIForestPlotTool extends AbstractAMITool {

	private static final Logger LOG = Logger.getLogger(AMIForestPlotTool.class);
	private static final String TEMPLATE_XML = "template.xml";

	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private enum PlotType {
		spss,
		stata
	}
	
	private enum Axis {
		x,
		y
	}
	

    @Option(names = {"--color"},
    		arity = "1",
            description = "colors of lines on plot as hex string")
    private Integer color = 0x0;

    @Option(names = {"--hocr"},
    		arity = "1",
            description = "use HOCR output from Tesseract",
            defaultValue = "true"
            )
    private boolean useHocr;

    @Option(names = {"--minline"},
    		arity = "1",
            description = "minimum line length")
    private Integer minline = 300;

    @Option(names = {"--minnested"},
    		arity = "1",
            description = "minimum level for internal countours to be significant. "
            		+ "Above this level we new expect isolated pixel islands to appear."
            		+ "Arcane and experiemental.")
    private Integer minNestedRings = 2;
	
    @Option(names = {"--offset"},
    		arity = "1..*",
            description = "offsets from split position/s")
    private List<Integer> offsets;

    @Option(names = {"--plottype"},
    		arity = "1",
            description = "type of SPSS plot")
    private PlotType plotType;

    @Option(names = {"--radius"},
    		arity = "1",
            description = "radius for drawing circle round centroid")
    private Double radius = 4.0;

//    @Option(names = {"--split"},
//    		arity = "1",
//            description = "split images along axis (x or y)")
//    private Axis splitAxis = null;

    @Option(names = {"--table"},
    		arity = "0..1",
            description = "use bounding boxes to create a table")
    private boolean table;

    @Option(names = {"--template"},
    		arity = "0..1",
            description = "template to give imagedir-specific operations (adaptively rewritable),"
            		+ "defaults to 'template.xml'")
    private String templateFilename = TEMPLATE_XML;

	private Real2Array localSummitCoordinates;
	private DiagramAnalyzer diagramAnalyzer;
	private SVGLineList horizontalLines;
	private String basename;

	private Map<String, String> lineTypeByAbbrev;

	private Multiset<String> abbrevSet;
	private List<List<String>> phraseListList;

	private HtmlElement hocrElement;
	private AbstractTemplateElement templateElement;

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
		createLineTypeByAbbrev();
		createPhraseSet();
		abbrevSet = HashMultiset.create();
	}

	private void createPhraseSet() {
		phraseListList = new ArrayList<>();
		
		List<String> phrases;
		phrases = Arrays.asList(new String[] {
				"Study or Subgroup", 
				"Mean", "SD", "Total",
				"Mean", "SD", "Total",
				"Weight"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"Study or Subgroup", 
				"Events", "Total",
				"Events", "Total",
				"Weight"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"Study",
				"or",
				"Subgroup", 
				"log",
				"\\[",
				"Odds",
				"Ratio",
				"\\]",
				"SE",
				"Weight"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"Total",
				"\\(",
				"95%",
				"C(I|l)",
				"\\)",
				"%I",
				"%I"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"Total",
				"events",
				"%I",
				"%I"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"Heterogeneity:", 
				"Tau.?",
				"=",
				"%F",
				";",
				"Chi.?",
				"=",
				"%F",
				",",
				"df",
				"=",
				"I",
				"\\(",
				"P",
				"=", 
				"%F",
				"\\)",
				";",
				"I.?",
				"=",
				"%F"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"Heterogeneity:", 
				"Chi.?",
				"=",
				"%F",
				",",
				"df",
				"=",
				"%I",
				"\\(",
				"P",
				"=",
				"%F",
				"\\)",
				";",
				"I.?",
				"=",
				"%%"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"Test",
				"for",
				"overall",
				"effect:", 
				"Z",
				"=",
				"%F",
				"\\(",
				"P",
				"<",
				"%F",
				"\\)",
				"Favours",
				"%A",
				
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"[A-a].*",
				"%D",
				"%I",
				"%I",
				"%I",
				"%I",
				"%%", 
				"%F", 
				"[\\[\\(]",
				"%F",
				"\\,",
				"%F",
				"[\\]\\)]",
				"%D"
				});
		phraseListList.add(phrases);
		phrases = Arrays.asList(new String[] {
				"\\-?\\d+",
				"\\-?\\d+",
				"\\-?\\d+",
				"\\-?\\d+",
				"\\-?\\d+"
				});
		phraseListList.add(phrases);
	}

	private void createLineTypeByAbbrev() {
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
	}
	
    public static void main(String[] args) throws Exception {
    	new AMIForestPlotTool().runCommands(args);
    }

    @Override
	protected void parseSpecifics() {
		System.out.println("color of lines      " + color);
		System.out.println("min line length     " + minline);
		System.out.println("min nested rings    " + minNestedRings);
		System.out.println("radius of contours  " + radius);
		System.out.println("plot type           " + plotType);
		System.out.println("use Hocr            " + useHocr);
		System.out.println("offsets             " + offsets);
		System.out.println("scaledFilename      " + basename);
//		System.out.println("split axis          " + splitAxis);
		System.out.println("table               " + table);
		System.out.println("template            " + templateFilename);
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
			templateElement = AbstractTemplateElement.readTemplateElement(imageDir, templateFilename);
			if (templateElement != null) {
				templateElement.process();
				continue;
			}

			// probably obsolete
			/**
			if (splitAxis != null){
				BufferedImage image = ImageUtil.readImageQuietly(new File(imageDir, "raw_s4_thr_150.png"));
				Axis2 axis2 = (Axis.x.equals(splitAxis)) ? Axis2.X : Axis2.Y; 
				ImageLineAnalyzer lineAnalyzer = new ImageLineAnalyzer(image);
				List<BufferedImage> imageList = lineAnalyzer.splitAtLeftOfBottomLine(
						color, minline, offsets.get(0), axis2);
				ImageUtil.writeImageQuietly(imageList.get(0), new File(imageDir, "raw_s4_thr_150.table.png"));
				ImageUtil.writeImageQuietly(imageList.get(1), new File(imageDir, "raw_s4_thr_150.plot.png"));
				continue;
			}
			*/
			if (table) {
				File hocrFile = new File(imageDir, "hocr/raw.raw.html");
				if (!hocrFile.exists()) {
					hocrFile = new File(imageDir, "hocr/raw/raw.hocr.html");
				}
				if (!hocrFile.exists()) {
					LOG.debug("cannot find HOCR in "+imageDir);
					continue;
				} 
				try {
					
					hocrElement = HtmlUtil.parseQuietlyToHtmlElementWithoutDTD(hocrFile);
					SPSSForestPlot spssForestPlot = new SPSSForestPlot();
					// clip
					spssForestPlot.setBoundingBox(new Int2Range(new IntRange(0,750), new IntRange(0,1000)));
					spssForestPlot.readHOCR(hocrElement);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.debug("Cannot read HOCR: "+hocrFile+"; "+e);
				}
				
				continue;
			}

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
			if (!textLineListFile.exists()) {
				LOG.error("Cannot find: "+textLineListFile+"\n"
						+ "CHECK that 'hocr' subdirectory exists; you must have run 'ami-ocr' to generate this");
				return;
			}
			svgElement = SVGUtil.parseToSVGElement(new FileInputStream(textLineListFile));
		} catch (FileNotFoundException fnfe) {
			throw new RuntimeException("Cannot find file: "+textLineListFile, fnfe);
		}
		SVGTextLineList textLineList = SVGTextLineList.createSVGTextLineList(svgElement);
		textLineList.splitAtCharacters("[]{}(),<>");
		for (SVGTextLine textLine : textLineList) {
			String abb = textLine.getOrCreateTypeAnnotatedString();
			System.out.println("tl: "+abb+";"+textLine);
			List<SVGPhrase> phraseList = textLine.createPhraseList();
			
			textLine.annotateWith(phraseListList);
		}
		List<String> textLineAbbs = textLineList.getOrCreateTypeAnnotations();
//		System.out.println("typeLines");
		for (String tl : textLineAbbs) {
			System.out.print(tl);
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
