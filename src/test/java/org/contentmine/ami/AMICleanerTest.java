package org.contentmine.ami;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.junit.Test;

import junit.framework.Assert;

/** test cleaning.
 * 
 * @author pm286
 *
 */
public class AMICleanerTest {
	private static final Logger LOG = Logger.getLogger(AMICleanerTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** 
	 * 
	 */
	public void testCleanForestPlotsSmall() throws Exception {
		String[] args = {
				"-p", "/Users/pm286/workspace/uclforest/forestplotssmall",
				"--dir", "svg/", "pdfimages/",
				"--file", "scholarly.html"
				};
		Assert.assertTrue(new File(args[1]).exists());
		AMICleaner amiCleaner = new AMICleaner();
		amiCleaner.runCommands(args);
		CProject cProject = amiCleaner.getCProject();
		Assert.assertNotNull("CProject not null", cProject);
	}



}
