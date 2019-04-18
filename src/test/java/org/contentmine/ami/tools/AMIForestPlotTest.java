package org.contentmine.ami.tools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.image.ImageUtil.ThresholdMethod;
import org.junit.Test;

import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.struct.ConfigLength;

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
	public static final String DEVTEST = "/Users/pm286/projects/forestplots/spss/";

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
	


	



}
