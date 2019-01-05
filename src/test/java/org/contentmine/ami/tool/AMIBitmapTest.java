package org.contentmine.ami.tool;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIBitmapTool;
import org.contentmine.ami.tools.AMIImageTool;
import org.contentmine.ami.tools.AMIPixelTool;
import org.contentmine.ami.tools.AbstractAMITool;
import org.junit.Test;

/** test cleaning.
 * 
 * @author pm286
 *
 */
public class AMIBitmapTest {
	private static final Logger LOG = Logger.getLogger(AMIBitmapTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** 
		imageProcessor.setThreshold(180);
		This is a poor subpixel image which need thresholding and sharpening
	 */
	public void testBitmapParameters() throws Exception {
		String args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 160"
//				+ " --thinning none"
				+ " --binarize true"
				+ " --basename threshold160";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 180"
//				+ " --thinning none"
				+ " --binarize true"
				+ " --basename threshold180"
				;
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 200"
//				+ " --thinning none"
				+ " --binarize true"
				+ " --basename threshold200";
		new AMIBitmapTool().runCommands(args);
	}
	
	@Test
	/** 
	 */
	public void testBitmapForestPlotsSmall() throws Exception {
		String[] args = {
				"-p", "/Users/pm286/workspace/uclforest/forestplotssmall",
				"--threshold", "180",
				"--thinning", "none",
				"--binarize", "true",
				};
		new AMIBitmapTool().runCommands(args);
	}
	
	@Test
	/** 
	 * 
	 */
	public void testBuzick() throws Exception {
//		IslandRingList ringList;
		String ctree = "/Users/pm286/workspace/uclforest/dev/buzick";
		new AMIImageTool().runCommands(" --ctree " + ctree);
		new AMIBitmapTool().runCommands(" --ctree " + ctree);
		AbstractAMITool amiPixelTool = new AMIPixelTool();
		amiPixelTool.runCommands(" --ctree " + ctree
				// these are not working well yet 
				+ " --minwidth 350"
				+ " --minheight 10"
				+ " --maxislands 2000"
//				+ " --outputDirname pixels"
				);
		
	}


	



}
