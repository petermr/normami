package org.contentmine.ami.tool;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMIOCRTool;
import org.junit.Test;

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
		String[] args = {
				"-t", "/Users/pm286/workspace/uclforest/dev/buzick",
				"--html", "true"
			};
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
	
}
