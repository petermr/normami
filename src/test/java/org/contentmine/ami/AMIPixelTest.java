package org.contentmine.ami;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

/** test cleaning.
 * 
 * @author pm286
 *
 */
public class AMIPixelTest {
	private static final Logger LOG = Logger.getLogger(AMIPixelTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testPixelForestPlotsSmallTree() throws Exception {
		String[] args = {
//				"-t", "/Users/pm286/workspace/uclforest/forestplotssmall/buzick",
				"-t", "/Users/pm286/workspace/uclforest/forestplotssmall/campbell",
//				"-p", "/Users/pm286/workspace/uclforest/forestplotssmall",
				"--maxislands", "1000",
				"--minimumx", "50",
				"--minimumy", "50",
				};
		new AMIPixel().runCommands(args);
	}
	
	@Test
	public void testPixelForestPlotsSmallProject() throws Exception {
		String[] args = {
				"-p", "/Users/pm286/workspace/uclforest/forestplotssmall",
				"--maxislands", "50",
				"--minimumx", "50",
				"--minimumy", "50",
				};
		new AMIPixel().runCommands(args);
	}
	



}
