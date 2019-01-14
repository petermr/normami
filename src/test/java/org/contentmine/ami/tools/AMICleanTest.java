package org.contentmine.ami.tools;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.tools.AMICleanTool;
import org.contentmine.cproject.files.CProject;
import org.junit.Test;

import junit.framework.Assert;

/** test cleaning.
 * 
 * @author pm286
 *
 */
public class AMICleanTest {
	private static final Logger LOG = Logger.getLogger(AMICleanTest.class);
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
		AMICleanTool amiCleaner = new AMICleanTool();
		amiCleaner.runCommands(args);
		CProject cProject = amiCleaner.getCProject();
		Assert.assertNotNull("CProject not null", cProject);
	}

	@Test
	/**
	 * tests cleaning directories in a single CTree.
	 */
	public void testCleanSingleTree() {
//		ami-clean -t /Users/pm286/workspace/uclforest/dev/higgins --dir pdfimages
		String cmd = "-t /Users/pm286/workspace/uclforest/dev/higgins --dir pdfimages";
		new AMICleanTool().runCommands(cmd);
	}


}
