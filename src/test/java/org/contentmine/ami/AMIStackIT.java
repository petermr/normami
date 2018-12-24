package org.contentmine.ami;

import java.io.File;

import org.contentmine.cproject.util.CMineTestFixtures;
import org.junit.Test;

public class AMIStackIT {

	@Test 
	public void testUCLForestMakeprojectIT() {
		String project = "uclforestopen";
		File targetDir = new File(AMIFixtures.TARGET_AMISTACK_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(
				new File(AMIFixtures.TEST_AMISTACK_DIR, project),
				targetDir);
		String cmd = "--cproject "+ targetDir + " --rawfiletypes " + "pdf" + " -vv";
		AMIMakeProject amiMakeProject = new AMIMakeProject();
		amiMakeProject.runCommands(cmd);
		
	}
	
	@Test 
	public void testUCLForestPDFIT() {
		String cmd;
		String project = "uclforestopen";
		File targetDir = new File(AMIFixtures.TARGET_AMISTACK_DIR, project);
		CMineTestFixtures.cleanAndCopyDir(
				new File(AMIFixtures.TEST_AMISTACK_DIR, project),
				targetDir);
		
//		makeProject(targetDir);
		cmd = "--cproject "+ targetDir ;
		AMIProcessorPDF readPDF = new AMIProcessorPDF();
		readPDF.runCommands(cmd);
		
		
	}

	private void makeProject(File targetDir) {
		new AMIMakeProject().runCommands("--cproject "+ targetDir + " --rawfiletypes " + "pdf" + " -vv");
	}
	

}
