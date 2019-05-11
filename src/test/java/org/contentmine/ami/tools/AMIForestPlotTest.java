package org.contentmine.ami.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.eucl.euclid.Axis.Axis2;
import org.contentmine.image.ImageLineAnalyzer;
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
	private final static String SPSS = "spss";
	private static final File SPSS_DIR = new File(FOREST_PLOT_DIR, SPSS);
	private final static String SPSS_SIMPLE = "spssSimple";
	private static final File SPSS_SIMPLE_DIR = new File(FOREST_PLOT_DIR, SPSS_SIMPLE);
	private final static String SPSS_MULTIPLE = "spssMultiple";
	private static final File SPSS_MULTIPLE_DIR = new File(FOREST_PLOT_DIR, SPSS_MULTIPLE);
	private final static String SPSS_SUBPLOT = "spssSubplot";
	private static final File SPSS_SUBPLOT_DIR = new File(FOREST_PLOT_DIR, SPSS_SUBPLOT);
	private final static String STATA = "stata";
	private static final File STATA_DIR = new File(FOREST_PLOT_DIR, STATA);
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



	// ========================================
	
	private void extractPlots(String plotType, String treename, boolean useTree) {
		File projectDir = STATA.equals(plotType) ? STATA_DIR : SPSS_DIR;
		
		CTree cTree = new CTree(new File(projectDir, treename));
		CProject cProject = new CProject(projectDir);
		AMIOCRTool ocrTool = new AMIOCRTool();

		String source = useTree ? "--ctree "+cTree.getDirectory() : "--cproject "+cProject.getDirectory();
		String cmd = ""
			+ source 
			+ " --extractlines horiz"
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
