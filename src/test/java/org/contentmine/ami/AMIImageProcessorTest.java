package org.contentmine.ami;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.CTreeList;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.util.ImageIOUtil;
import org.contentmine.image.diagram.DiagramAnalyzer;
import org.contentmine.image.pixel.PixelRing;
import org.contentmine.image.pixel.PixelRingList;
import org.contentmine.norma.image.ocr.ImageToHOCRConverter;
import org.junit.Assert;
import org.junit.Test;

/** test AMIProcessorPDF
 * 
 * @author pm286
 *
 */
public class AMIImageProcessorTest {
	private static final File UCLFOREST_DIR = new File("/Users/pm286/workspace/uclforest/");
	private static final String TARGET_HOCR = "target/hocr";
	public static final File FORESTPLOT_DIR = new File(UCLFOREST_DIR, "forestplots/");
	public static final File FORESTPLOT_CONVERTED_DIR = new File(UCLFOREST_DIR, "forestplotsConverted/");
	private static final String TARGET_UCLFOREST = "target/uclforest/";
	public static final Logger  LOG = Logger.getLogger(AMIImageProcessorTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	/** reads images in UCL corpus and excludes small/narroe
	 * 
	 */
	public void testMinWidthHeight() {
		File targetDir = new File(TARGET_UCLFOREST);
		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_CONVERTED_DIR, targetDir);
		// need to implement make
		CProject cProject = new CProject(targetDir);
		AMIProcessorPDF.runPDF(cProject);
		CTree cTree = cProject.getCTreeByName("case");
		File pdfImagesDir = cTree.getExistingPDFImagesDir();
		Assert.assertTrue(pdfImagesDir.exists());
		File smallDir = new File(pdfImagesDir, AMIImageProcessor.SMALL);
//		LOG.debug("small "+smallDir);
		Assert.assertFalse(smallDir.exists());
		File monochromeDir = new File(pdfImagesDir, AMIImageProcessor.MONOCHROME);
		Assert.assertFalse(monochromeDir.exists());
		AMIImageProcessor amiImageProcessor = new AMIImageProcessor()
				.setMinHeight(100).setMinWidth(100).setDiscardDuplicates(true).setDiscardMonochrome(true);
		amiImageProcessor.runImages(cTree);
		Assert.assertTrue(""+smallDir + "should exists", smallDir.exists());
		Assert.assertEquals(59,  smallDir.listFiles().length);
		amiImageProcessor.runImages();
	}

	@Test
	/** reads images in UCL corpus and excludes monochrome images
	 * 
	 */
	public void testMonochrome() {
		File targetDir = new File(TARGET_UCLFOREST);
		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
		// need to implement make
		String[] args = {targetDir.toString()};
		AMIProcessorPDF.main(args);
		CProject cProject = new CProject(targetDir);
		CTree cTree = cProject.getCTreeByName("goldberg");
		File imagesDir = cTree.getExistingPDFImagesDir();
		Assert.assertTrue(imagesDir.exists());
		File smallDir = new File(imagesDir, AMIImageProcessor.SMALL);
		Assert.assertFalse(smallDir.exists());
		File monochromeDir = new File(imagesDir, AMIImageProcessor.MONOCHROME);
		Assert.assertFalse(monochromeDir.exists());
		AMIImageProcessor amiImageProcessor = new AMIImageProcessor().setMinHeight(0).setMinWidth(0).setDiscardMonochrome(true);
		amiImageProcessor.runImages(cTree);
		Assert.assertFalse(smallDir.exists());
		Assert.assertTrue(monochromeDir.exists());
		Assert.assertEquals(159,  monochromeDir.listFiles().length);
	}

	@Test
	/** reads images in UCL corpus and outputs summary
	 * 
	 */
	public void testAll() {
		File targetDir = new File(TARGET_UCLFOREST);
		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
		String[] args = {targetDir.toString()};
		AMIProcessorPDF.main(args);
		args = new String[] {targetDir.toString(), "help"};
		Assert.assertTrue(new File(args[0]).exists());
		AMIImageProcessor.main(args);
	}

	@Test
	/** reads images in UCL corpus and discards duplicate images
	 * 
	 */
	public void testSingleDuplicate() {
		File targetDir = new File(TARGET_UCLFOREST);
		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
		String[] args = {targetDir.toString()};
		AMIProcessorPDF.main(args);
		args = new String[] {targetDir.toString(), "help"};
		CProject cProject = new CProject(targetDir);
		CTree cTree = cProject.getCTreeByName("goldberg");
		File imagesDir = cTree.getExistingPDFImagesDir();
		Assert.assertTrue(imagesDir.exists());
		File smallDir = new File(imagesDir, AMIImageProcessor.SMALL);
		Assert.assertFalse(smallDir.exists());
		File duplicatesDir = new File(imagesDir, AMIImageProcessor.DUPLICATES);
		LOG.debug("duplicates " + duplicatesDir);
//		Assert.assertFalse(duplicatesDir.exists());

		AMIImageProcessor amiImageProcessor = new AMIImageProcessor().setMinHeight(0).setMinWidth(0).setDiscardDuplicates(true);
		amiImageProcessor.runImages(cTree);
		Assert.assertFalse(smallDir.exists());
		Assert.assertTrue(duplicatesDir.exists());
		int length = duplicatesDir.listFiles().length;
		Assert.assertTrue("duplicates "+length, length > 150); // too flaky

	}
	
