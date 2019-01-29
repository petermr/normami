package org.contentmine.ami;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIPDFTool;
import org.contentmine.ami.tools.AbstractAMITool;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.CTreeList;
import org.contentmine.cproject.files.TreeImageManager;
import org.contentmine.cproject.files.TreeImageManager.TreeImageType;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.eucl.euclid.Axis.Axis2;
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.eucl.euclid.IntArray;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.graphics.svg.SVGCircle;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGG;
import org.contentmine.graphics.svg.SVGLine;
import org.contentmine.graphics.svg.SVGLineList;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.cache.ComponentCache;
import org.contentmine.graphics.svg.cache.LineCache;
import org.contentmine.graphics.svg.linestuff.LineMerger.MergeMethod;
import org.contentmine.graphics.svg.util.ImageIOUtil;
import org.contentmine.image.ImageProcessor;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.image.pixel.IslandRingList;
import org.contentmine.image.pixel.PixelGraphList;
import org.contentmine.image.pixel.PixelIslandList;
import org.contentmine.image.pixel.PixelRing;
import org.contentmine.image.pixel.PixelRingList;
import org.contentmine.image.processing.ZhangSuenThinning;
import org.contentmine.norma.image.ocr.ImageToHOCRConverter;
import org.junit.Assert;
import org.junit.Test;

import nu.xom.Attribute;

/** test AMIProcessorPDF
 * 
 * @author pm286
 *
 */
