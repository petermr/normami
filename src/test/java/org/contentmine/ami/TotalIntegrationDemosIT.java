package org.contentmine.ami;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.plugins.CommandProcessor;
import org.contentmine.ami.plugins.EntityAnalyzer;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.OccurrenceType;
import org.contentmine.ami.plugins.OccurrenceAnalyzer.SubType;
import org.contentmine.cproject.files.CProject;
import org.contentmine.cproject.util.CMineTestFixtures;
import org.contentmine.eucl.euclid.test.TestUtil;
import org.junit.Test;

/** these run to complete stack from PDF to co-occurrence and other tasks.
 *  
 * @author pm286
 *
 */
public class TotalIntegrationDemosIT {
	public static final Logger LOG = Logger.getLogger(TotalIntegrationDemosIT.class);
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
		if (!skipSVG) {
			CProject cProject = new CProject(targetDir);
			cProject.convertPDFOutputSVGFilesImageFiles();
		}
		if (!skipHtml) {
			CProject cProject = new CProject(targetDir);
			cProject.convertPDFSVGandWriteHtml();
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
	
	@Test
	public void testMakeCrystalSuppDataHTTP() {
		boolean skipCleanCopy = false;
		boolean skipSVG = true;
		boolean skipHtml = false;
		boolean skipRun = false;

		List<String> urlSList = Arrays.asList(new String[] {
		"https://pubs.acs.org/doi/suppl/10.1021/om049188b/suppl_file/om049188bsi20041020_053156.pdf",
//			-- born-digital, contains crystal info w/o coordinates.
		"https://pubs.acs.org/doi/suppl/10.1021/om049188b/suppl_file/om049188bsi20050104_114539.pdf",
//			-- born-digital, contains crystal info, including selectable CIF.
		"https://pubs.acs.org/doi/suppl/10.1021/om040132r/suppl_file/om040132r_s.pdf",
//			-- scanned, contains crystal info, including coordinates.
		"https://pubs.acs.org/doi/suppl/10.1021/om0489711/suppl_file/om0489711si20041230_042826.pdf",
//			-- born-digital, contains crystal info, including selectable coordinates.
		"https://pubs.acs.org/doi/suppl/10.1021/om040128f/suppl_file/om040128f_s.pdf",
//			-- scanned, poor quality, contains crystal info, including coordinates.
		});

		File sourceDir = null;
		File targetDir = new File(AMIFixtures.TARGET_TOTAL_INT_DIR, "andrius");
		CProject cProject = CProject.makeProjectFromURLs(targetDir, urlSList, 
				CProject.HTTP_ACS_SUPPDATA);
		if (1==1)return;
		cProject.convertPDFOutputSVGFilesImageFiles();
		cProject.convertPDFSVGandWriteHtml();
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " search(crystal)"
		+ " search(country)"
		+ " search(funders)"

	    ;
		File projectDir = targetDir;
		try {
			CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+cmd, e);
		}
		
		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(projectDir);
		
		entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.BINOMIAL).setMaxCount(25);
		entityAnalyzer.createAndAddOccurrenceAnalyzer(OccurrenceType.GENE, SubType.HUMAN).setMaxCount(30);
		entityAnalyzer.createAndAddOccurrenceAnalyzer("auxin");
		entityAnalyzer.writeCSVFiles();
		
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.createAllCooccurrences();

		
	}
	@Test
	public void testMakeCrystalSuppData() {

		List<String> urlSList = Arrays.asList(new String[] {
		"https://pubs.acs.org/doi/suppl/10.1021/om049188b/suppl_file/om049188bsi20041020_053156.pdf",
//			-- born-digital, contains crystal info w/o coordinates.
		"https://pubs.acs.org/doi/suppl/10.1021/om049188b/suppl_file/om049188bsi20050104_114539.pdf",
//			-- born-digital, contains crystal info, including selectable CIF.
		"https://pubs.acs.org/doi/suppl/10.1021/om040132r/suppl_file/om040132r_s.pdf",
//			-- scanned, contains crystal info, including coordinates.
		"https://pubs.acs.org/doi/suppl/10.1021/om0489711/suppl_file/om0489711si20041230_042826.pdf",
//			-- born-digital, contains crystal info, including selectable coordinates.
		"https://pubs.acs.org/doi/suppl/10.1021/om040128f/suppl_file/om040128f_s.pdf",
//			-- scanned, poor quality, contains crystal info, including coordinates.
		});

		File sourceDir = new File(AMIFixtures.TEST_TOTAL_INT_DIR, "acsSupp");
		File targetDir = new File(AMIFixtures.TARGET_TOTAL_INT_DIR, "acsSupp");
		CMineTestFixtures.cleanAndCopyDir(sourceDir, targetDir);
		CProject cProject = new CProject(targetDir);
		new CProject().run("--project "+targetDir+" --makeProject (\\1)/fulltext.pdf --fileFilter .*/(.*)\\.pdf");
		cProject.convertPDFOutputSVGFilesImageFiles();
		cProject.convertPDFSVGandWriteHtml();
		String cmd = "word(frequencies)xpath:@count>20~w.stopwords:pmcstop.txt_stopwords.txt"
		+ " search(crystal)"
		+ " search(country)"
		+ " search(funders)"

	    ;
		File projectDir = targetDir;
		try {
			CommandProcessor.main((projectDir+" "+cmd).split("\\s+"));
		} catch (IOException e) {
			throw new RuntimeException("Cannot run command: "+cmd, e);
		}
		
		EntityAnalyzer entityAnalyzer = EntityAnalyzer.createEntityAnalyzer(projectDir);
		
		entityAnalyzer.createAndAddOccurrenceAnalyzer("crystal").setMaxCount(25);
		entityAnalyzer.createAndAddOccurrenceAnalyzer("country").setMaxCount(30);
		entityAnalyzer.createAndAddOccurrenceAnalyzer("funders");
		entityAnalyzer.writeCSVFiles();
		
		entityAnalyzer.setWriteCSV(true);
		entityAnalyzer.createAllCooccurrences();

		
	}
}
