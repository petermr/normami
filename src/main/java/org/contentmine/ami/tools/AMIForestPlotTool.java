package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.eucl.euclid.Real2Array;
import org.contentmine.eucl.euclid.Real2Range;
import org.contentmine.eucl.euclid.util.MultisetUtil;
import org.contentmine.graphics.svg.SVGCircle;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGLine;
import org.contentmine.graphics.svg.SVGLineList;
import org.contentmine.graphics.svg.SVGRect;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.image.ImageProcessor;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.image.pixel.IslandRingList;
import org.contentmine.image.pixel.PixelIsland;
import org.contentmine.image.pixel.PixelRing;
import org.contentmine.image.processing.HilditchThinning;
import org.contentmine.image.processing.Thinning;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import boofcv.io.image.UtilImageIO;
import nu.xom.Attribute;
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
	
    @Option(names = {"--minnested"},
    		arity = "1",
            description = "minimum level for internal countours to be significant. "
            		+ "Above this level we new expect isolated pixel islands to appear."
            		+ "Arcane and experiemental.")
    private Integer minNestedRings = 2;
	
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
//		System.out.println("imagefiles           " + imageFilenames);
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
			runForestPlot(imageFile);
		}
	}

	public void runForestPlot(File imageFile) {
		diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.setInputFile(imageFile);
		localSummitCoordinates = diagramAnalyzer.extractLocalSummitCoordinates(minNestedRings);
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
