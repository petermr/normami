package org.contentmine.ami;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

/** test cleaning.
 * 
 * @author pm286
 *
 */
public class AMIImageTest {
	private static final Logger LOG = Logger.getLogger(AMIImageTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** 
	 * 
	 */
	public void testImageForestPlotsSmall() throws Exception {
		String[] args = {
				"-p", "/Users/pm286/workspace/uclforest/forestplotssmall",
				"--monochrome", "true",
				"--monochromedir", "monochrome",
				"--minwidth", "100",
				"--minheight", "100",
				"--smalldir", "small",
				"--duplicates", "true", 
				"--duplicatedir", "duplicates",
				};
		AMIImage amiImage = new AMIImage();
		amiImage.runCommands(args);
	}




}
