package org.contentmine.ami.tools;

import java.io.File;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.gocr.LevenshteinDistanceAligment;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.junit.Test;

/** test cleaning.
 * 
 * @author pm286
 *
 */
public class AMIForestPlotTest {
	private static final Logger LOG = Logger.getLogger(AMIForestPlotTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	private static final File FOREST_PLOT_DIR = new File("/Users/pm286/projects/forestplots");
	private static final File STATA_FOREST_PLOT_DIR = new File(FOREST_PLOT_DIR, "stataforestplots");
	private final static String SPSS = "spss";
	private static final File SPSS_DIR = new File(FOREST_PLOT_DIR, SPSS);
	private final static String SPSS_SIMPLE = "spssSimple";
	private static final File SPSS_SIMPLE_DIR = new File(FOREST_PLOT_DIR, SPSS_SIMPLE);
	private final static String SPSS_MULTIPLE = "spssMultiple";
	private static final File SPSS_MULTIPLE_DIR = new File(FOREST_PLOT_DIR, SPSS_MULTIPLE);
	private final static String SPSS_SUBPLOT = "spssSubplot";
	private static final File SPSS_SUBPLOT_DIR = new File(FOREST_PLOT_DIR, SPSS_SUBPLOT);
	private final static String STATA = "stata";
	private static final File STATA_DIR = new File(STATA_FOREST_PLOT_DIR, STATA);
	private final static String STATA_TOTAL = "stataTotal";
	private static final File STATA_TOTAL_DIR = new File(STATA_FOREST_PLOT_DIR, STATA_TOTAL);
	private final static String STATA_TOTAL_EDITED = "stataTotalEdited";
	private static final File STATA_TOTAL_EDITED_DIR = new File(STATA_FOREST_PLOT_DIR, STATA_TOTAL_EDITED);
	public static final String DEVTEST = SPSS_DIR.toString();

	@Test
	public void testHelp() {
		new AMIForestPlotTool().runCommands("--help");
	}
	
	@Test
	/** 
	 */
	public void testFilterTrees() throws Exception {
		String args = 
//				"-t "+DEVTEST+"bowmann-perrottetal_2013"
//				"-t "+DEVTEST+"buzick_stone_2014_readalo"
//				"-t "+DEVTEST+"campbell_systematic_revie"
				"-t "+DEVTEST+"PMC5502154"
//				"-t "+DEVTEST+"mcarthur_etal2012_cochran"
//				"-t "+DEVTEST+"puziocolby2013_co-operati"
//				"-t "+DEVTEST+"torgersonetal_2011dferepo"
//				"-t "+DEVTEST+"zhengetal_2016"
//				+ " --sharpen sharpen4"
				+ " --threshold 180"
				+ " --binarize BLOCK_OTSU"
//				+ " --rotate 270"
				+ " --priority SCALE"
				;
		new AMIForestPlotTool().runCommands(args);
	}


	@Test
	/** 
	 * convert single tree
	 */
	public void testBuzick() throws Exception {
		String[] args = {
				"-t", ""+DEVTEST+"PMC6417514"
			};
		new AMIForestPlotTool().runCommands(args);
	}
	
	@Test
	/** 
	 * convert single tree
	 */
	public void testDonkerPlus() throws Exception {
		String args =
		"-p "+DEVTEST+""
		+ " --includetree"
		+ " donkerdeboerkostons2014_l"
		+ " ergen_canagli_17_"
		+ " fanetal_2017_meta_science"
		+ ""
		+ " --sharpen sharpen4"
		+ " --threshold 180"
//		+ " --binarize GLOBAL_ENTROPY"
		+ " --priority SCALE"
		;
//		new AMIImageTool().runCommands(args);

		args = 	"-p "+DEVTEST+""
				+ " --includetree"
				+ " donkerdeboerkostons2014_l"
				+ " ergen_canagli_17_"
				+ " fanetal_2017_meta_science"
			;
		new AMIForestPlotTool().runCommands(args);
	}
	
	
	@Test
	/** scale small text
	 * 
	 * @throws Exception
	 */
	public void testScaleOCRProject() throws Exception {
		String args = ""
				+ "-p "+DEVTEST+""
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
		new AMIForestPlotTool().runCommands(args);
	}
	

	@Test
	public void testSPSSTable() {
		String plotType = SPSS;
		boolean useTree = true;
//		useTree = false;
		String treename = "PMC5502154";
//		extractPlots(plotType, treename, useTree);		
		analyzePlots(plotType, treename, useTree);		
	}

	@Test
	public void testStataTable() {
		String plotType = STATA;
		String treename = "PMC5502154";
		boolean useTree = false;
		extractPlots(plotType, treename, useTree);
		
	}
	
	@Test 
	/** splits components of SPSS ForestPlots
	 * 
	 */
	public void testSPSSSplit() {
		

		boolean useTree = true;
		File projectDir = SPSS_DIR;
		String treename = "PMC5502154";
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		AMIForestPlotTool forestPlotTool = new AMIForestPlotTool();
		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source
			+ " --split x"
//			+ " --color 0x0"
			+ " --offset -10"
			+ " --minline 300"
		    + "";
		forestPlotTool.runCommands(cmd);

	}

	@Test 
	/** creates columns for SPSS Tables
	 * 
	 */
	public void testSPSSTableBBoxes() {
		
//		boolean useTree = true;
		boolean useTree = false;
//		File projectDir = SPSS_DIR;
		File projectDir = SPSS_SIMPLE_DIR;
//		File projectDir - SPSS_MULTIPLE_DIR;
//		File projectDir - SPSS_SUBPLOT_DIR;
		String treename = "PMC5502154";
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		AMIForestPlotTool forestPlotTool = new AMIForestPlotTool();
		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source
			+ " --table"
		    + "";
		System.out.println("ami-forest "+cmd);
		forestPlotTool.runCommands(cmd);

	}

	@Test 
	/** splits SPSS Plots vertically into Table and Graph
	 * 
	 */
	public void testSplitSPSSSimpleTableGraph() {
		

		boolean useTree = false;
//		File projectDir = SPSS_DIR;
		File projectDir = SPSS_SIMPLE_DIR;
//		File projectDir - SPSS_MULTIPLE_DIR;
//		File projectDir - SPSS_SUBPLOT_DIR;
//		String treename = "PMC5502154";
		String treename = "PMC5911624";
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		AMIForestPlotTool forestPlotTool = new AMIForestPlotTool();
		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source
			+ " --template template.xml"
		    + "";
		System.out.println("ami-forest "+cmd);
		forestPlotTool.runCommands(cmd);

	}

	@Test
	public void testSplitSPSSMultipleTableGraph() {
		
		File projectDir = SPSS_MULTIPLE_DIR;
		CProject cProject = new CProject(projectDir);
		AMIForestPlotTool forestPlotTool = new AMIForestPlotTool();
		String source = "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source
			+ " --template template.xml"
		    + "";
		System.out.println("ami-forest "+cmd);
		forestPlotTool.runCommands(cmd);

	}

	@Test
	public void testSplitSPSSSubplotTableGraph() {
		
		File projectDir = SPSS_SUBPLOT_DIR;
		CProject cProject = new CProject(projectDir);
		AMIForestPlotTool forestPlotTool = new AMIForestPlotTool();
		String source = "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source
			+ " --template template.xml"
		    + "";
		System.out.println("ami-forest "+cmd);
		forestPlotTool.runCommands(cmd);

	}

	@Test
	public void testSPSSImageProcessing() {
		
		boolean useTree = true;
		File projectDir = SPSS_DIR;
		String treename = "PMC5502154";
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		AMIImageTool imageTool = new AMIImageTool();
		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source
			+ " --sharpen sharpen4"
			+ " --threshold 150"
			+ " --scalefactor 2.0";
		imageTool.runCommands(cmd);
		AMIForestPlotTool forestPlotTool = new AMIForestPlotTool();
		cmd = ""
			+ source
			+ " --table";
		
		forestPlotTool.runCommands(cmd);
	}

	/** assumes CProject structure but no subdirectories 
	 * */
	@Test
	public void testTotalStata() {
		
		boolean useTree = 
//				true
				false
				;
//		File projectDir = STATA_TOTAL_DIR;
		File projectDir = STATA_TOTAL_EDITED_DIR;
		String treename = "PMC5882397";
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		
		boolean makeProject = 
//				true
				false
				;
		boolean makePdf = 
//				true
				false
				;
		boolean makeImage = 
				false
//				true
				;
		boolean makeOCR = 
				false
//				true
				;
		boolean makeHOCR = 
				false
//				true
				;
		boolean makeGOCR = 
				false
//				true
				;
		boolean extractLines = true;
		boolean makePixel = 
//				false
				true
				;
		boolean makeForest = 
//				false
				true
				;
		
		/** make the CTrees -no-op if already present 
		 * from commandline:
		 *  ami-makeproject -p /Users/pm286/my/project  --rawfiletypes html,pdf,xml";
		 */
		String makeProjectCmd = " -p " + cProject.getDirectory().getAbsoluteFile() + " --rawfiletypes html,pdf,xml" + " --omit template\\.xml log\\.txt"
				;
		if (makeProject) new AMIMakeProjectTool().runCommands(makeProjectCmd);

		
		// =====PDF======
		/** parse PDFs and extract images // this will contain non-forest images
		 * from commandline:
		 *  ami-pdf -p /Users/pm286/my/project ";
		 */
		String pdfCmd = " -p " + cProject.getDirectory().getAbsoluteFile()  ;
		if (makePdf) new AMIPDFTool().runCommands(pdfCmd);
		
		// =====Image======
		/** enhance images by thresholding and sharpening.
		 * from commandline:
		 *  ami-pdf -p /Users/pm286/my/project --sharpen sharpen4 --threshold 150 despeckle true";
		 * 
		 */

		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String imageCmd = ""
				+ source
				+ " --sharpen sharpen4"
				+ " --threshold 120"
				+ " --despeckle true"
				;
		if (makeImage) new AMIImageTool().runCommands(imageCmd);
		imageCmd = ""
				+ source
				+ " --threshold 120"
				+ " --despeckle true"
				;
		if (makeImage) new AMIImageTool().runCommands(imageCmd);
		imageCmd = ""
				+ source
				+ " --sharpen sharpen4"
				+ " --threshold 200"
				+ " --despeckle true"
				;
		if (makeImage) new AMIImageTool().runCommands(imageCmd);
		imageCmd = ""
				+ source
				+ " --threshold 200"
				+ " --despeckle true"
				+ " -vvv"
				;
		if (makeImage) new AMIImageTool().runCommands(imageCmd);

		imageCmd = ""
				+ source
				+ " --threshold 240"
				+ " --despeckle true"
				+ " -vvv"
				;
		if (makeImage) new AMIImageTool().runCommands(imageCmd);

		// =====OCR======
		/** Optical Character Recognition OCR.
		 * from commandline:
		 *  ami-ocr -p /Users/pm286/my/project " --gocr /usr/local/bin/gocr"
		 * 
		 */
		String ocrCmd = ""
				+ source
				+ " --tesseract /usr/local/bin/tesseract"
				+ " --extractlines hocr"
//				+ " --html false"
		;
		if (makeHOCR) new AMIOCRTool().runCommands(ocrCmd);
		ocrCmd = ""
				+ source
				+ " --gocr /usr/local/bin/gocr"
				+ " --extractlines gocr"
		;
		if (makeGOCR) new AMIOCRTool().runCommands(ocrCmd);

		// =====Pixel get subimage dimensions ======
		String pixelCmd = ""
			+ source
			+ " --projections"
			+ " --yprojection 0.8"
			+ " --xprojection 0.5"
			+ " --minheight -1"
			+ " --rings -1"
			+ " --islands 0"
			+ " --inputname raw_thr_230_ds"
			+ " --subimage statascale y 2 delta 10 projection x"
			+ " --templateinput raw_thr_230_ds/projections.xml"
			+ " --templateoutput template.xml"
			+ " --templatexsl /org/contentmine/ami/tools/stataTemplate.xsl"
			;
		if (makePixel) new AMIPixelTool().runCommands(pixelCmd);

		// =====Forest======

		if (makeForest) new AMIForestPlotTool().runCommands(source + " --template template.xml");


	}
	
	
	@Test
	public void testTemplateXML() {
		File projectDir = STATA_TOTAL_EDITED_DIR;
		CProject cProject = new CProject(projectDir);
		String treename = "PMC5882397";
		CTree cTree = new CTree(new File(projectDir, treename));
//		String source = "-t "+cTree.getDirectory();
		String source = "-p "+cProject.getDirectory();

		// first make template.xml
		String pixelCmd = ""
			+ source
			+ " --projections"
			+ " --yprojection 0.8"
			+ " --xprojection 0.5"
			+ " --minheight -1"
			+ " --rings -1"
			+ " --islands 0"
			+ " --inputname raw_thr_230_ds"
			+ " --subimage statascale y 2 delta 10 projection x"
			+ " --templateinput raw_thr_230_ds/projections.xml"
			+ " --templateoutput template.xml"
			+ " --templatexsl /org/contentmine/ami/tools/stataTemplate.xsl"
			;
		new AMIPixelTool().runCommands(pixelCmd);
		// now use it to section images
		String forestCmd = source + " --segment --template raw_thr_230_ds/template.xml"
				;
		new AMIForestPlotTool().runCommands(forestCmd);
	}
	
	@Test
	public void testHOCROnSplitRegions() {
		File projectDir = STATA_TOTAL_EDITED_DIR;
		CProject cProject = new CProject(projectDir);
		String treename = "PMC5882397";
		CTree cTree = new CTree(new File(projectDir, treename));
		String source = "-t "+cTree.getDirectory();
//		String source = "-p "+cProject.getDirectory();

//		String imageCmd = source + " --inputname raw.body.ltable --sharpen sharpen4 --threshold 120 --despeckle true";
//		new AMIImageTool().runCommands(imageCmd);

		new AMIOCRTool().runCommands(source + " --inputname raw.body.ltable --tesseract /usr/local/bin/tesseract --extractlines hocr");
		new AMIOCRTool().runCommands(source + " --inputname raw.body.rtable --tesseract /usr/local/bin/tesseract --extractlines hocr");
		new AMIOCRTool().runCommands(source + " --inputname raw.header --tesseract /usr/local/bin/tesseract --extractlines hocr");
		new AMIOCRTool().runCommands(source + " --inputname raw.scale --tesseract /usr/local/bin/tesseract --extractlines hocr");
	}
	
	@Test
	public void testHOCRandGOCR() {
		File projectDir = STATA_TOTAL_EDITED_DIR;
		CProject cProject = new CProject(projectDir);
		String treename = "PMC5882397";
		CTree cTree = new CTree(new File(projectDir, treename));
//		String source = "-t "+cTree.getDirectory();
		String source = "-p "+cProject.getDirectory();
		String FORCEMAKE = " --forcemake";
		FORCEMAKE = "";
		// sharpen subimages

		new AMIImageTool().runCommands(source + " --inputname raw.header --sharpen sharpen4 --threshold 120 --despeckle true");
		new AMIImageTool().runCommands(source + " --inputname raw.body.ltable --sharpen sharpen4 --threshold 120 --despeckle true");
		new AMIImageTool().runCommands(source + " --inputname raw.body.rtable --sharpen sharpen4 --threshold 120 --despeckle true");		

 		new AMIOCRTool().runCommands(source + " --inputname raw.header_s4_thr_120_ds --tesseract /usr/local/bin/tesseract --extractlines hocr" + FORCEMAKE);
		new AMIOCRTool().runCommands(source + " --inputname raw.body.ltable_s4_thr_120_ds --tesseract /usr/local/bin/tesseract --extractlines hocr" + FORCEMAKE);
		new AMIOCRTool().runCommands(source + " --inputname raw.body.rtable_s4_thr_120_ds --tesseract /usr/local/bin/tesseract --extractlines hocr" + FORCEMAKE);

		new AMIOCRTool().runCommands(source + " --inputname raw.header_s4_thr_120_ds --gocr /usr/local/bin/gocr --extractlines gocr" + FORCEMAKE);
		new AMIOCRTool().runCommands(source + " --inputname raw.body.ltable_s4_thr_120_ds --gocr /usr/local/bin/gocr --extractlines gocr" + FORCEMAKE);
		new AMIOCRTool().runCommands(source + " --inputname raw.body.rtable_s4_thr_120_ds --gocr /usr/local/bin/gocr --extractlines gocr" + FORCEMAKE
				+ " --replace"
				+ " o 0 O 0 d 0"
				+ " e 2"
				+ " q 4 A 4"
				+ " s 5 S 5 $ 5"
				+ " d 6 G 6"
				+ " J 7"
				+ " T 7"
				+ " Z 2"
				// ambiguous?
//				+ " a 0 a 4"
				+ " a 4"
				);
		
	}
	
	@Test
	/** requires some tests to be run previously (UGH)
	 * 
	 */
	public void testDisplay() {
		File projectDir = STATA_TOTAL_EDITED_DIR;
		CProject cProject = new CProject(projectDir);
		String treename = "PMC5882397";
		CTree cTree = new CTree(new File(projectDir, treename));
//			String source = "-t "+cTree.getDirectory();
		String source = "-p "+cProject.getDirectory();
		new AMIForestPlotTool().runCommands(source + ""
				+ " --inputname raw.body.rtable_s4_thr_120_ds"
				+ " --display .png hocr/hocr.svg gocr/gocr.svg "
				+ " --summary raw.body.rtable_s4_thr_120_ds.html"
// doesn't work - use width and height, then it might
//				+ " --header raw.header_s4_thr_120_ds.png"
//				+ " --template raw_thr_230_ds/template.xml"
				);
		
	}

	
	@Test
	public void testAlign() {
		align("abcdef", "abxdef");
		align("abcdef", "abdef");
		align("abdef", "abcdef");
		align("abcdef", "abcxdef");
		align("123abcdef", "123abxdcef");
		align("123456789", "123a456b789");
		align("123456789", "123a46b789");
	}
	
	@Test
	public void testSPSSSimple() {
		File projectDir = SPSS_SIMPLE_DIR;
		CProject cProject = new CProject(projectDir);
		String treename = "PMC5502154";
		CTree cTree = new CTree(new File(projectDir, treename));
//		String source = "-t "+cTree.getDirectory();
		String source = "-p "+cProject.getDirectory();
		String FORCEMAKE = " --forcemake";
		new AMIPDFTool().runCommands(source);
		new AMIImageTool().runCommands(source + " --sharpen sharpen4" + " --threshold 120" + " --despeckle true");
		new AMIImageTool().runCommands(source + " --sharpen sharpen4" + " --threshold 150" + " --despeckle true");
		new AMIImageTool().runCommands(source + " --sharpen sharpen4" + " --threshold 180" + " --despeckle true");
		// display what we have got
		new AMIForestPlotTool().runCommands(source + ""
//				+ " --inputname raw.body.rtable_s4_thr_120_ds"
				+ " --display "
				+ " raw_s4_thr_120_ds.png"
				+ " raw_s4_thr_150_ds.png"
				+ " raw_s4_thr_180_ds.png"
				+ " --orientation vertical"
				+ " --summary raw_s4_120_150_180_ds.html");

		new AMIPixelTool().runCommands(source
			+ " --projections --yprojection 0.4 --xprojection 0.7 --lines"
			+ " --minheight -1 --rings -1 --islands 0"
			+ " --inputname raw_s4_thr_150_ds"
			+ " --templateinput raw_s4_thr_150_ds/projections.xml"
			+ " --templateoutput template.xml"
			+ " --templatexsl /org/contentmine/ami/tools/spssTemplate.xsl");

		new AMIForestPlotTool().runCommands(source + " --segment --template raw_s4_thr_150_ds/template.xml");
		new AMIImageTool().runCommands(source + " --inputname raw.header  --sharpen sharpen4" + " --threshold 150" + " --despeckle true");
		new AMIImageTool().runCommands(source + " --inputname raw.body --sharpen sharpen4" + " --threshold 150" + " --despeckle true");
		
 		new AMIOCRTool().runCommands(source 
 				+ " --inputname raw.header_s4_thr_150_ds --tesseract /usr/local/bin/tesseract --extractlines hocr" + FORCEMAKE);
		new AMIOCRTool().runCommands(source
				+ " --inputname raw.body_s4_thr_150_ds --tesseract /usr/local/bin/tesseract --extractlines hocr" + FORCEMAKE);
		
 		new AMIOCRTool().runCommands(source 
 				+ " --inputname raw.header_s4_thr_150_ds --gocr /usr/local/bin/gocr --extractlines gocr" + FORCEMAKE);
		new AMIOCRTool().runCommands(source
				+ " --inputname raw.body_s4_thr_150_ds --gocr /usr/local/bin/gocr --extractlines gocr" + FORCEMAKE + " --table");

		new AMIForestPlotTool().runCommands(source + ""
				+ " --inputname raw.body_s4_thr_150_ds"
				+ " --display ../raw.body.png .png hocr/hocr.svg gocr/gocr.svg"
				+ " --orientation vertical"
				+ " --summary raw.body_s4_thr_150_ds.html");

		new AMIForestPlotTool().runCommands(source + ""
				+ " --inputname raw.header_s4_thr_150_ds"
				+ " --display ../raw.header.png .png hocr/hocr.svg gocr/gocr.svg"
				+ " --orientation vertical"
				+ " --summary raw.header_s4_thr_150_ds.html");


	}

	// ========================================

	private void align(String s1, String s2) {
		LevenshteinDistanceAligment<Character> align01 =
                LevenshteinDistanceAligment.createAlignment(s1, s2);
		System.out.println();
        System.out.println("Levenshtein distance = " + align01.getAlignment());
        System.out.println("Levenshtein distance = " + align01.getDistance());
        System.out.println("           " + s1);
        System.out.println("Alignment: " + align01.getAlignmentString());
        System.out.println("           " + s2);
	}

	
	private void extractPlots(String plotType, String treename, boolean useTree) {
		File projectDir = STATA.equals(plotType) ? STATA_DIR : SPSS_DIR;
		
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		AbstractAMITool ocrTool = new AMIOCRTool();

		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source 
			+ " --extractlines gocr"
			+ "";
		// create SVG
		
		ocrTool.runCommands(cmd);
	}
	
	private void analyzePlots(String plotType, String treename, boolean useTree) {
		File projectDir = STATA.equals(plotType) ? STATA_DIR : SPSS_DIR;
		
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		AMIForestPlotTool forestPlotTool = new AMIForestPlotTool();
		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source
//		    + " --plottype " + plotType
//		    + " --hocr=true"
		    + "";
		forestPlotTool.runCommands(cmd);
	}
	


}
