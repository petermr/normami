package org.contentmine.ami;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.cproject.files.CProject;
import org.junit.Assert;
import org.junit.Test;

/** test AMIProcessorPDF
 * 
 * @author pm286
 *
 */
public class AMIProcessorPDFTest {
	private static final Logger LOG = Logger.getLogger(AMIProcessorPDFTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	/** reads UCL corpus as PDFs and creates first pass SVG , images and scholarly html
	 * 
	 */
	public void testForestPlotsSmall() throws Exception {
		String[] args = {
				"-p", "/Users/pm286/workspace/uclforest/forestplotssmall",
				"--rawfiletypes", "pdf",
				"--maxpages", "20",
				"--pdfimages", "false",
				"--svgpages", "true",
				};
		Assert.assertTrue(new File(args[1]).exists());
		AMIProcessorPDF amiProcessorPDF = new AMIProcessorPDF();
		amiProcessorPDF.runCommands(args);
		CProject cProject = amiProcessorPDF.getCProject();
		Assert.assertNotNull("CProject not null", cProject);
	}

	@Test
	/** reads UCL corpus as PDFs and creates first pass SVG , images and scholarly html
	 * 
	 */
	public void testForestPlotsSmallSVG() throws Exception {
		String projectDir = "/Users/pm286/workspace/uclforest/forestplotssmall";
		// delete the existing svg/ directories
		new AMICleaner().runCommands("-p " + projectDir + " --dir svg/");
		// and then recreate them
		String[] args = {
				"-p", projectDir,
				"--rawfiletypes", "pdf",
				"--maxpages", "20",
				"--pdfimages", "false",
				"--svgpages", "true",
				};
		Assert.assertTrue(new File(args[1]).exists());
		AMIProcessorPDF amiProcessorPDF = new AMIProcessorPDF();
		amiProcessorPDF.runCommands(args);
		CProject cProject = amiProcessorPDF.getCProject();
		Assert.assertNotNull("CProject not null", cProject);
	}

	@Test
	/** reads UCL corpus as PDFs and creates first pass PDFImages
	 * 
	 */
	public void testForestPlotsSmallPDFImages() throws Exception {
		String projectDir = "/Users/pm286/workspace/uclforest/forestplotssmall";
		// delete the existing pdfimagesdirectories
//		new AMICleaner().runCommands("-p " + projectDir + " --dir pdfimages/");
		// and then recreate them
		String[] args = {
				"-p", projectDir,
				"--rawfiletypes", "pdf",
				"--maxpages", "20",
				"--pdfimages", "true",
				"--svgpages", "false",
				};
		AMIProcessorPDF amiProcessorPDF = new AMIProcessorPDF();
		amiProcessorPDF.runCommands(args);
	}

}
