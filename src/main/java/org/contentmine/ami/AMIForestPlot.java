package org.contentmine.ami;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AbstractAMITool;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.eucl.euclid.util.MultisetUtil;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.image.pixel.PixelIsland;
import org.contentmine.image.pixel.PixelIslandList;
import org.contentmine.image.pixel.PixelRingList;
import org.contentmine.image.processing.HilditchThinning;
import org.contentmine.image.processing.Thinning;
import org.contentmine.image.processing.ZhangSuenThinning;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import boofcv.io.image.UtilImageIO;
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

public class AMIForestPlot extends AbstractAMITool {
	private static final Logger LOG = Logger.getLogger(AMIForestPlot.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	
//	public enum ThinningMethod {
//		hilditch(new HilditchThinning()),
//		none((Thinning)null),
//		zhangsuen(new ZhangSuenThinning()),
//		;
//		
//		private Thinning thinning;
//
//		private ThinningMethod(Thinning thinning) {
//			this.thinning = thinning;
//		}
//		
//		public static Thinning getThinning(String name) {
//			for (ThinningMethod thinningMethod : values()) {
//				if (thinningMethod.toString().equals(name)) {
//					return thinningMethod.thinning;
//				}
//			}
//			return null;
//		}
//	}
//	
//    @Option(names = {"--islands"},
//    		arity = "1",
//    		defaultValue = "10",
//            description = "create pixelIslands and tabulate properties of first $maxIslandCount islands sorted by size."
//            		+ "0 means no anaysis.")
//    private Integer maxIslandCount = 10;
//	
//    @Option(names = {"--rings"},
//    		arity = "1",
//    		defaultValue = "-1",
//            description = "create pixelRings and tabulate properties. "
//            		+ "Islands are only analyzed if they have more than minRingCount. "
//            		+ "Default (negative) means analyze none. 0 means all islands. Only '--islands' count are analyzed")
//    
//    private Integer minRingCount = -1;
//	
//    @Option(names = {"--imagefiles"},
//    		arity = "1",
//            description = "binarized file/s to be processed (I think)")
//    private String[] imageFilenames;
//    
//    @Option(names = {"--maxislands"},
//    		arity = "1",
//    		defaultValue = "500",
//            description = "maximum number of pixelIslands. Only use if the original is 'too spotty' and taking far too long. "
//            		+ "The output is truncated.")
//    private int maxislands;
//
//    @Option(names = {"--minwidth"},
//    		arity = "1",    		
//    		defaultValue = "30",
//            description = "minimum width for islands ")
//    private int minwidth = 30;
//    
//    @Option(names = {"--minheight"},
//    		arity = "1",    		
//   	   		defaultValue = "30",
//            description = "minimum height range for islands ")
//    private int minheight = 30;
//    
//    @Option(names = {"--thinning"},
//    		arity = "1",
//    		defaultValue = "none",
//            description = "Apply thinning (${COMPLETION-CANDIDATES}) (none, or absence -> no thinning)")
//    private String thinningName;
//    
//	private File derivedImagesDir;
//	private DiagramAnalyzer diagramAnalyzer;
//
//	private PixelIslandList pixelIslandList;
//
//    /** used by some non-picocli calls
//     * obsolete it
//     * @param cProject
//     */
//	public AMIForestPlot(CProject cProject) {
//		this.cProject = cProject;
//	}
//	
//	public AMIForestPlot() {
//	}
//	
//    public static void main(String[] args) throws Exception {
//    	new AMIForestPlot().runCommands(args);
//    }
//
//    @Override
	protected void parseSpecifics() {
//		System.out.println("maxislands           " + maxislands);
//		System.out.println("imagefiles           " + imageFilenames);
//		System.out.println("minwidth             " + minwidth);
//		System.out.println("minheight            " + minheight);
//		System.out.println("thinning             " + thinningName);
//		System.out.println("thinning             " + thinningName);
//		System.out.println("maxIslandCount       " + maxIslandCount);
//		System.out.println("minRingCount         " + minRingCount);
//		System.out.println();
	}
//
//    @Override
    protected void runSpecifics() {
//    	if (cProject != null) {
//    		for (CTree cTree : cProject.getOrCreateCTreeList()) {
//    			runPixel(cTree);
//    		}
//    	} else if (cTree != null) {
//   			runPixel(cTree);
//    	} else if (imageFilenames != null) {
//    		for (String imageFilename : imageFilenames) {
//    			runPixel(new File(imageFilename));
//    		}
//   		    		
//    	} else {
//			DebugPrint.debugPrint(Level.ERROR, "must give cProject or cTree ie imageFile");
//	    }
    }
//
//	private void runPixel(CTree cTree) {
//		System.out.println("cTree: "+cTree.getName());
//		derivedImagesDir = cTree.getOrCreateDerivedImagesDir();
//		if (derivedImagesDir == null || !derivedImagesDir.exists()) {
//			LOG.warn("no derivedimages/ dir");
//		} else {
//			List<File> derivedImageFiles = new CMineGlobber("**/*.png", derivedImagesDir).listFiles();
//			Collections.sort(derivedImageFiles);
//			for (File derivedImageFile : derivedImageFiles) {
//				System.err.print(".");
//				runPixel(derivedImageFile);
//			}
//		}
//	}
//	
//	private void runPixel(File derivedImageFile) {
//		String basename = FilenameUtils.getBaseName(derivedImageFile.toString());
//		System.err.println("?"+basename+"?");
//		BufferedImage image = UtilImageIO.loadImage(derivedImageFile.toString());
//		diagramAnalyzer = new DiagramAnalyzer().setImage(image);
//		diagramAnalyzer.setMaxIsland(maxislands);
//		Thinning thinning = thinningName == null ? null : ThinningMethod.getThinning(thinningName);
//		diagramAnalyzer.setThinning(thinning);
//		pixelIslandList = diagramAnalyzer.getOrCreateSortedPixelIslandList();
//		System.out.println("pixel islands "+pixelIslandList.size());
//		if (maxIslandCount > 0) {
//			analyzeIslandSizes();
//		}
//		if (minRingCount >= 0) {
//			analyzeRings();
//		}
//		if (minwidth > 0 && minheight > 0) {
//			selectIslands();
//		}
//		
//	}
//
//	private void analyzeRings() {
//		for (int i = 0; i < Math.min(pixelIslandList.size(), maxislands); i++) {
//			PixelIsland island = pixelIslandList.get(i);
//			PixelRingList pixelRingList = island.getOrCreateInternalPixelRings();
//			int size = pixelRingList.size();
//			if (size >= minRingCount) {
//				LOG.debug("rings "+size);
//			}
//		}
//	}
//
//	private void analyzeIslandSizes() {
//		Multiset<Int2> pixelIslandBoxSet = HashMultiset.create();
//		Multiset<Integer> pixelIslandXSet = HashMultiset.create();
//		Multiset<Integer> pixelIslandYSet = HashMultiset.create();
//		for (PixelIsland pixelIsland : pixelIslandList) {
//			Int2 box = pixelIsland.getIntBoundingBox().getLimits();
//			pixelIslandBoxSet.add(box);
//			pixelIslandXSet.add(box.getX());
//			pixelIslandYSet.add(box.getY());
//		}
//		List<Entry<Int2>> boxes = MultisetUtil.createListSortedByCount(pixelIslandBoxSet);
//		System.out.println("boxes "+boxes);
//		List<Entry<Integer>> xx = MultisetUtil.createListSortedByCount(pixelIslandXSet);
//		System.out.println("commonest x "+xx);
//		List<Entry<Integer>> yy = MultisetUtil.createListSortedByCount(pixelIslandYSet);
//		System.out.println("commonest y "+yy);
//		
////		List<Entry<Int2>> boxes1 = MultisetUtil.createListSortedByValue(pixelIslandBoxSet);
////		System.out.println("boxes "+boxes1);
//		List<Entry<Integer>> xx1 = MultisetUtil.createListSortedByValue(pixelIslandXSet);
//		System.out.println("increasing x "+xx1);
//		List<Entry<Integer>> yy1 = MultisetUtil.createListSortedByValue(pixelIslandYSet);
//		System.out.println("increasing y "+yy1);
//	}
//
//	private void selectIslands() {
//		pixelIslandList.removeIslandsWithBBoxesLessThan(new Int2((int)minwidth, (int)minheight));
//	}
//
//	/** from ForestPlotIT
//			BufferedImage image1 = imageProcessor.getBinarizedImage();
//			ImageIOUtil.writeImageQuietly(image1, new File(targetDir, fileRoot+"/raw.png"));
//			PixelIslandList pixelIslandList = imageProcessor.getOrCreatePixelIslandList();
//			List<PixelRingList> pixelRingListList = pixelIslandList.createRingListList();
//	//		Assert.assertEquals("characters", 178, points.size());
//			PlotTest.drawRings(pixelRingListList, new File(targetDir, fileRoot+"/points00.svg"));
//			PixelRingListComparator pixelRingListComparator = new PixelRingListComparator();
//			Collections.sort(pixelRingListList, pixelRingListComparator);
////			pixelRingListList.sort(new PixelRingListComparator());
//			Collections.reverse(pixelRingListList);
//			for (PixelRingList pixelRingList : pixelRingListList) {
//				LOG.trace(pixelRingList.get(0).size());
//			}
//			PixelRingList pixelRingList = pixelRingListList.get(0);
//			SVGG g = null;
//			pixelRingList.plotRings(g, new String[] {"red", "cyan", "purple", "yellow", "blue", "pink", "green"});
//			SVGSVG.wrapAndWriteAsSVG(g, new File(targetDir, fileRoot+"/allRings.svg"));
//			for (int i = 0; i < pixelRingList.size(); i+=5) {
//				PixelRing pixelRing = pixelRingList.get(i);
//				g = null;
//				g = pixelRing.plotPixels(g, "red");
//				SVGSVG.wrapAndWriteAsSVG(g, new File(targetDir, fileRoot+"/allRings"+i+".svg"));
//			}
//			PixelRing pixelRing10 = pixelRingList.get(10);
//			PixelIslandList pl;
//	//		PixelIslandList ringIslandList = PixelIslandList.;
//	 */
}