public class AMIImageProcessorIT {
	private static final File UCLFOREST_DIR = new File("/Users/pm286/workspace/uclforest/");
	private static final String TARGET_HOCR = "target/hocr";
	public static final File FORESTPLOT_DIR = new File(UCLFOREST_DIR, "forestplots/");
	public static final File FORESTPLOT_CONVERTED_DIR = new File(UCLFOREST_DIR, "forestplotsConverted/");
	public static final File FORESTPLOT_IMAGES_DIR = new File(UCLFOREST_DIR, "forestplotsImages/");
	private static final String TARGET_UCLFOREST = "target/uclforest/";
	public static final Logger  LOG = Logger.getLogger(AMIImageProcessorIT.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	
//	@Test
//	/** reads images in UCL corpus and excludes small/narroe
//	 * 
//	 */
//	public void testMinWidthHeight() {
//		File targetDir = new File(TARGET_UCLFOREST);
//		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_CONVERTED_DIR, targetDir);
//		CProject cProject = new CProject(targetDir);
//		AMIProcessorPDF amiProcessorPDF = new AMIProcessorPDF(cProject);
//		amiProcessorPDF.runPDF();
//		// restrict to single tree
//		CTree cTree = cProject.getCTreeByName("case");
//		File pdfImagesDir = cTree.getExistingPDFImagesDir();
//		Assert.assertTrue(pdfImagesDir.exists());
//		File smallDir = new File(pdfImagesDir, AMIImageProcessor.SMALL);
//		Assert.assertFalse(smallDir.exists());
//		File monochromeDir = new File(pdfImagesDir, AMIImageProcessor.MONOCHROME);
//		Assert.assertFalse(monochromeDir.exists());
//		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cTree)
//				.setMinHeight(100).setMinWidth(100).setDiscardDuplicates(true).setDiscardMonochrome(true);
//		amiImageProcessor.runImages(cTree);
//		Assert.assertTrue(""+smallDir + "should exists", smallDir.exists());
//		Assert.assertEquals(59,  smallDir.listFiles().length);
//		// run on whole lot
//		LOG.debug("all");
//		amiImageProcessor = AMIImageProcessor.createAIProcessor(cTree)
//				.setMinHeight(100).setMinWidth(100).setDiscardDuplicates(true).setDiscardMonochrome(true);
//		amiImageProcessor.runImages(cProject);
//	}

//	@Test
//	/** reads images in UCL corpus and excludes monochrome images
//	 * 
//	 */
//	public void testMonochrome() throws Exception {
//		File targetDir = new File(TARGET_UCLFOREST);
//		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
//		// need to implement make
//		String[] args = {targetDir.toString()};
//		AMIProcessorPDF.main(args);
//		CProject cProject = new CProject(targetDir);
//		CTree cTree = cProject.getCTreeByName("goldberg");
//		File imagesDir = cTree.getExistingPDFImagesDir();
//		Assert.assertTrue(imagesDir.exists());
//		File smallDir = new File(imagesDir, AMIImageProcessor.SMALL);
//		Assert.assertFalse(smallDir.exists());
//		File monochromeDir = new File(imagesDir, AMIImageProcessor.MONOCHROME);
//		Assert.assertFalse(monochromeDir.exists());
//		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cTree).setMinHeight(0).setMinWidth(0).setDiscardMonochrome(true);
//		amiImageProcessor.runImages(cTree);
//		Assert.assertFalse(smallDir.exists());
//		Assert.assertTrue(monochromeDir.exists());
//		Assert.assertEquals(159,  monochromeDir.listFiles().length);
//	}

	@Test
	/** reads images in UCL corpus and outputs summary
	 * 
	 */
	public void testAll() throws Exception {
		File targetDir = new File(TARGET_UCLFOREST);
		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
		String[] args = {targetDir.toString()};
		AMIPDFTool.main(args);
		args = new String[] {targetDir.toString(), "help"};
		Assert.assertTrue(new File(args[0]).exists());
		AMIImageProcessor.main(args);
	}

//	@Test
//	/** reads images in UCL corpus and discards duplicate images
//	 * 
//	 */
//	public void testSingleDuplicate() throws Exception {
//		File targetDir = new File(TARGET_UCLFOREST);
//		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
//		String[] args = {targetDir.toString()};
//		AMIProcessorPDF.main(args);
//		args = new String[] {targetDir.toString(), "help"};
//		CProject cProject = new CProject(targetDir);
//		CTree cTree = cProject.getCTreeByName("goldberg");
//		File imagesDir = cTree.getExistingPDFImagesDir();
//		Assert.assertTrue(imagesDir.exists());
//		File smallDir = new File(imagesDir, AMIImageProcessor.SMALL);
//		Assert.assertFalse(smallDir.exists());
//		File duplicatesDir = new File(imagesDir, AMIImageProcessor.DUPLICATES);
//		LOG.debug("duplicates " + duplicatesDir);
////		Assert.assertFalse(duplicatesDir.exists());
//
//		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cTree).setMinHeight(0).setMinWidth(0).setDiscardDuplicates(true);
//		amiImageProcessor.runImages(cTree);
//		Assert.assertFalse(smallDir.exists());
//		Assert.assertTrue(duplicatesDir.exists());
//		int length = duplicatesDir.listFiles().length;
//		Assert.assertTrue("duplicates "+length, length > 150); // too flaky
//
//	}
	
//	@Test
//	/** reads images in UCL corpus and discards duplicate images
//	 * 
//	 */
//	public void testAllDuplicate() throws Exception {
//		File targetDir = new File(TARGET_UCLFOREST);
//		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
//		String[] args = {targetDir.toString()};
//		AMIProcessorPDF.main(args);
//		CProject cProject = new CProject(targetDir);
//		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cProject);
//		amiImageProcessor.setMinHeight(100).setMinWidth(100).setDiscardMonochrome(true).setDiscardDuplicates(true);
//		amiImageProcessor.runImages();
//
//	}
	
	
	
	@Test
	public void testExtractSingleImagePixelRings() {
		
		CTree cTree = new CTree(new File(FORESTPLOT_IMAGES_DIR, "campbell"));
		TreeImageManager treeImageManager = TreeImageManager.createTreeImageManager(cTree, cTree.getExistingPDFImagesDir());
		treeImageManager.setImageType(TreeImageType.RAW).setBasename("page.41.2");
		String pngFilename = "page.41.2.png";
		String imageRoot = FilenameUtils.getBaseName(pngFilename);
		File imageFile = treeImageManager.getImageFileDerived(pngFilename);
//		File imageFile = new File(cTree.getExistingPDFImagesDir(), pngFilename);
		Assert.assertTrue("exists "+imageFile, imageFile.exists());
		
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.setThinning(null);
		diagramAnalyzer.readAndProcessInputFile(imageFile);
		
		File ringDir = treeImageManager.getMakePixelDir();

		PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings();
		Assert.assertEquals("pixelRings", 6, pixelRingList.size());
		for (int i = 0; i < pixelRingList.size(); i++) {
			SVGSVG.wrapAndWriteAsSVG(pixelRingList.get(i).getOrCreateSVG(), 
					new File(ringDir, "ring." + i + "." + CTree.SVG));
			
		}
		
	}

	@Test
	public void testExtractMultipleImagePixelRings() {
		
		CTree cTree = new CTree(new File(FORESTPLOT_IMAGES_DIR, "campbell"));
		List<File> imageFiles = cTree.getOrCreatePDFImageManager().getRawImageFiles(CTree.PNG);
		File derivedImagesDir = cTree.getOrCreatePDFImageManager().getMakeOutputDirectory("derived");
		Collections.reverse(imageFiles);
		for (File imageFile : imageFiles) {
			String imageRoot = FilenameUtils.getBaseName(imageFile.toString());
			LOG.debug("root "+imageRoot);
			File derivedImageDir = new File(derivedImagesDir, imageRoot+"/");
			
			DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
			diagramAnalyzer.setThinning(null);
			diagramAnalyzer.readAndProcessInputFile(imageFile);
			BufferedImage bufferedImage = diagramAnalyzer.getImageProcessor().getBinarizedImage();
			ImageIOUtil.writeImageQuietly(bufferedImage, new File(derivedImageDir, "binarized.png"));
			
	
			PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings();
//			Assert.assertEquals("pixelRings", 6, pixelRingList.size());
			for (int i = 0; i < Math.min(6,  pixelRingList.size()); i++) {
				SVGSVG.wrapAndWriteAsSVG(pixelRingList.get(i).getOrCreateSVG(), 
						new File(derivedImageDir, "ring." + i + "." + CTree.SVG));
			}
			LOG.debug("end of pixels");
		}
		
	}
	
	@Test
	public void testExtractMultiplePixelIslands() {
		
		CTree cTree = new CTree(new File(FORESTPLOT_IMAGES_DIR, "campbell"));
		File derivedImagesDir = cTree.getOrCreatePDFImageManager().getMakeOutputDirectory("derived");
		List<File> imageFiles = cTree.getOrCreatePDFImageManager().getRawImageFiles(CTree.PNG);
		Collections.reverse(imageFiles);
		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cTree).setMaxPixelIslandSize(250000);
		amiImageProcessor.writeImageFilesForTree(derivedImagesDir, imageFiles);
	}

