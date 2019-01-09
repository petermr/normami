package org.contentmine.ami.tools;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
	public void testScale() throws Exception {
		String args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --scalefactor 0.5"
				+ " --maxwidth 100"
				+ " --maxheight 100"
						
				+ " --basename scale0_5";
		new AMIBitmapTool().runCommands(args);
	}

	@Test
	/** 
	 * rotate by multiples of 90 degrees
	 */
	public void testRotate() throws Exception {
		String args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --rotate 90"
				+ " --basename rot90";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --rotate 180"
				+ " --basename rot180";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --rotate 270"
				+ " --basename rot270";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --rotate 0"
				+ " --basename rot0";
		new AMIBitmapTool().runCommands(args);
	}

	@Test
	/** 
		imageProcessor.setThreshold(180);
		This is a poor subpixel image which need thresholding and sharpening
	 */
	public void testThreshold() throws Exception {
		String args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --basename noop";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 160"
				+ " --basename thresh160";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 20"
				+ " --basename thresh20"
				;
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 30"
				+ " --basename thresh30"
				;
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 35"
				+ " --basename thresh35"
				;
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 40"
				+ " --basename thresh40"
				;
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 200"
//				+ " --thinning none"
//				+ " --binarize min_max"
				+ " --basename threshold200";
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 220"
//				+ " --thinning none"
//				+ " --binarize min_max"
				+ " --basename threshold220";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 225"
//				+ " --thinning none"
//				+ " --binarize min_max"
				+ " --basename threshold225";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 230"
//				+ " --thinning none"
//				+ " --binarize min_max"
				+ " --basename threshold230";
		new AMIBitmapTool().runCommands(args);
		args = 
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --threshold 240"
//				+ " --thinning none"
//				+ " --binarize min_max"
				+ " --basename threshold240";
		new AMIBitmapTool().runCommands(args);
	}
	
	@Test
	/** 
	 */
	public void testBitmapForestPlotsSmall() throws Exception {
		String args = 
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --threshold 180"
//				+ " --thinning none"
				+ " --binarize entropy"
				;
		new AMIBitmapTool().runCommands(args);
	}
	
//	@Test
//	/** 
//	 */
//	public void testSharpen() throws Exception {
//		String[] args = {
//				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
//				+ " --sharpen laplacian"
//				};
//		new AMIBitmapTool().runCommands(args);
//	}
	
	@Test
	/** 
	 */
	public void testSharpen() throws Exception {
		String args =
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --sharpen sharpen4"
				+ " --basename sharpen4"
				;
		new AMIBitmapTool().runCommands(args);
		args =
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --sharpen sharpen8"
				+ " --basename sharpen8"
				;
		new AMIBitmapTool().runCommands(args);
		args =
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --sharpen laplacian"
				+ " --basename laplacian"
				;
		new AMIBitmapTool().runCommands(args);
	}

	@Test
	/** 
	 */
	public void testSharpenBoofcv() throws Exception {
		String args =
				"-t /Users/pm286/workspace/uclforest/forestplotssmall/cole"
				+ " --sharpen sharpen4"
				+ " --basename sharpen4mean"
//				+ " --binarize local_mean"
				;
		new AMIBitmapTool().runCommands(args);
		args =
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --sharpen laplacian"
				+ " --basename laplacian"
				;
		new AMIBitmapTool().runCommands(args);
	}
	
	@Test
	/** 
	 */
	public void testSharpenThreshold1() throws Exception {
		String args =
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --sharpen sharpen8"
				+ " --basename sharpen8otsu"
				+ " --binarize block_otsu"

				;
		new AMIBitmapTool().runCommands(args);
		args =
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --sharpen laplacian"
				+ " --basename laplacian180"
				+ " --threshold 180"
				;
		new AMIBitmapTool().runCommands(args);
	}
	


	



}
