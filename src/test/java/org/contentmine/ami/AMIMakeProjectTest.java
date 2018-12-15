package org.contentmine.ami;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.util.CMineGlobber;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.norma.NormaFixtures;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/** tests AMIMakeProject
 * 
 * @author pm286
 *
 */
public class AMIMakeProjectTest {
	private static final Logger LOG = Logger.getLogger(AMIMakeProjectTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	private static final String PROJECT1 = "project1/";

	@Test 
	@Ignore // just shows help
	public void testMakeProject() throws Exception {
		String cmd = "-h -c 12";
		AMIMakeProject.main(cmd.split("\\s+"));
	}
	

	@Test
	public void testMakeProject1long() throws Exception {
		File targetDir = new File(NormaFixtures.TARGET_MAKEPROJECT_DIR, PROJECT1);
		CMineTestFixtures.cleanAndCopyDir(
				new File(NormaFixtures.TEST_MAKEPROJECT_DIR, PROJECT1),
				targetDir);
		Assert.assertTrue(targetDir.exists());
		String cmd = "-p " + targetDir +" -f html,pdf,xml";
		AMIMakeProject.main(cmd.split("\\s+"));
		List<File> childDirectories = CMineGlobber.listSortedChildDirectories(targetDir);
		Assert.assertEquals("Ergen & Canagli_17'", FilenameUtils.getBaseName(childDirectories.get(0).toString()));
		Assert.assertEquals("[target/makeproject/project1/Ergen & Canagli_17',"
				+ " target/makeproject/project1/JNEUROSCI.4415-13.2014,"
				+ " target/makeproject/project1/multiple-1471-2148-11-312,"
				+ " target/makeproject/project1/multiple-1471-2148-11-313,"
				+ " target/makeproject/project1/multiple.312,"
				+ " target/makeproject/project1/pb1,"
				+ " target/makeproject/project1/tree-1471-2148-11-313]", childDirectories.toString());
		Assert.assertEquals(7, childDirectories.size());
		Assert.assertEquals(3, CMineGlobber.listSortedDescendantFiles(targetDir, CTree.HTML).size());
		Assert.assertEquals(5, CMineGlobber.listSortedDescendantFiles(targetDir, CTree.PDF).size());
	}
	
	@Test
	public void testMakeProject1short() throws Exception {
		File targetDir = new File(NormaFixtures.TARGET_MAKEPROJECT_DIR, PROJECT1);
		CMineTestFixtures.cleanAndCopyDir(
				new File(NormaFixtures.TEST_MAKEPROJECT_DIR, PROJECT1),
				targetDir);
		Assert.assertTrue(targetDir.exists());
		String cmd = "-p " + targetDir +" -f html,pdf,xml -c 12";
		AMIMakeProject.main(cmd.split("\\s+"));
		List<File> childDirectories = CMineGlobber.listSortedChildDirectories(targetDir);
		Assert.assertEquals("ergen_canagl", FilenameUtils.getBaseName(childDirectories.get(0).toString()));
		Assert.assertEquals("[target/makeproject/project1/ergen_canagl,"
				+ " target/makeproject/project1/jneurosci.44,"
				+ " target/makeproject/project1/multiple-147,"
				+ " target/makeproject/project1/multiple-1472,"
				+ " target/makeproject/project1/multiple.312,"
				+ " target/makeproject/project1/pb1,"
				+ " target/makeproject/project1/tree-1471-21]", childDirectories.toString());
		Assert.assertEquals(7, childDirectories.size());
		Assert.assertEquals(3, CMineGlobber.listSortedDescendantFiles(targetDir, CTree.HTML).size());
		Assert.assertEquals(5, CMineGlobber.listSortedDescendantFiles(targetDir, CTree.PDF).size());
	}
	
	@Test
	public void testMakeProjectLog() throws Exception {
		File targetDir = new File(NormaFixtures.TARGET_MAKEPROJECT_DIR, PROJECT1);
		CMineTestFixtures.cleanAndCopyDir(
				new File(NormaFixtures.TEST_MAKEPROJECT_DIR, PROJECT1),
				targetDir);
		Assert.assertTrue(targetDir.exists());
		String cmd = "-p " + targetDir +" -f html,pdf,xml -c 15";
//		String cmd = "-p " + targetDir +" -f html,pdf,xml ";
		AMIMakeProject  amiMakeProject = new AMIMakeProject();
		amiMakeProject.runCommands(cmd.split("\\s+"));
		List<File> childDirectories = CMineGlobber.listSortedChildDirectories(targetDir);
		Assert.assertEquals(7, childDirectories.size());
		Assert.assertEquals("ergen_canagli_1", FilenameUtils.getBaseName(childDirectories.get(0).toString()));
		Assert.assertEquals("["
				+ "target/makeproject/project1/ergen_canagli_1,"
				+ " target/makeproject/project1/jneurosci.4415-,"
				+ " target/makeproject/project1/multiple-1471-2,"
				+ " target/makeproject/project1/multiple-1471-22,"
				+ " target/makeproject/project1/multiple.312,"
				+ " target/makeproject/project1/pb1,"
				+ " target/makeproject/project1/tree-1471-2148-]",
				childDirectories.toString());
		CProject cProject = amiMakeProject.getCProject();
		File logFile = cProject.getMakeProjectLogfile();
		Assert.assertNotNull("nust have logFile", logFile);
		
		Assert.assertTrue(logFile+" exists",logFile.exists());
		Assert.assertEquals(7, childDirectories.size());
		Assert.assertEquals(3, CMineGlobber.listSortedDescendantFiles(targetDir, CTree.HTML).size());
		Assert.assertEquals(5, CMineGlobber.listSortedDescendantFiles(targetDir, CTree.PDF).size());
	}
	
	@Test // PMR only
	public void testUCLForest() {
		
	}

}
