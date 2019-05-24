package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.gocr.GOCRPageElement;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.eucl.euclid.Int2;
import org.contentmine.eucl.euclid.Int2Range;
import org.contentmine.eucl.euclid.Util;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGImage;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.image.ImageUtil;
import org.contentmine.norma.NormaFixtures;
import org.contentmine.norma.image.ocr.GOCRCharBox;
import org.contentmine.norma.image.ocr.GOCRConverter;
import org.junit.Test;

import com.google.common.collect.Multimap;

/** test OCR.
 * 
 * @author pm286
 *
 */
public class AMIOCRTest {
	private static final Logger LOG = Logger.getLogger(AMIOCRTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** 
	 * convert single (good) file
	 */
	public void testHelp() throws Exception {
		String args = 
				"-t /Users/pm286/workspace/uclforest/dev/shenderovich"
				+ " --html true"
			;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** 
	 * convert single (good) file
	 */
	public void testHOCR() throws Exception {
		String[] args = {
				"-t", "/Users/pm286/workspace/uclforest/dev/shenderovich",
				"--html", "true"
			};
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** 
	 * convert single (moderate) tree
	 */
	public void testHOCR1() throws Exception {
		String args = 
				"-t /Users/pm286/workspace/uclforest/dev/buzick"
				+ " --html true"
			;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** convert whole project
	 * most files are too low resolution to convert well
	 * 
	 * @throws Exception
	 */
	public void testHOCRProject() throws Exception {
		String args = ""
				+ "-p /Users/pm286/workspace/uclforest/dev --html true"
				;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** scale small text
	 * 
	 * @throws Exception
	 */
	public void testForceScale() throws Exception {
		String args = ""
				+ "-t /Users/pm286/workspace/uclforest/dev/case"
				+ " --html true"
				+ " --scalefactor 2.0"
				;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** scale small text
	 * 
	 * @throws Exception
	 */
	public void testForceScaleProject() throws Exception {
		String args = ""
				+ "-p /Users/pm286/workspace/uclforest/dev"
				+ " --html true"
				+ " --scalefactor 1.7"
				+ " --filename scale1_7"
				;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** scale small text
	 * 
	 * @throws Exception
	 */
	public void testScaleMaxsize() throws Exception {
		String args = ""
				+ "-t /Users/pm286/workspace/uclforest/dev/case"
				+ " --html true"
				+ " --maxsize 700"
				+ " --scaled maxsize"
				;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** scale small text
	 * 
	 * @throws Exception
	 */
	public void testScaleMaxsizeProject() throws Exception {
		String args = ""
				+ "-p /Users/pm286/workspace/uclforest/dev/"
				+ " --html true"
				+ " --maxsize 700"
				+ " --filename maxsize700"
				;
		new AMIOCRTool().runCommands(args);
	}

	
	@Test
	/** scale small text
	 * 
	 * @throws Exception
	 */
	public void testScaleOCR() throws Exception {
		String args = ""
				+ "-t /Users/pm286/workspace/uclforest/devtest/case_systematic_review_ar"
				+ " --html true"
//				+ " --maxsize 700"
//				+ " --filename maxsize700"
                + " --scalefactor 2.0"
				;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	/** scale small text
	 * 
	 * @throws Exception
	 */
	public void testScaleOCRProject() throws Exception {
		String args = ""
				+ "-p /Users/pm286/workspace/uclforest/devtest/"
				+ " --includetree"
				+ " buzick%"
				+ " case_systematic_review_ar"
				+ " case_systematic_review_ju"
				+ " cole_2014"
				+ " dietrichson%"
				+ " donkerdeboerkostons2014_l"
				+ " ergen_canagli_17_"
				+ " fanetal_2017_meta_science"
//				+ " fauzan03"  // large scanned
				+ " higginshallbaumfieldmosel"
				+ " kunkel_2015"
				+ " marulis_2010-300-35review"
				+ " mcarthur_etal2012_cochran"
				+ " puziocolby2013_co-operati"
				+ " rui2009_meta_detracking"
				+ " shenderovich_2016_pub"
//				+ " tamim-2009-effectsoftechn" // large scanned
				+ " zhengetal_2016"
				+ ""
				+ " --html true"
                + " --scalefactor 2.0"
				;
		new AMIOCRTool().runCommands(args);
	}
	
	@Test
	public void testBatteryGraph() {
		CTree cTree = new CTree(new File(NormaFixtures.TEST_IMAGES_DIR, "ocr/battery"));
		LOG.debug("ctree "+cTree);
//		File plotImageFile = new File(batteryDir, "panel0_0_128_true.png");
//		Assert.assertTrue(plotImageFile.exists());
		AMIOCRTool ocrTool = new AMIOCRTool();
		String cmd = "--ctree "+cTree.getDirectory();
		LOG.debug(cmd);
		ocrTool.runCommands(cmd);
		
	
	}

	@Test
	public void testSPSS() {
		File projectDir = new File("/Users/pm286/projects/forestplots/spss");
//		CTree cTree = new CTree(new File(projectDir, "PMC5502154"));
		CProject cProject = new CProject(projectDir);
		AMIOCRTool ocrTool = new AMIOCRTool();

//		String cmd = "--ctree "+cTree.getDirectory();
		String cmd = "--cproject "+cProject.getDirectory();
//		cmd += " --inputname raw_sc_2_s4_b_10_thr_150";
//		cmd += " --outputname raw_sc_2_s4_b_10_thr_150";
		cmd += " --inputname raw_s4_b_10_thr_180";
		cmd += " --outputname raw_s4_b_10_thr_180";
				
//		LOG.debug(cmd);
		ocrTool.runCommands(cmd);
	
	}
	
	@Test
	public void testGOCRa() throws Exception {
		File projectDir = new File("/Users/pm286/projects/forestplots/spssSimple");
		CTree cTree = new CTree(new File(projectDir, "PMC5502154"));
		File pdfImageDir = new File(cTree.getDirectory(), "pdfimages/");
		File imageDir = new File(pdfImageDir, "image.4.3.96_553.569_697/");
		File inputFile = new File(imageDir, "raw.png");

		File gocrXmlFile = new File(imageDir, "raw.gocr.xml");
		GOCRConverter gocrConverter = new GOCRConverter();
		gocrConverter.createGOCRElement(inputFile, gocrXmlFile);
		SVGElement svgElement = gocrConverter.createSVGElementWithGlyphs(imageDir);
		File svgFile = new File(imageDir, "raw.gocr.svg");
		SVGSVG.wrapAndWriteAsSVG(svgElement, svgFile);	
		
		if (false) {
			gocrConverter.createMaps(svgElement);
			gocrConverter.correlateImagesForGlyphs();
		}

	}
	
	@Test
	public void testGOCR() throws Exception {
		File projectDir = new File("/Users/pm286/projects/forestplots/spssSimple");
		CProject cProject = new CProject(projectDir);
		CTree cTree = new CTree(new File(projectDir, "PMC5502154"));
		AMIOCRTool ocrTool = new AMIOCRTool();
		String cmd = ""
				+ "--cproject "+cProject.getDirectory()
				+ " --gocr /usr/local/bin/gocr"
				+ " --html false"
		;
		LOG.debug(cmd);
		ocrTool.runCommands(cmd);

	}

	@Test
	public void testGOCRBasename() throws Exception {
		File projectDir = new File("/Users/pm286/projects/forestplots/spssSimple");
		CProject cProject = new CProject(projectDir);
		CTree cTree = new CTree(new File(projectDir, "PMC5502154"));
		AMIOCRTool ocrTool = new AMIOCRTool();
		String cmd = ""
				+ "--cproject "+cProject.getDirectory()
				+ " --gocr /usr/local/bin/gocr"
				+ " --html false"
				+ " --inputname raw.table.body"
		;
		LOG.debug(cmd);
		ocrTool.runCommands(cmd);

	}

	@Test
	public void testGOCRSharpen() throws Exception {
		File projectDir = new File("/Users/pm286/projects/forestplots/spssSimple");
		CProject cProject = new CProject(projectDir);
		String imageBaseCmd = ""
				+ "--cproject "+cProject.getDirectory()
				+ " --sharpen sharpen4"
				+ " --threshold 180"
				;
		;
		new AMIImageTool().runCommands(imageBaseCmd + " --inputname " + "raw.table.header");
		new AMIImageTool().runCommands(imageBaseCmd + " --inputname " + "raw.table.body");
		new AMIImageTool().runCommands(imageBaseCmd + " --inputname " + "raw.graph.header");
		new AMIImageTool().runCommands(imageBaseCmd + " --inputname " + "raw.graph.footer");
		
		AMIOCRTool ocrTool = new AMIOCRTool();
		String ocrBaseCmd = "--cproject "+cProject.getDirectory()+" --gocr /usr/local/bin/gocr --html false";

		new AMIOCRTool().runCommands(ocrBaseCmd + " --inputname raw.graph.footer_s4_b_10_thr_180");
		new AMIOCRTool().runCommands(ocrBaseCmd + " --inputname raw.graph.header_s4_b_10_thr_180");
		new AMIOCRTool().runCommands(ocrBaseCmd + " --inputname raw.table.footer_s4_b_10_thr_180");
		new AMIOCRTool().runCommands(ocrBaseCmd + " --inputname raw.table.body_s4_b_10_thr_180");

	}


}