	@Test
	public void testAllPixelIslands() {
		CProject cProject = new CProject(FORESTPLOT_IMAGES_DIR);
		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cProject).setMaxPixelIslandSize(50000);
		amiImageProcessor.writeImageFilesForProject(cProject);
	}

	@Test
	public void testPixelIslandListAndRings() {
		LOG.debug(">> "+FORESTPLOT_IMAGES_DIR);
		CTree cTree = new CTree(new File(FORESTPLOT_IMAGES_DIR, "campbell"));
		File derivedImagesDir = cTree.getOrCreatePDFImageManager().getMakeOutputDirectory("derived");
		File pngDir = new File(derivedImagesDir, "page.41.2");
		LOG.debug(">> "+pngDir);
		File pngFile = new File(pngDir, "binarized.png");
		Assert.assertTrue(pngFile.exists());
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.setThinning(null);
		diagramAnalyzer.readAndProcessInputFile(pngFile);
		ImageProcessor imageProcessor = diagramAnalyzer.getImageProcessor();
		PixelIslandList pixelIslandList = imageProcessor.getOrCreatePixelIslandList();
		Assert.assertEquals("pil", 29, pixelIslandList.size());
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getOrCreateSVGG(), 
				new File(pngDir, "pixelIslands"+"." + CTree.SVG));
		
		List<PixelRing> pixelRingList = pixelIslandList.getOrCreatePixelRings();
		SVGG g = new SVGG();
		for (PixelRing pixelRing : pixelRingList) {
			g.appendChild(pixelRing.getOrCreateSVG());
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File(pngDir, "outerRings"+"." + CTree.SVG));
		
		Assert.assertEquals(29, pixelRingList.size());
	}

	@Test
	/** complete process from pixels to normalized horizontal lines
	 * SHOWCASE
	 */
	public void testPixelGraphGridExtractHorizLines() {
		CTree cTree = new CTree(new File(FORESTPLOT_IMAGES_DIR, "campbell"));
		LOG.debug("images>"+FORESTPLOT_IMAGES_DIR);
		File derivedImagesDir = cTree.getOrCreatePDFImageManager().getMakeOutputDirectory("derived");
		File pngDir = new File(derivedImagesDir, "page.41.2");
		File pngFile = new File(pngDir, "binarized.png");
		Assert.assertTrue(pngFile.exists());
		
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer().setDebug(false);
		diagramAnalyzer.setThinning(new ZhangSuenThinning());
		diagramAnalyzer.readAndProcessInputFile(pngFile);
		ImageProcessor imageProcessor = diagramAnalyzer.getImageProcessor();
		PixelIslandList pixelIslandList = imageProcessor.getOrCreatePixelIslandList();
		
		Assert.assertEquals("all islands", 29,  pixelIslandList.size());
		pixelIslandList.removeIslandsWithBBoxesLessThan(new Int2(10,10)); //<<<
		Assert.assertEquals("large islands",6,  pixelIslandList.size());
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getOrCreateSVGG(), 
				new File(pngDir, "pixelIslandsThin"+"." + CTree.SVG));
		
		PixelGraphList graphList = diagramAnalyzer.getOrCreateGraphList();
		Assert.assertEquals(6, graphList.size());
		graphList.drawGraphs(new File(pngDir, "rawgraphs.svg"));

		graphList.mergeNodesCloserThan(3.0);                            //<<<
		graphList.drawGraphs(new File(pngDir, "contracted.svg"));

		ComponentCache componentCache = new ComponentCache(); 
		LineCache lineCache = new LineCache(componentCache);
		lineCache.setSegmentTolerance(1.0);
		lineCache.addGraphList(graphList);
		
		IntArray xArray = lineCache.getGridXCoordinates();
