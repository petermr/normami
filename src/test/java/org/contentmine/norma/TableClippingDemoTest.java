package org.contentmine.norma;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.eucl.euclid.Real2;
import org.contentmine.graphics.svg.SVGElement;
import org.contentmine.graphics.svg.SVGSVG;
import org.contentmine.graphics.svg.SVGText;
import org.contentmine.graphics.svg.fonts.StyleRecordFactory;
import org.contentmine.graphics.svg.fonts.StyleRecordSet;
import org.contentmine.graphics.svg.fonts.TypefaceMaps;
import org.contentmine.norma.Norma;
import org.contentmine.norma.NormaRunner;
import org.contentmine.norma.pubstyle.util.RegionFinder;
import org.contentmine.svg2xml.page.PageCropper;
import org.contentmine.svg2xml.page.PageCropper.Units;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/** creates complete workflow for extracting clipped tables.
 * 
 * @author pm286
 *
 */
public class TableClippingDemoTest {
	private static final String LANCET_BOX_FILL = "#b30838";
	public static final Logger LOG = Logger.getLogger(TableClippingDemoTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public static final String FULLTEXT_PAGE = "fulltext-page";
	
	@Test
	@Ignore
	public void testGlobbing() throws IOException {
		File svgDir = new File("target/clipping/tracemonkey-pldi-09", "svg");
		Assert.assertTrue(""+svgDir+" is existing dir", svgDir.exists() && svgDir.isDirectory());
		CMineGlobber globber = new CMineGlobber();
		globber.setLocation(svgDir.toString());
		globber.setRegex(".*/fulltext\\-page.*\\.svg");
		List<File> fulltextFiles = globber.listFiles();
		LOG.debug(fulltextFiles);
		Assert.assertEquals(14, fulltextFiles.size());
	}
	
	@Test
	@Ignore
	public void testCropping() {
		PageCropper cropper = new PageCropper();
		cropper.setTLBRUserMediaBox(new Real2(0, 800), new Real2(600, 0));
		Assert.assertEquals("cropToLocalTransformation", 
			"(1.0,0.0,0.0,\n"
			+ "0.0,-1.0,800.0,\n"
			+ "0.0,0.0,1.0,)",
			cropper.getCropToLocalTransformation().toString());
		// clip a table - cropping coordinates, 
		cropper.setTLBRUserCropBox(new Real2(50, 467), new Real2(293, 242));
		String fileroot = FULLTEXT_PAGE+1;
		File svgDir = new File("target/clipping/tracemonkey-pldi-09/svg/");
		File inputFile = new File(svgDir, fileroot + ".svg");
		Assert.assertTrue(""+inputFile+" exists", inputFile.exists());
		SVGElement svgElement = SVGElement.readAndCreateSVG(inputFile);
		List<SVGElement> descendants = cropper.extractDescendants(svgElement);
		Assert.assertEquals("contained ", 4287, descendants.size());
		SVGSVG.wrapAndWriteAsSVG(descendants, new File(new File("target/crop/"), fileroot+".raw.svg"));
		List<SVGElement> contained = cropper.extractContainedElements(descendants);
		Assert.assertEquals("contained ", 950, contained.size());
		SVGSVG.wrapAndWriteAsSVG(contained, new File(new File("target/crop/"), fileroot+".crop.svg"));
		
		/**
top: 117.3, left: 17.6, width: 85.6, height: 79.5
		 */
		cropper = new PageCropper();
		svgElement = SVGElement.readAndCreateSVG(inputFile);
		cropper.setSVGElementCopy(svgElement);
		double x0 = 117.3;
		double width = 85.6;
		double x1 = x0 + width;
		double y0 = 17.6;
		double height = 79.5;
		double y1 = y0 + height;
		svgElement = cropper.cropElementTLBR(new Real2(x0, y0), new Real2(x1, y1), Units.MM);
		Assert.assertNotNull(svgElement);
		SVGSVG.wrapAndWriteAsSVG(svgElement, new File(new File("target/crop/"), fileroot+".cropmmx.svg"));

	}
	
	@Test
	/** may move elsewhere later
	 * assumes SVG files have been created in target.
	 */
	@Ignore("missing files")
	public void testCroppingArguments() {
		File projectDir = new File("target/clipping/tracemonkey-pldi-09/");
		File svgDir = new File("target/clipping/tracemonkey-pldi-09/svg/");
		String fileroot = FULLTEXT_PAGE+1;
		File inputFile = new File(svgDir, fileroot + ".svg");
		Assert.assertTrue(""+inputFile+" exists", inputFile.exists());
//		SVGElement svgElement = SVGElement.readAndCreateSVG(inputFile);
		/**
		double MM2PX = 72 / 25.4;
		double x0 = 17.6; // mm
		double width = 85.6; // mm
		double x1 = x0 + width;
//		double y0 = 117.3;
		double y0 = (800 / MM2PX) - 117.3; // coordinate system wrong way up // mm
		double height = 79.5; // mm
		double y1 = y0 - height;
		 */
		String cmd = "--project "+projectDir +
				" --cropbox x0 17.6 y0 117.3 width 85.6 height 79.5 ydown units mm "+
				" --page 1 "+
				" --mediabox x0 0 y0 0 width 600 height 800 ydown units px " +
				" --output svg/crop1.2.svg"
		;
		Norma norma = new Norma();
		norma.run(cmd);
	}
	
	@Test
	@Ignore // NYworking
	public void testGetBoxesByXPath() throws IOException {
		NormaRunner normaRunner = new NormaRunner();

		File projectDir = new File(NormaFixtures.TEST_DEMOS_DIR, "lancet");
		File targetDir = new File("target/demos/lancet/");
		CMineTestFixtures.cleanAndCopyDir(projectDir, targetDir);
		normaRunner.convertRawPDFToProjectToSVG(targetDir);
		
		RegionFinder regionFinder = new RegionFinder();
		 // lancet
		regionFinder.setOutputDir("target/clipping/lancet/");
		regionFinder.setRegionPathFill(LANCET_BOX_FILL);
		File[] ctreeDirectories = targetDir.listFiles();
		Assert.assertEquals(3,  ctreeDirectories.length);
		for (File ctreeDirectory : ctreeDirectories) {
			regionFinder.findRegions(ctreeDirectory);
		}
		
	}
	
	@Test
	public void testTypefaces() throws IOException {
		NormaRunner normaRunner = new NormaRunner();
		File projectDir = new File(NormaFixtures.TEST_DEMOS_DIR, "cert");
		File targetDir = new File("target/demos/cert/");
		/**
		CMineTestFixtures.cleanAndCopyDir(projectDir, targetDir);
		normaRunner.convertRawPDFToProjectToCompactSVG(targetDir);
		*/
		File ctreeDir; String cmd;

		CMineGlobber globber = new CMineGlobber();
		globber.setRegex(".*/fulltext-page.*compact.svg");
		globber.setLocation(targetDir.toString());
		List<File> textFiles = globber.listFiles();
		List<SVGText> svgTexts = SVGText.readAndCreateTexts(textFiles);
		Assert.assertEquals(8538, svgTexts.size());
		StyleRecordFactory styleRecordFactory = new StyleRecordFactory();
		StyleRecordSet styleRecordSet = styleRecordFactory.createStyleRecordSet(svgTexts);
		TypefaceMaps typefaceSet = styleRecordSet.extractTypefaceMaps("cert");
		Assert.assertEquals(29, typefaceSet.size());
		LOG.debug(typefaceSet);

//		ctreeDir = new File(targetDir, "Timmermans_etal_2016_B_Cell_Crohns");
//		cmd = "--ctree "+ctreeDir +
//			" --cropbox x0 32.0 y0 728.0 x1 578 y1 274 yup " + " --pageNumbers 3 "+" --output " + "tables/table1/table.svg";
//		new Norma().run(cmd);
//		
//		ctreeDir = new File(targetDir, "Varga2001");
//		cmd = "--ctree "+ctreeDir +
//			" --cropbox x0 70.0 y0 62.0 x1 460 y1 252 "+" --pageNumbers 3 "+" --output " + "tables/table1/table.svg";
//		new Norma().run(cmd);
//		
//		cmd = "--ctree "+ctreeDir +
//			" --cropbox x0 268 y0 481 x1 514 y1 255 yup "+" --pageNumbers 7 "+" --output " + "maths/maths1/maths.svg";
//		new Norma().run(cmd);
	}


}
