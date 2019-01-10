package org.contentmine.ami.tools;

import java.io.File;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.contentmine.cproject.files.CProject;
import org.junit.Assert;
import org.junit.Test;

/** test AMIProcessorPDF
 * 
 * @author pm286
 *
 */
public class AMIPDFTest {
	private static final Logger LOG = Logger.getLogger(AMIPDFTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	/** reads UCL corpus as PDFs and creates first pass SVG , images and scholarly html
	 * 
	 */
	public void testForestPlotsSmall() throws Exception {

//		log4j.appender.file=org.apache.log4j.DailyRollingFileAppender
//				log4j.appender.file.File=${user.home}/logs/app.log
//				log4j.appender.file.layout=org.apache.log4j.PatternLayout
//				log4j.appender.file.layout.ConversionPattern=%d [%t] %c %p %m%n
		String filename = "foo";
		boolean append = true;
		Layout layout = new PatternLayout();
		Appender appender = new FileAppender(layout, filename, append);
		String args = 
				"-p /Users/pm286/workspace/uclforest/forestplotssmall"
				+ " --rawfiletypes pdf"
				+ " --maxpages 20"
				+ " --pdfimages false"
				+ " --svgpages true"
				;
//		Assert.assertTrue(new File(args[1]).exists());
		AMIPDFTool amiProcessorPDF = new AMIPDFTool();
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
		new AMICleanTool().runCommands("-p " + projectDir + " --dir svg/");
		// and then recreate them
		String[] args = {
				"-p", projectDir,
				"--rawfiletypes", "pdf",
				"--maxpages", "20",
				"--pdfimages", "false",
				"--svgpages", "true",
				};
		Assert.assertTrue(new File(args[1]).exists());
		AMIPDFTool amiProcessorPDF = new AMIPDFTool();
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
		AMIPDFTool amiProcessorPDF = new AMIPDFTool();
		amiProcessorPDF.runCommands(args);
	}

}
