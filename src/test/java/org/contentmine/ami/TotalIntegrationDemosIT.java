package org.contentmine.ami;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.ami.plugins.EntityAnalyzer;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.OccurrenceType;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.SubType;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.files.CTree;
import org.contentmine.cproject.files.CTreeList;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.eucl.euclid.test.TestUtil;
import org.contentmine.eucl.xml.XMLUtil;
import org.contentmine.graphics.html.HtmlHtml;
import org.contentmine.pdf2svg2.PDFDocumentProcessor;
import org.contentmine.svg2xml.pdf.SVGDocumentProcessor;
import org.junit.Test;

/** these run to complete stack from PDF to co-occurrence and other tasks.
 *  
 * @author pm286
 *
 */
public class TotalIntegrationDemosIT {
	private static final Logger LOG = Logger.getLogger(TotalIntegrationDemosIT.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testMakeBiorxiv() {
		
		boolean skipCleanCopy = true;
		boolean skipSVG = true;
		boolean skipHtml = false;
		boolean skipRun = false;
		File sourceDir = new File(AMIFixtures.TEST_TOTAL_INT_DIR, "biorxiv/all39");
		File targetDir = new File(AMIFixtures.TARGET_TOTAL_INT_DIR, "biorxiv/all39");
		if (!TestUtil.checkForeignDirExists(sourceDir)) return;
		if (!skipCleanCopy) CMineTestFixtures.cleanAndCopyDir(sourceDir, targetDir);
		new CProject().run("--project "+targetDir+" --makeProject (\\1)/fulltext.pdf --fileFilter .*/(.*)\\.pdf");
		LOG.debug("xxxxxxxxxxxxx");
		CProject cProject = null;
		CTreeList cTreeList = null;
		if (!skipSVG) {
//			CMineTestFixtures.cleanAndCopyDir(sourceDir, targetDir);
			cProject = new CProject(targetDir);
			cTreeList = cProject.getOrCreateCTreeList();
			for (CTree cTree : cTreeList) {
				LOG.debug("******* "+cTree+" **********");
				List<File> svgFiles = cTree.getExistingSVGFileList();
			    PDFDocumentProcessor documentProcessor = new PDFDocumentProcessor();
			    documentProcessor.setMinimumImageBox(100, 100);
			    try {
				    documentProcessor.readAndProcess(cTree.getExistingFulltextPDF());
				    File outputDir = new File(targetDir, cTree.getName());
					documentProcessor.writeSVGPages(outputDir);
			    	documentProcessor.writeRawImages(outputDir);
			    } catch (IOException ioe) {
			    	LOG.error("cannot read/process: " + cTree + "; "+ioe);
			    }
			}
		}
		if (!skipHtml) {
			cProject = new CProject(targetDir);
			cTreeList = cProject.getOrCreateCTreeList();
			for (CTree cTree : cTreeList) {
				String name = cTree.getName();
				if (name.equals("220731")) {
					LOG.debug("skip: "+name);
					continue;
				}
				LOG.debug("-----------" + name + "-----------");
				List<File> svgFiles = cTree.getExistingSVGFileList();
				SVGDocumentProcessor svgDocumentProcessor = new SVGDocumentProcessor();
				svgDocumentProcessor.readSVGFilesIntoSortedPageList(svgFiles);
				HtmlHtml html = svgDocumentProcessor.readAndConvertToHtml(svgFiles);
				File htmlFile = new File(new File(targetDir, cTree.getName()), "scholarly.html");
				try {
					XMLUtil.debug(html, htmlFile, 1);
				} catch (IOException e) {
					LOG.error("Cannot write html: " + htmlFile);
				}
			}
		}
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " species(binomial)"
		+ " gene(human) "
		+ " search(auxin)"
		+ " search(plantDevelopment)"
//		+ " search(pectin)"
+ " search(plantparts)"
+ " search(country)"
+ " search(funders)"
		+ " search(synbio)"

	    ;
		File projectDir = targetDir;
		if (!skipRun) {
			try {
				CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
			} catch (IOException e) {
				throw new RuntimeException("Cannot run command: "+cmd, e);
			}
		}
		
		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(projectDir);
		
		entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.BINOMIAL).setMaxCount(25);
		entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.GENE, SubType.HUMAN).setMaxCount(30);
		entityAnalyzer.createAndAddOccurrenceAnalyzer("auxin");
		entityAnalyzer.writeCSVFiles();
		
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.createAllCooccurrences();

		

	}
}
