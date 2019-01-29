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

	@Test
	public void testHelp() {
		new AMIForestPlotTool().runCommands("--help");
	}
	
	@Test
	/** 
	 */
	public void testFilterTrees() throws Exception {
		String args = 
//				"-t /Users/pm286/workspace/uclforest/devtest/bowmann-perrottetal_2013"
//				"-t /Users/pm286/workspace/uclforest/devtest/buzick_stone_2014_readalo"
//				"-t /Users/pm286/workspace/uclforest/devtest/campbell_systematic_revie"
				"-t /Users/pm286/workspace/uclforest/devtest/case_systematic_review_ar"
//				"-t /Users/pm286/workspace/uclforest/devtest/mcarthur_etal2012_cochran"
//				"-t /Users/pm286/workspace/uclforest/devtest/puziocolby2013_co-operati"
//				"-t /Users/pm286/workspace/uclforest/devtest/torgersonetal_2011dferepo"
//				"-t /Users/pm286/workspace/uclforest/devtest/zhengetal_2016"
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
				"-t", "/Users/pm286/workspace/uclforest/devtest/buzick_stone_2014_readalo"
			};
		new AMIForestPlotTool().runCommands(args);
	}
	
	@Test
	/** 
	 * convert single tree
	 */
	public void testDonkerPlus() throws Exception {
		String args = 
				"-p /Users/pm286/workspace/uclforest/devtest/"
				+ " --includetree"
//				+ " donkerdeboerkostons2014_l"
//				+ " ergen_canagli_17_"
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
		new AMIForestPlotTool().runCommands(args);
	}
	


	



}