	@Test
	/** reads images in UCL corpus and discards duplicate images
	 * 
	 */
	public void testAllDuplicate() {
		File targetDir = new File(TARGET_UCLFOREST);
		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
		String[] args = {targetDir.toString()};
		AMIProcessorPDF.main(args);
		CProject cProject = new CProject(targetDir);
		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cProject);
		amiImageProcessor.setMinHeight(100).setMinWidth(100).setDiscardMonochrome(true).setDiscardDuplicates(true);
		amiImageProcessor.runImages();

	}
	
	
	
	@Test
	public void testExtractSingleImagePixelRings() {
		/** recreates clean target*/
		
		/** in production mode
		File targetDir = new File(TARGET_UCLFOREST);
		CMineTestFixtures.cleanAndCopyDir(FORESTPLOT_DIR, targetDir);
		String[] args = {targetDir.toString()};
		AMIProcessorPDF.main(args);
//		args = new String[] {targetDir.toString(), "help"};
		CProject cProject = new CProject(targetDir);
		CTree cTree = cProject.getCTreeByName("campbell");
		File imagesDir = cTree.getExistingPDFImagesDir();
		Assert.assertTrue(imagesDir.exists());
		File imageFile = new File(imagesDir, "page.41.2.png");
		*/
		CTree cTree = new CTree(new File(FORESTPLOT_DIR, "campbell"));
		File imageFile = new File(cTree.getExistingPDFImagesDir(), "page.41.2.png");
		Assert.assertTrue("exists "+imageFile, imageFile.exists());
		
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.setThinning(null);
		diagramAnalyzer.readAndProcessInputFile(imageFile);
		PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings(imageFile);
		Assert.assertEquals("pixelRings", 6, pixelRingList.size());
		int pixelRingID = 1;
		PixelRing pixelRing1 = pixelRingList.get(pixelRingID);
		SVGSVG.wrapAndWriteAsSVG(pixelRing1.getOrCreateSVG(), 
				new File(cTree.getOrCreateDerivedImagesDir(), "ring." + pixelRingID + "." + CTree.SVG));
		for (int i = 0; i < pixelRingList.size(); i++) {
			SVGSVG.wrapAndWriteAsSVG(pixelRingList.get(i).getOrCreateSVG(), 
					new File(cTree.getOrCreateDerivedImagesDir(), "ring." + i + "." + CTree.SVG));
			
		}
		
	}

	@Test
	public void testExtractSingleArticlePixelRings() {
		CTree cTree = new CTree(new File(FORESTPLOT_DIR, "campbell"));
		
		for (File imageFile : cTree.getExistingPDFImagesDir().listFiles()) {
			LOG.debug(imageFile);
			String baseName = FilenameUtils.getBaseName(imageFile.toString());
			DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
			diagramAnalyzer.setThinning(null);
			diagramAnalyzer.readAndProcessInputFile(imageFile);
			BufferedImage image = diagramAnalyzer.getImage();
			if (image == null || image.getWidth() * image.getHeight() > 1000000) {
				LOG.debug("skipped "+imageFile);
			} else {
				PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings(imageFile);
				for (int i = 0; i < pixelRingList.size(); i++) {
					SVGSVG.wrapAndWriteAsSVG(pixelRingList.get(i).getOrCreateSVG(), 
							new File(cTree.getOrCreateDerivedImagesDir(), baseName+"."+"ring." + i + "." + CTree.SVG));
				}
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
						PixelRingList pixelRingList = diagramAnalyzer.createDefaultPixelRings(imageFile);
						for (int i = 0; i < pixelRingList.size(); i++) {
							SVGSVG.wrapAndWriteAsSVG(pixelRingList.get(i).getOrCreateSVG(), 
									new File(cTree.getOrCreateDerivedImagesDir(), baseName+"."+"ring." + i + "." + CTree.SVG));
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
						File binarizedFile = new File(cTree.getOrCreateDerivedImagesDir(), baseName+"."+"bin." + CTree.PNG);
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
		String base = "page.11.1";
		File imageFile = new File(imageDir, base + ".bin.png");
		/** Tesseract adds the suffix ".hocr" automatically */
		File outputBase = new File(outputDir, base);
		Assert.assertTrue(imageFile+" should exist", imageFile.exists());
		AMIImageProcessor amiImageProcessor = AMIImageProcessor.createAIProcessor(cTree);
		amiImageProcessor.setCTreeOutputDir(outputDir);
		ImageToHOCRConverter imageToHOCRConverter = new ImageToHOCRConverter();
		File hocrHtmlFile = imageToHOCRConverter.writeHOCRFile(imageFile, outputBase);
		
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

}