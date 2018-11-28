package org.contentmine.ami;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMICleaner.Cleaner;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.DebugPrint;
import org.contentmine.cproject.util.CMineGlobber;
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
	public void testCleanBMC() throws IOException {
		File destDir = new File(AMIFixtures.TARGET_DIR, "bmc/");
		FileUtils.copyDirectory(AMIFixtures.TEST_BMC_DIR, destDir);
		CProject cProject = new CProject(destDir);
		AMICleaner cleaner = new AMICleaner(cProject);
		CMineGlobber globber = new CMineGlobber().setRegex(".*/scholarly\\.html").setLocation(destDir);
		List<File> fileList = globber.listFiles();
		Assert.assertEquals("files: "+fileList, 3,  fileList.size());
		cleaner.cleanReserved("junk");
		fileList = globber.listFiles();
		Assert.assertEquals("files: "+fileList, 3,  fileList.size());
		cleaner.cleanReserved(Cleaner.SCHOLARLY_HTML.file);
		fileList = globber.listFiles();
		DebugPrint.debugPrint("files "+fileList);
		Assert.assertEquals(0,  fileList.size());
		
	}
	

	@Test
	/**  this test is ephemeral
	 */
	public void testCleanUCLForest() throws IOException {
		
//		File destDir = new File(AMIFixtures.TARGET_DIR, "uclforest/");
//		FileUtils.copyDirectory(AMIImageProcessorTest.FOREST_PLOT_DIR, destDir);
//		CProject cProject = new CProject(destDir);
//		CTree cTree = cProject.getCTreeByName("buzick");
////		CMineGlobber globber = new CMineGlobber().setGlob("glob:**/svgold").setLocation(cTree.getDirectory());
////		CMineGlobber globber = new CMineGlobber().setGlob("glob:**/svgold/").setLocation(cTree.getDirectory());
//		CMineGlobber globber = new CMineGlobber().setGlob("**/svgold/*").setLocation(cTree.getDirectory());
//		List<File> fileList = globber.listFiles();
//		Assert.assertEquals("files: "+fileList, 14,  fileList.size());
//		globber = new CMineGlobber().setGlob("**/svgold/").setLocation(cTree.getDirectory());
//		fileList = globber.listFiles();
//		Assert.assertEquals("files: "+fileList, 0,  fileList.size());
//
//		AMICleaner cleaner = new AMICleaner(cProject);
//		cleaner.clean("svgold");
//		globber = new CMineGlobber().setGlob("**/svgold/*").setLocation(cTree.getDirectory());
//		fileList = globber.listFiles();
//		Assert.assertEquals("files: "+fileList, 0,  fileList.size());

		CProject forestProject = new CProject(AMIImageProcessorTest.FORESTPLOT_DIR);
		AMICleaner forestCleaner = new AMICleaner(forestProject);
		forestCleaner.clean("image");
		forestCleaner.clean("images");
		forestCleaner.clean("svg");
		forestCleaner.clean("svgold");
		forestCleaner.clean("scholarly.html");
		// *
	}
	

}