//		graphList.snapNodesToArray(xArray, Axis2.X, 2);
		IntArray yArray = lineCache.getGridYCoordinates();
		graphList.snapNodesToArray(yArray, Axis2.Y, 1);
		
		
		graphList.drawGraphs(new File(pngDir, "snapped.svg"));
		LOG.debug("=========================");
		/** recreate cache to clear old values */
		lineCache = new LineCache(new ComponentCache());
		
		SVGLineList edgeLines = graphList.createLinesFromEdges();
		lineCache.addLines(edgeLines.getLineList());
		SVGLineList lineList0 = lineCache.getOrCreateLineList();
		SVGSVG.wrapAndWriteAsSVG(lineList0.createSVGElement(), new File(pngDir, "newLines.svg"));
		List<SVGLine> horLines = lineCache.getOrCreateHorizontalLineList();
		Assert.assertEquals("lines", 12, horLines.size());
		SVGLineList horSVGLineList = new SVGLineList(horLines);
		horSVGLineList.mergeLines(1.0, MergeMethod.OVERLAP);
		Assert.assertEquals("lines", 11, horSVGLineList.size());
		
		lineList0 = lineCache.getOrCreateLineList();
		
		SVGSVG.wrapAndWriteAsSVG(lineList0.createSVGElement(), new File(pngDir, "newLines1.svg"));
		// after this we are snapped to grid
		
		/**
		230
		[main] DEBUG org.contentmine.graphics.svg.cache.LineCache
		- g 
		[ 
 (155,14) (222,14) y-min nodes

 (105,19) (155,19) (182,19)

 (123,33) (155,33) (172,33) 

 (128,47) (153,47) (176,47)

 (156,62) (205,62)

 (153,76) (214,76) 

 (134,90) (155,90) (222,90) (231,90)

 (142,104) (155,104) (222,104) (224,104) 

 (150,118) (155,118) (219,118) 

 (137,147) (155,147) (222,147) (275,147) 

 (155,167) (222,167) y-max nodes
==
== 
 (165,133) (214,133)
==
 (160,162) (184,162)
==
==
 (21,14) (21,167)
== 
 (88,14) (88,167) 
==
 (289,14) (289,167)
*/
		/**
line: from((155.0,14.0)) to((155.0,18.0)) v((0.0,4.0)),
line: from((182.0,19.0)) to((156.0,19.0)) v((-26.0,0.0)),
line: from((172.0,33.0)) to((156.0,33.0)) v((-16.0,0.0)),
line: from((123.0,33.0)) to((154.0,34.0)) v((31.0,1.0)),
line: from((176.0,47.0)) to((154.0,46.0)) v((-22.0,-1.0)),
line: from((105.0,19.0)) to((154.0,19.0)) v((49.0,0.0)),
line: from((128.0,47.0)) to((153.0,47.0)) v((25.0,0.0)),
line: from((134.0,90.0)) to((154.0,90.0)) v((20.0,0.0)),
line: from((205.0,62.0)) to((156.0,62.0)) v((-49.0,0.0)),
line: from((142.0,104.0)) to((154.0,104.0)) v((12.0,0.0)),
line: from((150.0,118.0)) to((154.0,118.0)) v((4.0,0.0)),
line: from((214.0,76.0)) to((156.0,76.0)) v((-58.0,0.0)),
line: from((137.0,147.0)) to((154.0,147.0)) v((17.0,0.0)),
line: from((231.0,90.0)) to((223.0,90.0)) v((-8.0,0.0)),
line: from((155.0,167.0)) to((155.0,148.0)) v((0.0,-19.0)),
line: from((219.0,118.0)) to((156.0,118.0)) v((-63.0,0.0)),
line: from((222.0,14.0)) to((222.0,89.0)) v((0.0,75.0)),
line: from((222.0,167.0)) to((222.0,148.0)) v((0.0,-19.0)),
line: from((275.0,147.0)) to((223.0,147.0)) v((-52.0,0.0)),
line: from((155.0,20.0)) to((155.0,32.0)) v((0.0,12.0)),
line: from((155.0,35.0)) to((154.0,46.0)) v((-1.0,11.0)),
line: from((153.0,47.0)) to((155.0,51.0)) v((2.0,4.0)),
line: from((155.0,51.0)) to((156.0,62.0)) v((1.0,11.0)),
line: from((156.0,62.0)) to((155.0,75.0)) v((-1.0,13.0)),
line: from((155.0,77.0)) to((155.0,89.0)) v((0.0,12.0)),
line: from((156.0,90.0)) to((221.0,90.0)) v((65.0,0.0)),
line: from((155.0,91.0)) to((155.0,103.0)) v((0.0,12.0)),
line: from((156.0,104.0)) to((221.0,104.0)) v((65.0,0.0)),
line: from((155.0,105.0)) to((155.0,117.0)) v((0.0,12.0)),
line: from((155.0,119.0)) to((155.0,146.0)) v((0.0,27.0)),
line: from((156.0,147.0)) to((221.0,147.0)) v((65.0,0.0)),
line: from((222.0,91.0)) to((222.0,103.0)) v((0.0,12.0)),
line: from((222.0,105.0)) to((222.0,146.0)) v((0.0,41.0)),
line: from((289.0,14.0)) to((289.0,167.0)) v((0.0,153.0)),
line: from((88.0,14.0)) to((88.0,167.0)) v((0.0,153.0)),
line: from((21.0,14.0)) to((21.0,167.0)) v((0.0,153.0)),
line: from((165.0,133.0)) to((214.0,133.0)) v((49.0,0.0)),
line: from((160.0,162.0)) to((184.0,162.0)) v((24.0,0.0))		 */
		
		SVGG gg = new SVGG();
		gg.appendChildCopies(SVGElement.addAttributes(lineCache.getOrCreateLineList().getLineList(), 
				new Attribute("stroke", "gray"), new Attribute("stroke-width", "2.5")));
		gg.appendChildCopies(SVGElement.addAttributes(lineCache.getOrCreateHorizontalLineList(), 
				new Attribute("stroke", "red"), new Attribute("stroke-width", "1.5"), new Attribute("opacity", "0.3")));
		if (false || true) {
			gg.appendChildCopies(SVGElement.addAttributes(lineCache.getOrCreateVerticalLineList(), 
					new Attribute("stroke", "black"), new Attribute("stroke-width", "0.5")));
		}
		
		SVGSVG.wrapAndWriteAsSVG(gg, new File(pngDir, "horizontalVertical.svg"));
		SVGSVG.wrapAndWriteAsSVG(horSVGLineList.getLineList(), new File(pngDir, "horizontal.svg"));
	}

	@Test
	public void testExtractSingleArticlePixelRings() {
		CTree cTree = new CTree(new File(FORESTPLOT_DIR, "campbell"));
//		cTree.setIncludeImageBasenames("image.41.2%","image.42.1%","image.42.5%","image.43.1%","image.44.5%");
		List<File> imageFiles = cTree.getOrCreatePDFImageManager().getRawImageFiles(CTree.PNG);
		int minNestedRings = 2;
		Double radius = 5.0;
		for (File imageFile : imageFiles) {
			File parentFile = imageFile.getParentFile();
			LOG.debug(">>>>>"+imageFile.getName() +"/"+parentFile );
			String baseName = FilenameUtils.getBaseName(imageFile.toString());
			DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
			diagramAnalyzer.setThinning(null);
			diagramAnalyzer.readAndProcessInputFile(imageFile);
			BufferedImage image = diagramAnalyzer.getImage();
			if (image == null || image.getWidth() * image.getHeight() > 1000000) {
				LOG.debug("skipped "+imageFile);
			} else {
				// list of pixelRings by island
				List<PixelRingList> pixelRingListList = diagramAnalyzer.createDefaultPixelRingListList();
				SVGG totalSVG = new SVGG();
				for (int isl = 0; isl < pixelRingListList.size(); isl++) {
					PixelRingList pixelRingListIsland = pixelRingListList.get(isl);
					int nestedRings = pixelRingListIsland.size();
					if (nestedRings > minNestedRings) {
						for (int ring = 0; ring < nestedRings; ring++) {
							PixelRing pixelRing = pixelRingListIsland.get(ring);
							SVGG ringSVG = pixelRing.getOrCreateSVG();
							// lower nesting likely to contain isolated points
							if (ring == minNestedRings - 1) {
								IslandRingList islandRingList = IslandRingList.createFromPixelRing(pixelRing, null);
								for (PixelRing islandRing : islandRingList) {
									Real2 centreCoordinate = islandRing.getCentreCoordinate();
									SVGCircle circle = (SVGCircle) new SVGCircle(centreCoordinate, radius)
											.setStrokeWidth(0.7).setOpacity(0.4).setStroke("green");
									ringSVG.appendChild(circle);
									totalSVG.appendChild(circle.copy());
								}
								totalSVG.appendChild(ringSVG.copy());
								SVGSVG.wrapAndWriteAsSVG(ringSVG, 
								new File(parentFile, baseName+"."+"ring." + isl + "." + ring + "." + CTree.SVG));
							}
						}
					}
				}
				SVGSVG.wrapAndWriteAsSVG(totalSVG, 
					new File(parentFile, baseName+"."+"total" + "." + CTree.SVG));
			}
		}		
	}

	@Test
	public void testExtractSeveralArticlePixelRings() {
		CProject cProject = new CProject(FORESTPLOT_DIR);
		CTreeList cTreeList = cProject.getOrCreateCTreeList();
		
		String[] treeNames = new String[]{
				"busick", 
				"campbell", 
				"case", 
				"casejuly", 
				"cole",
				"davis",
				"donker",
				"ergen",
				"fan",
				"kunkel",
				"marulis",
				"mcarthur",
				"puzio",
				"rui",
				"shenderovich",
				"zheng",
		};
				
		for (String name : treeNames) {
			CTree cTree = cTreeList.get(name);
			if (cTree == null) continue;
			File imageDir = cTree.getExistingPDFImagesDir();
			for (File imageFile : imageDir.listFiles()) {
				if (imageFile.toString().endsWith(".png")) {
					LOG.debug(imageFile);
					String baseName = FilenameUtils.getBaseName(imageFile.toString());
					DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
					diagramAnalyzer.setThinning(null);
					diagramAnalyzer.readAndProcessInputFile(imageFile);
					BufferedImage image = diagramAnalyzer.getImage();
					if (image == null || image.getWidth() * image.getHeight() > 1000000) {
						LOG.debug("skipped "+imageFile);
					} else {
						PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings();
						for (int i = 0; i < pixelRingList.size(); i++) {
							SVGSVG.wrapAndWriteAsSVG(pixelRingList.get(i).getOrCreateSVG(), 
									new File(imageFile.getParentFile(), baseName+"."+"ring." + i + "." + CTree.SVG));
						}
					}
				}
			}
		}		
	}

	@Test
	public void testExtractBinarizedImages() {
		CProject cProject = new CProject(FORESTPLOT_DIR);
		CTreeList cTreeList = cProject.getOrCreateCTreeList();
		
		for (CTree cTree : cTreeList) {
			File imageDir = cTree.getExistingPDFImagesDir();
			for (File imageFile : imageDir.listFiles()) {
				if (imageFile.toString().endsWith(".png")) {
					LOG.debug(imageFile);
					String baseName = FilenameUtils.getBaseName(imageFile.toString());
					DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
					diagramAnalyzer.setThinning(null);
					diagramAnalyzer.readAndProcessInputFile(imageFile);
					BufferedImage image = diagramAnalyzer.getImageProcessor().getBinarizedImage();
					if (image == null) {
						LOG.debug("skipped null "+imageFile);
					} else {
						File binarizedFile = new File(imageFile.getParentFile(), baseName+"."+"bin." + CTree.PNG);
						ImageIOUtil.writeImageQuietly(image, binarizedFile);
						LOG.debug("wrote "+binarizedFile);
					}
				}
			}
		}		
	}
	
	@Test
	public void testSingleGoodTesseract() {
//		 tesseract  /Users/pm286/workspace/uclforest/forestplots/shenderovich/image/derived/page.11.1.bin.png test tsv
		String ctreeName = "shenderovich";
		CTree cTree = new CTree(new File(FORESTPLOT_DIR, ctreeName));
		File outputDir = new File(TARGET_HOCR, ctreeName);
		File imageDir = new File(cTree.getExistingPDFImagesDir(), CTree.DERIVED); 
		String base = "image.11.1.41_508.565_732";
		File imageFile = new File(imageDir, base + ".png");
		/** Tesseract adds the suffix ".hocr" automatically */
		File outputBase = new File(outputDir, base);
		Assert.assertTrue(imageFile+" should exist", imageFile.exists());
		AbstractAMITool amiImageProcessor = AMIImageProcessor.createAIProcessor(cTree);
		amiImageProcessor.setCTreeOutputDir(outputDir);
		ImageToHOCRConverter imageToHOCRConverter = new ImageToHOCRConverter();
		File hocrHtmlFile = imageToHOCRConverter.writeHOCRFile(imageFile, outputBase);
		LOG.debug(outputBase+" / "+hocrHtmlFile);
		
	}

	@Test
	public void testMediumTesseract() {
		CProject cProject = new CProject(FORESTPLOT_DIR);
		String cTreeName = "buzick";
		CTree cTree = cProject.getCTreeByName(cTreeName);
		File outputDir = new File("target/hocrx", cTreeName);
		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cProject);
		amiImageProcessor.setCTreeOutputDir(outputDir);
		amiImageProcessor.convertImageAndWriteHOCRFiles(cTree, outputDir);
	}
	
	@Test
	public void testCProjectImages() {
		CProject cProject = new CProject(FORESTPLOT_DIR);
		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cProject);
		amiImageProcessor.setCProjectOutputDir(new File(TARGET_HOCR));
		for (CTree cTree : cProject.getOrCreateCTreeList()) {
			File outputDir = new File(TARGET_HOCR, cTree.getName());
			amiImageProcessor.setCTreeOutputDir(outputDir);
			amiImageProcessor.convertImageAndWriteHOCRFiles(cTree, outputDir);
		}
		
	}
	
	@Test
	public void testForestPlotLinesAndSymbols() {
		CTree cTree = new CTree(new File(FORESTPLOT_IMAGES_DIR, "campbell"));
		File derivedImagesDir = cTree.getOrCreatePDFImageManager().getMakeOutputDirectory("derived");
		File pngDir = new File(derivedImagesDir, "page.41.2");
		File pngFile = new File(pngDir, "binarized.png");
		
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer().setDebug(false);
		diagramAnalyzer.setThinning(new ZhangSuenThinning());
		diagramAnalyzer.readAndProcessInputFile(pngFile);
		ImageProcessor imageProcessor = diagramAnalyzer.getImageProcessor();
		PixelIslandList pixelIslandList = imageProcessor.getOrCreatePixelIslandList();
		
		pixelIslandList.removeIslandsWithBBoxesLessThan(new Int2(10,10)); //<<<
		
		PixelGraphList graphList = diagramAnalyzer.getOrCreateGraphList();
		graphList.mergeNodesCloserThan(3.0);                            //<<<

		ComponentCache componentCache = new ComponentCache(); 
		LineCache lineCache = new LineCache(componentCache);
		lineCache.setSegmentTolerance(1.0);
		lineCache.addGraphList(graphList);
		
		IntArray xArray = lineCache.getGridXCoordinates();
		IntArray yArray = lineCache.getGridYCoordinates();
		graphList.snapNodesToArray(yArray, Axis2.Y, 1);
		
		/** recreate cache to clear old values */
		lineCache = new LineCache(new ComponentCache());
		
		SVGLineList edgeLines = graphList.createLinesFromEdges();
		lineCache.addLines(edgeLines.getLineList());
		SVGLineList horSVGLineList = lineCache.getOrCreateHorizontalSVGLineList();
		horSVGLineList.mergeLines(1.0, MergeMethod.OVERLAP);
		
//
		
//		PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings(imageFile);
		List<PixelRingList> pixelRingListList = pixelIslandList.createInternalPixelRingListList();
//		PixelRingList aggregatedPixelRingList = pixelIslandList.createAggregatedInternalPixelRingList();
		SVGG g = new SVGG();
		for (PixelRingList pixelRingList : pixelRingListList) {
			SVGG gg = pixelRingList.plotPixels();
			g.appendChild(gg);
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File(pngDir, "aggregated.svg"));

		LOG.debug("end of pixels");

	}
}