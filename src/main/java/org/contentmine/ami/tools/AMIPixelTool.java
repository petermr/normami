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
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.eucl.euclid.IntArray;
import org.contentmine.eucl.euclid.IntRange;
import org.contentmine.eucl.euclid.Real2Range;
import org.contentmine.eucl.euclid.RealArray;
import org.contentmine.eucl.euclid.RealRange;
import org.contentmine.eucl.euclid.util.MultisetUtil;
import org.contentmine.graphics.svg.SVGG;
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

    @Option(names = {"--projections"},
    		arity = "0",
            description = "project pixels onto both axes. Results are IntArrays with frequency of"
            		+ "black pixels ")
    private boolean projections = false;
    
//    @Option(names = {"--imagefiles"},
//    		arity = "1",
//            description = "binarized file/s to be processed (I think)")
//    private String[] imageFilenames;
    
    @Option(names = {"--islands"},
    		arity = "1",
    		defaultValue = "10",
            description = "create pixelIslands and tabulate properties of first $maxIslandCount islands sorted by size."
            		+ "0 means no anaysis.")
    private Integer maxIslandCount = 10;
	
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
    
    @Option(names = {"--rings"},
    		arity = "1",
    		defaultValue = "-1",
            description = "create pixelRings and tabulate properties. "
            		+ "Islands are only analyzed if they have more than minRingCount. "
            		+ "Default (negative) means analyze none. 0 means all islands. Only '--islands' count are analyzed")
    
    private Integer minRingCount = -1;
	
    @Option(names = {"--thinning"},
    		arity = "1",
    		defaultValue = "none",
            description = "Apply thinning (${COMPLETION-CANDIDATES}) (none, or absence -> no thinning)")
    private String thinningName;
    
    @Option(names = {"--outputDirectory"},
    		arity = "1",
    		defaultValue = "pixels",
            description = "subdirectory for output of pixel analysis and diagrams")
    private String outputDirname;
    
	private DiagramAnalyzer diagramAnalyzer;
	private PixelIslandList pixelIslandList;
	private File outputDirectory;

	private static String[] COLORS = new String[] {"red", "green", "blue", "pink", "yellow", "cyan", "magenta", "brown"};

	private AxialPixelFrequencies axialPixelFrequencies;


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
    	return super.parseGenerics();
    }
    @Override
	protected void parseSpecifics() {
    	outputDirname = outputDirname.endsWith("/") ? outputDirname : outputDirname + "/";
		System.out.println("maxislands           " + maxislands);
		System.out.println("minwidth             " + minwidth);
		System.out.println("minheight            " + minheight);
		System.out.println("projections          " + projections);
		System.out.println("thinning             " + thinningName);
		System.out.println("thinning             " + thinningName);
		System.out.println("maxIslandCount       " + maxIslandCount);
		System.out.println("minRingCount         " + minRingCount);
		System.out.println("outputDirname        " + outputDirname);
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
		File imageDir = imageFile.getParentFile();
		outputDirectory = new File(imageDir, outputDirname+"/");
		outputDirectory.mkdirs();
		basename = FilenameUtils.getBaseName(imageFile.toString());
		if (includeExclude(basename)) {
			LOG.debug("basename: "+basename);
		}
		if (!imageFile.exists()) {
			throw new RuntimeException("Image file does not exist: "+imageFile);
		}
		BufferedImage image = UtilImageIO.loadImage(imageFile.toString());
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
		System.out.println("pixel island sizes "+pixelIslandList.size());
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
		}
		if (minRingCount > 0 || maxIslandCount > 0) {
			this.analyzeAndPlotIslands();
		}

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
		axialPixelFrequencies = diagramAnalyzer.getAxialPixelFrequencies();
		RealArray xFrequencies = new RealArray(axialPixelFrequencies.getXFrequencies());
//		LOG.debug("x axial frequencies "+xFrequencies);
		IntArray xindexes = xFrequencies.getIndexesWithinRange(new RealRange(width/2, 9999));
		if (xindexes.size() > 0) {
//			LOG.debug("xindexes "+xindexes);
		}
		List<IntRange> xRangeList = xFrequencies.createMaskArray((double)height * 0.5);
		LOG.debug("x mask: possible vertical "+xRangeList);
		
		RealArray yFrequencies = new RealArray(axialPixelFrequencies.getYFrequencies());
//		LOG.debug("y axial frequencies "+yFrequencies);
		IntArray yindexes = yFrequencies.getIndexesWithinRange(new RealRange(height/2, 9999));
		if (yindexes.size() > 0) {
//			LOG.debug("yindexes "+yindexes);
		}
		List<IntRange> yRangeList = yFrequencies.createMaskArray((double)width * 0.5);
		LOG.debug("y mask: possible horizontal "+yRangeList);
		return;
	}

	private void analyzeRings() {
		for (int i = 0; i < Math.min(pixelIslandList.size(), maxislands); i++) {
			PixelIsland island = pixelIslandList.get(i);
			PixelRingList pixelRingList = island.getOrCreateInternalPixelRings();
			int size = pixelRingList.size();
			if (size >= minRingCount) {
				LOG.debug("rings "+size);
			}
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
		runPixel(imageFile);
	}

}
