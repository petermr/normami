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

	@Test 
	// SHOWCASE
	public void testUCLForestSmall() {
		
		String cproject = "/Users/pm286/workspace/uclforest/forestplotssmall/";
		// clean the target directories 
//		new AMICleaner().runCommands("--help");
		new AMICleaner().runCommands(" --cproject " + cproject + " --dir svg/ pdfimages/ --file scholarly.html");
		
		// convert PDF to SVG and images
//		new AMIProcessorPDF().runCommands("-h");
		new AMIProcessorPDF().runCommands(" --cproject " + cproject);
		
		// filter small, monochrome and duplicate images
//		new AMIImage().runCommands("-h");
		new AMIImage().runCommands(" --cproject " + cproject);
		
		// create binarized images; 
//		new AMIBitmap().runCommands("-h");
		new AMIBitmap().runCommands(" --cproject " + cproject);
		
		// analyze bitmaps and create PixelIslands; doesn't yet create output
//		new AMIPixel().runCommands("-h");
		new AMIPixel().runCommands(" --cproject " + cproject + " --rings 3");
		
	}

	@Test 
	// SHOWCASE
	public void testUCLForestLarge() {
		
		String cproject = "/Users/pm286/workspace/uclforest/forestplots/";
		new AMICleaner().runCommands(" --cproject " + cproject + " --dir svg/ pdfimages/ --file scholarly.html");
		new AMIProcessorPDF().runCommands(" --cproject " + cproject);
		new AMIImage().runCommands(" --cproject " + cproject);
		new AMIBitmap().runCommands(" --cproject " + cproject);
		new AMIPixel().runCommands(" --cproject " + cproject + " --rings 3");
		
	}

	private void makeProject(File targetDir) {
		new AMIMakeProject().runCommands("--cproject "+ targetDir + " --rawfiletypes " + "pdf" + " -vv");
	}
	

}
